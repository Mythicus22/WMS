package com.example.myapplication.shared.features.reports.repository

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import android.graphics.Typeface
import java.io.ByteArrayOutputStream

class AndroidPlatformFileExporter(private val context: Context) : PlatformFileExporter {
    override suspend fun exportFile(
        outputDirectoryUri: String,
        fileName: String,
        mimeType: String,
        content: ByteArray
    ): String = withContext(Dispatchers.IO) {
        try {
            val dirUri = Uri.parse(outputDirectoryUri)
            val documentFile = DocumentFile.fromTreeUri(context, dirUri)
                ?: throw IllegalArgumentException("Invalid output directory URI")

            val file = documentFile.createFile(mimeType, fileName)
                ?: throw IllegalStateException("Could not create file $fileName in the selected directory")

            context.contentResolver.openOutputStream(file.uri)?.use { outputStream ->
                if (mimeType == "application/pdf") {
                    val pdfDocument = PdfDocument()
                    val textContent = String(content)
                    val lines = textContent.split("\n")
                    
                    val paint = Paint()
                    paint.textSize = 12f
                    paint.typeface = Typeface.MONOSPACE
                    
                    var yPosition = 30f
                    var pageNumber = 1
                    var pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create() // A4 size
                    var page = pdfDocument.startPage(pageInfo)
                    
                    for (line in lines) {
                        if (yPosition > 800f) {
                            pdfDocument.finishPage(page)
                            pageNumber++
                            pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                            page = pdfDocument.startPage(pageInfo)
                            yPosition = 30f
                        }
                        page.canvas.drawText(line, 30f, yPosition, paint)
                        yPosition += 15f
                    }
                    pdfDocument.finishPage(page)
                    
                    pdfDocument.writeTo(outputStream)
                    pdfDocument.close()
                } else {
                    outputStream.write(content)
                }
                outputStream.flush()
            } ?: throw IllegalStateException("Could not open output stream for file")

            return@withContext file.uri.toString()
        } catch (e: Exception) {
            Napier.e("Failed to export file: $fileName", e, "AndroidPlatformFileExporter")
            throw e
        }
    }
}

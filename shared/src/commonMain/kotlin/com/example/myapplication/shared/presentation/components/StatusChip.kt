package com.example.myapplication.shared.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.myapplication.shared.presentation.theme.AppColors
import com.example.myapplication.shared.presentation.theme.AppDimensions

enum class ChipStatus {
    SUCCESS, WARNING, ERROR, INFO
}

@Composable
fun StatusChip(
    text: String,
    status: ChipStatus,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (status) {
        ChipStatus.SUCCESS -> AppColors.Success.copy(alpha = 0.2f)
        ChipStatus.WARNING -> AppColors.Warning.copy(alpha = 0.2f)
        ChipStatus.ERROR -> AppColors.Error.copy(alpha = 0.2f)
        ChipStatus.INFO -> AppColors.Info.copy(alpha = 0.2f)
    }

    val textColor = when (status) {
        ChipStatus.SUCCESS -> AppColors.Success
        ChipStatus.WARNING -> AppColors.Warning
        ChipStatus.ERROR -> AppColors.Error
        ChipStatus.INFO -> AppColors.Info
    }

    Box(
        modifier = modifier
            .background(backgroundColor, shape = RoundedCornerShape(AppDimensions.cornerRadiusSmall))
            .padding(horizontal = AppDimensions.spacing8, vertical = AppDimensions.spacing4)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}


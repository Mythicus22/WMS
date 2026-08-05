package com.example.myapplication.shared.core.security

import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import java.security.SecureRandom
import java.util.Base64

class SecurityProviderImpl : SecurityProvider {
    
    // Argon2id parameters
    private val iterations = 3
    private val memLimit = 65536 // 64 MB
    private val hashLength = 32
    private val parallelism = 4

    override fun hashPassword(password: String): String {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        
        val builder = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withVersion(Argon2Parameters.ARGON2_VERSION_13)
            .withIterations(iterations)
            .withMemoryAsKB(memLimit)
            .withParallelism(parallelism)
            .withSalt(salt)
            
        val generate = Argon2BytesGenerator()
        generate.init(builder.build())
        
        val result = ByteArray(hashLength)
        generate.generateBytes(password.toByteArray(Charsets.UTF_8), result, 0, result.size)
        
        val saltBase64 = Base64.getEncoder().withoutPadding().encodeToString(salt)
        val hashBase64 = Base64.getEncoder().withoutPadding().encodeToString(result)
        
        // Return standard encoded string: $argon2id$v=19$m=65536,t=3,p=4$<salt>$<hash>
        return "\$argon2id\$v=19\$m=$memLimit,t=$iterations,p=$parallelism\$$saltBase64\$$hashBase64"
    }

    override fun verifyPassword(password: String, hash: String): Boolean {
        try {
            val parts = hash.split("$")
            if (parts.size != 6 || parts[1] != "argon2id") return false
            
            val paramsPart = parts[3]
            var m = memLimit
            var t = iterations
            var p = parallelism
            
            paramsPart.split(",").forEach { param ->
                val kv = param.split("=")
                when (kv[0]) {
                    "m" -> m = kv[1].toInt()
                    "t" -> t = kv[1].toInt()
                    "p" -> p = kv[1].toInt()
                }
            }
            
            val salt = Base64.getDecoder().decode(parts[4])
            val expectedHashBase64 = parts[5]
            
            val builder = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withIterations(t)
                .withMemoryAsKB(m)
                .withParallelism(p)
                .withSalt(salt)
                
            val generate = Argon2BytesGenerator()
            generate.init(builder.build())
            
            val result = ByteArray(hashLength)
            generate.generateBytes(password.toByteArray(Charsets.UTF_8), result, 0, result.size)
            
            val resultBase64 = Base64.getEncoder().withoutPadding().encodeToString(result)
            return resultBase64 == expectedHashBase64
        } catch (e: Exception) {
            return false
        }
    }
}

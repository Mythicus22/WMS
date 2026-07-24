package com.example.myapplication.shared.domain.validation

// Validation utilities placeholder
interface Validator<T> {
    fun validate(value: T): ValidationResult
}

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val errors: List<String>) : ValidationResult()
}


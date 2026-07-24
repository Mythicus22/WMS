package com.example.myapplication.shared.domain.usecase

// Use case base interface and implementations placeholder
interface UseCase<in Params, out Result> {
    suspend operator fun invoke(params: Params): Result
}

interface NoParamsUseCase<out Result> {
    suspend operator fun invoke(): Result
}


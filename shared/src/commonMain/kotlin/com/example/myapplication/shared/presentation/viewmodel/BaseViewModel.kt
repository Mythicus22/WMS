package com.example.myapplication.shared.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.myapplication.shared.core.common.UiEvent
import com.example.myapplication.shared.core.common.UiEffect
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

open class BaseViewModel<State, Event : UiEvent, Effect : UiEffect> : ViewModel() {

    protected val _uiState = MutableStateFlow<State?>(null)
    val uiState: StateFlow<State?> = _uiState.asStateFlow()

    protected val _uiEffect = MutableSharedFlow<Effect>()
    val uiEffect: SharedFlow<Effect> = _uiEffect.asSharedFlow()

    protected suspend fun emitEffect(effect: Effect) {
        _uiEffect.emit(effect)
    }

    protected fun setState(newState: State) {
        _uiState.value = newState
    }

    open fun onEvent(event: Event) {
        // Override in subclasses to handle events
    }
}


package com.bhojnify.core.repository

import com.bhojnify.core.model.MessState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryMessRepository(
    initialState: MessState = MessState.defaultInitialState()
) : MessRepository {

    private val mutex = Mutex()
    private val _stateFlow = MutableStateFlow(initialState)
    override val stateFlow: StateFlow<MessState> = _stateFlow.asStateFlow()

    override suspend fun getState(): MessState = mutex.withLock {
        _stateFlow.value
    }

    override suspend fun saveState(state: MessState) {
        mutex.withLock {
            _stateFlow.value = state
        }
    }

    override suspend fun updateState(transform: (MessState) -> MessState): MessState = mutex.withLock {
        val updated = transform(_stateFlow.value)
        _stateFlow.value = updated
        updated
    }

    override suspend fun resetToDefault(): MessState = mutex.withLock {
        val defaultState = MessState.defaultInitialState()
        _stateFlow.value = defaultState
        defaultState
    }
}

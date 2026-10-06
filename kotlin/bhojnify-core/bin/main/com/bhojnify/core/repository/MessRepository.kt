package com.bhojnify.core.repository

import com.bhojnify.core.model.MessState
import kotlinx.coroutines.flow.StateFlow

interface MessRepository {
    val stateFlow: StateFlow<MessState>
    suspend fun getState(): MessState
    suspend fun saveState(state: MessState)
    suspend fun updateState(transform: (MessState) -> MessState): MessState
    suspend fun resetToDefault(): MessState
}

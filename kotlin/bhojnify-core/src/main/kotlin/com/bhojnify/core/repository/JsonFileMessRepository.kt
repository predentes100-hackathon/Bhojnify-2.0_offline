package com.bhojnify.core.repository

import com.bhojnify.core.model.MessState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class JsonFileMessRepository(
    private val storageFile: File
) : MessRepository {

    private val mutex = Mutex()
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val _stateFlow = MutableStateFlow(loadInitial())
    override val stateFlow: StateFlow<MessState> = _stateFlow.asStateFlow()

    private fun loadInitial(): MessState {
        return try {
            if (storageFile.exists()) {
                val content = storageFile.readText(Charsets.UTF_8)
                json.decodeFromString<MessState>(content)
            } else {
                MessState.defaultInitialState()
            }
        } catch (e: Exception) {
            System.err.println("State parsing failed! Corrupt file preserved.")
            val backup = File(storageFile.parentFile, "${storageFile.name}.corrupt-${System.currentTimeMillis()}")
            storageFile.renameTo(backup)
            MessState.defaultInitialState()
        }
    }

    override suspend fun getState(): MessState = mutex.withLock {
        _stateFlow.value
    }

    override suspend fun saveState(state: MessState) {
        mutex.withLock {
            _stateFlow.value = state
            persistToFile(state)
        }
    }

    override suspend fun updateState(transform: (MessState) -> MessState): MessState = mutex.withLock {
        val updated = transform(_stateFlow.value)
        _stateFlow.value = updated
        persistToFile(updated)
        updated
    }

    override suspend fun resetToDefault(): MessState = mutex.withLock {
        val defaultState = MessState.defaultInitialState()
        _stateFlow.value = defaultState
        persistToFile(defaultState)
        defaultState
    }

    private suspend fun persistToFile(state: MessState) = withContext(Dispatchers.IO) {
        try {
            storageFile.parentFile?.mkdirs()
            val text = json.encodeToString(MessState.serializer(), state)
            val tempFile = File(storageFile.parentFile, "${storageFile.name}.tmp")
            tempFile.writeText(text, Charsets.UTF_8)
            Files.move(tempFile.toPath(), storageFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        } catch (e: Exception) {
            System.err.println("Failed to persist state: ${e.message}")
        }
    }
}

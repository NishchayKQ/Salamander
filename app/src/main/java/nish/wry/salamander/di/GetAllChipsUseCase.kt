package nish.wry.salamander.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import nish.wry.salamander.data.room.task.Chip


class GetAllChipsUseCase(
    private val repository: TaskRepository,
) {
    operator fun invoke(viewModelScope: CoroutineScope): StateFlow<List<Chip>> {
        return repository.getAllChips()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = listOf()
            )
    }

    private companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
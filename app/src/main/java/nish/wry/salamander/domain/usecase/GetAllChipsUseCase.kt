package nish.wry.salamander.domain.usecase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn


class GetAllChipsUseCase<GenericChip>(
    private val getAllChips: () -> Flow<List<GenericChip>>,
) {
    operator fun invoke(viewModelScope: CoroutineScope): StateFlow<List<GenericChip>> {
        return getAllChips()
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
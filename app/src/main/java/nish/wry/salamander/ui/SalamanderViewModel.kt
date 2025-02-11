package nish.wry.salamander.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import nish.wry.salamander.data.MutableSaveStateFlow
import nish.wry.salamander.ui.navigation.MainDestination
import nish.wry.salamander.ui.screens.MainNishchayDestination
import nish.wry.salamander.ui.screens.MainSuBaseDestination
import nish.wry.salamander.ui.screens.MainTaskDestination

class SalamanderViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    val listOfDestination: List<MainDestination> = listOf(
        MainSuBaseDestination, MainTaskDestination, MainNishchayDestination
    )
    private val _currentDestination = MutableSaveStateFlow(
        savedStateHandle = savedStateHandle,
        key = CURRENT_DESTINATION_KEY,
        defaultValue = listOfDestination[1]
    )

    val currentDestination: StateFlow<MainDestination> = _currentDestination.asStateFlow()

    fun setDestination(destination: MainDestination) {
        _currentDestination.update {
            destination
        }
    }


    private companion object {
        private const val CURRENT_DESTINATION_KEY = "CURRENT_DESTINATION"
    }
}
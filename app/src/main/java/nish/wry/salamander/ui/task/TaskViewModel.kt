package nish.wry.salamander.ui.task

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import nish.wry.salamander.di.TaskRepository

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState = _uiState.asStateFlow()

    fun updateZoomAndScroll(newScale: Float, newOffsetY: Float) {
        _uiState.update {
            TaskUiState(scale = newScale, scrollOffset = newOffsetY)
        }

    }


}

data class TaskUiState(
    val scale: Float = 1f,
    val scrollOffset: Float = 0f,
)
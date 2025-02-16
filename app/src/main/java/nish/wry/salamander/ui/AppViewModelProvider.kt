package nish.wry.salamander.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import nish.wry.salamander.data.TaskDataSource
import nish.wry.salamander.data.TaskToTaskDrawingData
import nish.wry.salamander.di.GetAllChipsUseCase
import nish.wry.salamander.di.SalamanderApplication
import nish.wry.salamander.ui.chip.create.CreateChipViewModel
import nish.wry.salamander.ui.task.TaskViewModel
import nish.wry.salamander.ui.task.create.CreateTaskViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val taskToTaskDrawingData =
                TaskToTaskDrawingData.getInstance(salamanderApplication().container.taskRepository)
            TaskViewModel(
                savedStateHandle = this.createSavedStateHandle(),
                repository = salamanderApplication().container.taskRepository,
                taskDataSource = TaskDataSource.getInstance(taskToTaskDrawingData),
                getAllChipsUseCase = GetAllChipsUseCase(salamanderApplication().container.taskRepository)
            )
        }
        initializer {
            CreateTaskViewModel(
                savedStateHandle = this.createSavedStateHandle(),
                repository = salamanderApplication().container.taskRepository,
                getAllChipsUseCase = GetAllChipsUseCase(salamanderApplication().container.taskRepository)
            )
        }
        initializer {
            CreateChipViewModel(
                savedStateHandle = this.createSavedStateHandle(),
                repository = salamanderApplication().container.taskRepository
            )
        }
    }
}

fun CreationExtras.salamanderApplication(): SalamanderApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SalamanderApplication)
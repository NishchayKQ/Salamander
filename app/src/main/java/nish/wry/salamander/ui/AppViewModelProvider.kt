package nish.wry.salamander.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import nish.wry.salamander.di.SalamanderApplication
import nish.wry.salamander.ui.task.TaskViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            SalamanderViewModel()
        }
        initializer {
            TaskViewModel(
//                salamanderApplication().container.taskRepository
            )
        }
    }
}

fun CreationExtras.salamanderApplication(): SalamanderApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SalamanderApplication)
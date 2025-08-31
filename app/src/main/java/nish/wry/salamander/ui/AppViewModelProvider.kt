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
import nish.wry.salamander.ui.life.PaymentChipViewModel
import nish.wry.salamander.ui.life.PaymentHistoryViewModel
import nish.wry.salamander.ui.life.PaymentRecordScreenViewModel
import nish.wry.salamander.ui.suBase.SubBaseViewModel
import nish.wry.salamander.ui.suBase.category.CategoryViewModel
import nish.wry.salamander.ui.taskTab.chip.CreateChipViewModel
import nish.wry.salamander.ui.taskTab.main.TaskViewModel
import nish.wry.salamander.ui.taskTab.task.CreateTaskViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            CategoryViewModel(
                activityRepository = salamanderApplication().container.activityRepository,
                savedStateHandle = this.createSavedStateHandle()
            )
        }


        initializer {
            SubBaseViewModel(
                dateTimeTracker = salamanderApplication().container.dateTimeTracker,
                activityRepository = salamanderApplication().container.activityRepository,
                savedStateHandle = this.createSavedStateHandle()
            )
        }
        initializer {
            val taskToTaskDrawingData =
                TaskToTaskDrawingData.getInstance(salamanderApplication().container.taskRepository)
            TaskViewModel(
                savedStateHandle = this.createSavedStateHandle(),
                dateTimeTracker = salamanderApplication().container.dateTimeTracker,
                repository = salamanderApplication().container.taskRepository,
                taskDataSource = TaskDataSource.getInstance(taskToTaskDrawingData),
                getAllChipsUseCase = GetAllChipsUseCase(salamanderApplication().container.taskRepository::getAllChips)
            )
        }
        initializer {
            CreateTaskViewModel(
                savedStateHandle = this.createSavedStateHandle(),
                repository = salamanderApplication().container.taskRepository,
                getAllChipsUseCase = GetAllChipsUseCase(salamanderApplication().container.taskRepository::getAllChips)
            )
        }
        initializer {
            CreateChipViewModel(
                savedStateHandle = this.createSavedStateHandle(),
                repository = salamanderApplication().container.taskRepository
            )
        }

        initializer {
            PaymentRecordScreenViewModel(
                savedStateHandle = this.createSavedStateHandle(),
                getAllPaymentChipsUseCase = GetAllChipsUseCase(salamanderApplication().container.paymentRepository::getAllPaymentChips),
                paymentRepository = salamanderApplication().container.paymentRepository
            )
        }

        initializer {
            PaymentChipViewModel(
                savedStateHandle = this.createSavedStateHandle(),
                repository = salamanderApplication().container.paymentRepository
            )
        }

        initializer {
            PaymentHistoryViewModel(
                paymentRepository = salamanderApplication().container.paymentRepository
            )
        }
    }
}

fun CreationExtras.salamanderApplication(): SalamanderApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SalamanderApplication)
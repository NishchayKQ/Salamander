package nish.wry.salamander.ui.screens

import android.os.Parcelable
import androidx.activity.compose.BackHandler
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import nish.wry.salamander.R
import nish.wry.salamander.ui.AppViewModelProvider
import nish.wry.salamander.ui.chip.CreateChip
import nish.wry.salamander.ui.navigation.NavigationDestination
import nish.wry.salamander.ui.newTask.NewTask
import nish.wry.salamander.ui.task.TaskTimelineScreen
import nish.wry.salamander.ui.task.TaskViewModel

@Serializable
object TaskDestination : NavigationDestination {
    override val titleRes: Int = R.string.task
    override val iconRes = R.drawable.outline_checklist_24
}


@Parcelize
sealed class InTaskDestination(val role: ThreePaneScaffoldRole) : Parcelable {
    data object Timeline : InTaskDestination(ListDetailPaneScaffoldRole.List)
    data object CreateTask : InTaskDestination(ListDetailPaneScaffoldRole.Extra)
    data object CreateChip : InTaskDestination(ListDetailPaneScaffoldRole.Extra)
    data class EditTask(val taskId: Int) : InTaskDestination(ListDetailPaneScaffoldRole.Detail)
    data class EditChip(val chipId: Int) : InTaskDestination(ListDetailPaneScaffoldRole.Detail)
}


@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun TaskScreen() {
    val navigator = rememberListDetailPaneScaffoldNavigator<InTaskDestination>()

    BackHandler(navigator.canNavigateBack()) {
        navigator.navigateBack()
    }

    ListDetailPaneScaffold(directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            AnimatedPane {
                TaskTimelineScreen(onAddTaskClick = {
                    navigator.navigateTo(
                        InTaskDestination.CreateTask.role,
                        InTaskDestination.CreateTask
                    )
                })
            }
        },
        detailPane = {
            AnimatedPane { }
        },
        extraPane = {
            AnimatedPane {
                navigator.currentDestination?.content?.let { inTaskDestination ->
                    when (inTaskDestination) {
                        InTaskDestination.CreateChip -> {
                            CreateChip(exitChip = {
                                navigator.navigateTo(
                                    InTaskDestination.CreateTask.role,
                                    InTaskDestination.CreateTask
                                )
                            })
                        }

                        InTaskDestination.CreateTask -> {
                            NewTask(
                                onCreateChip = {
                                    navigator.navigateTo(
                                        InTaskDestination.CreateChip.role,
                                        InTaskDestination.CreateChip
                                    )
                                },
                                exitCreateTask = {
                                    navigator.navigateTo(
                                        InTaskDestination.Timeline.role,
                                        InTaskDestination.Timeline
                                    )
                                })
                        }

                        else -> {

                        }
                    }
                }
            }

        })
}


@Preview
@Composable
fun TaskScreenPreview() {
    // get expanded screen preview
    val viewModel: TaskViewModel = viewModel(factory = AppViewModelProvider.Factory)
    viewModel.setSearchExpandedState(false)
    TaskScreen()
}
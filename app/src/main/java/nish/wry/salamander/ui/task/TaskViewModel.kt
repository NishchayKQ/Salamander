package nish.wry.salamander.ui.task

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import nish.wry.salamander.data.Constants
import nish.wry.salamander.data.MutableSaveStateFlow
import nish.wry.salamander.data.Week
import nish.wry.salamander.data.and
import nish.wry.salamander.data.room.Chip
import nish.wry.salamander.data.room.Task
import nish.wry.salamander.di.GetAllChipsUseCase
import nish.wry.salamander.di.TaskRepository
import java.util.Calendar
import java.util.PriorityQueue

class TaskViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: TaskRepository,
    getAllChipsUseCase: GetAllChipsUseCase,
) : ViewModel() {

    // timeline stuff
    private val _timelineUiState = MutableSaveStateFlow(
        savedStateHandle = savedStateHandle,
        key = TIMELINE_UI_STATE_KEY,
        defaultValue = TimelineUiState()
    )
    val timelineUiState = _timelineUiState.asStateFlow()

    // searchbar stuff
    private val _taskScreenUiState = MutableSaveStateFlow(
        savedStateHandle = savedStateHandle,
        key = TASK_SCREEN_UI_STATE_KEY,
        defaultValue = TaskScreenUiState()
    )

    val taskScreenUiState = _taskScreenUiState.asStateFlow()

    fun setSearchExpandedState(value: Boolean) {
        _taskScreenUiState.update { cur ->
            cur.copy(expanded = value)
        }
    }

    fun updateSearchQuery(query: String) {
        _taskScreenUiState.update { cur ->
            cur.copy(query = query)
        }
    }

    // chips
    val chips: StateFlow<List<Chip>> = getAllChipsUseCase(viewModelScope)

    fun onChipClicked(chipId: Int) {
        _timelineUiState.update { cur ->
            val updateChipIds = if (chipId in cur.selectedChipIDs) {
                cur.selectedChipIDs - chipId
            } else cur.selectedChipIDs + chipId
            cur.copy(selectedChipIDs = updateChipIds)
        }
    }

    // TODO the scope and stuff, repo should put it in recycle bin with today's date as deleted date, delete in 30 days
    fun onDeleteTaskClicked(taskId: Int) {
        viewModelScope.launch {
            val task = repository.getTaskWithId(taskId).first()
            repository.deleteTask(task)
        }
    }

    fun saveScrollState(scroll: Int, scale: Float) {
        _timelineUiState.update { cur ->
            cur.copy(scrollValue = scroll, scale = scale)
        }
    }

    private val cal: Calendar = Calendar.getInstance()

    // storing as DayOfTheYear, warning data integrity fails if at a time more than a years worth of tasks are stored
    // TODO we can store today's task(or this week's) separately in a variable
    //  1. always have access to current date's data at a click of a button
    //  2. we can have query is db with offset == null for the paging integration
    //  so we don't pull the task we don't have to show (we show offset task only for today
    // TODO refactor this to be maintainable
    val currentDayDataFlow: StateFlow<Map<Int, List<Cluster>>> =
        repository.getTaskForTwoDays(date = cal).map { taskList ->
            val cal: Calendar = Calendar.getInstance()
            val minsInADay = 1440
            val tomorrowCal = cal.clone() as Calendar
            tomorrowCal.add(Calendar.DAY_OF_YEAR, 1)

            val currentTimeInMins = (cal[Calendar.HOUR_OF_DAY] * 60) + cal[Calendar.MINUTE]

            val todayCalendarInt = cal[Calendar.DAY_OF_WEEK]

            val todayWeek = Week.calenderDayToWeekEnum(todayCalendarInt)
            val tomorrowWeek =
                Week.calenderDayToWeekEnum(Week.giveNextValidWeekdayNumber(todayCalendarInt))

            val map = mutableMapOf<Int, MutableList<TaskOnGraph>>()
            val valueTransform: (task: Task) -> TaskOnGraph = { task: Task ->
                val minutes: Int
                if (task.dateTime != null) {
                    val dateTime = task.dateTime
                    minutes = (dateTime[Calendar.HOUR_OF_DAY] * 60) + dateTime[Calendar.MINUTE]
                } else {
                    require(task.offsetHours != null) { "both dateTime and offsetHours null for task id = ${task.id}, task = $task" }
                    minutes =
                        if (task.offsetHours * 60 + currentTimeInMins < minsInADay) currentTimeInMins + task.offsetHours * 60
                        else
                        // subtract from offset the remaining hours of today (its being added to tomorrow)
                            task.offsetHours * 60 - (minsInADay - currentTimeInMins)
                }

                TaskOnGraph(
                    id = task.id,
                    name = task.name,
                    startMins = minutes,
                    endMins = minutes + Constants.TASK_DURATION
                )

            }

            // populate the first map
            for (elem in taskList) {
                var done = false
                // repeating task get caught first so that we add em multiple times if needed to
                if (elem.repeatOnDaysBitFlag and todayWeek > 0) {
                    val list = map.getOrPut(cal[Calendar.DAY_OF_YEAR]) { ArrayList() }
                    list.add(valueTransform(elem))
                    done = true
                }
                if (elem.repeatOnDaysBitFlag and tomorrowWeek > 0) {
                    val list = map.getOrPut(tomorrowCal[Calendar.DAY_OF_YEAR]) { ArrayList() }
                    list.add(valueTransform(elem))
                    done = true
                }
                if (!done) {
                    // they have a valid upcoming datetime
                    if (elem.dateTime != null) {
                        val list =
                            map.getOrPut(elem.dateTime[Calendar.DAY_OF_YEAR]) { ArrayList() }
                        list.add(valueTransform(elem))
                    }
                    // offset tasks
                    else {
                        require(elem.offsetHours != null) { "both dateTime and offsetHours null for task id = ${elem.id}, task = $elem" }
                        val list = map.getOrPut(
                            if (elem.offsetHours * 60 + currentTimeInMins < minsInADay) {
                                cal[Calendar.DAY_OF_YEAR]
                            } else {
                                tomorrowCal[Calendar.DAY_OF_YEAR]
                            }
                        ) { ArrayList() }
                        list.add(valueTransform(elem))
                    }
                }

            }

            // make clusters using first map (safe with variable task end Times)
            val clusterMap = map.mapValues { entry: Map.Entry<Int, MutableList<TaskOnGraph>> ->
                // sort by starting time
                entry.value.sortBy { it.startMins }

                val parentList = mutableListOf(Cluster())
                var lastClusterIndex = 0

                for (elem in entry.value) {
                    // cluster empty or cluster's biggestEndTime is small
                    var lastCluster = parentList[lastClusterIndex]
                    if (lastCluster.list.isEmpty() || lastCluster.biggestEndTime > elem.startMins) {
                        lastCluster.list.add(elem)
                        if (lastCluster.biggestEndTime < elem.endMins) {
                            lastCluster.biggestEndTime = elem.endMins
                        }
                    } else {
                        lastClusterIndex += 1
                        parentList.add(Cluster())
                        lastCluster = parentList[lastClusterIndex]
                        lastCluster.list.add(elem)
                        lastCluster.biggestEndTime = elem.endMins
                    }

                }
                parentList
            }

            // counting max no of simultaneous task for each cluster
            // (not safe with variable end time), works for const task lengths
            clusterMap.forEach { entry: Map.Entry<Int, MutableList<Cluster>> ->
                for (cluster in entry.value) {
                    var max = 1
                    var lastEndTime = -1
                    for (task in cluster.list) {
                        if (task.startMins < lastEndTime) {
                            max += 1
                        } else {
                            lastEndTime = task.endMins
                            // we found bigger num of simultaneous task
                            if (cluster.maxTaskSimultaneously < max) {
                                cluster.maxTaskSimultaneously = max
                            }
                            max = 1
                        }
                    }
                    if (max > cluster.maxTaskSimultaneously) {
                        cluster.maxTaskSimultaneously = max
                    }
                }
            }

            data class HeapItem(
                val index: Int,
                val endMins: Int,
            )

            // assigning column no to each Task
            clusterMap.forEach { entry: Map.Entry<Int, MutableList<Cluster>> ->

                for (cluster in entry.value) {
                    if (cluster.maxTaskSimultaneously == 1) {
                        for (task in cluster.list) {
                            task.column = 0
                        }
                    } else {

                        val heap = PriorityQueue<HeapItem>(
                            /* initialCapacity = */ cluster.maxTaskSimultaneously,
                            /* comparator = */compareBy { it.endMins }
                        )

                        var columnCount = -1

                        for (task in cluster.list) {
                            var reusableColumn: HeapItem? = null

                            if (heap.isNotEmpty() && heap.peek()!!.endMins <= task.startMins) {
                                reusableColumn = heap.poll()
                            }

                            if (reusableColumn == null) {
                                columnCount += 1
                            }

                            val column =
                                reusableColumn?.copy(endMins = task.endMins) ?: HeapItem(
                                    index = columnCount, endMins = task.endMins
                                )

                            heap.add(column)

                            task.column = column.index
                            // TODO debugging
                            require(column.index in 0..<cluster.maxTaskSimultaneously) {
                                "column index must be less than maxTaskSimultaneously, " +
                                        "columnIndex=${column.index}, maxTaskSimultaneously=${cluster.maxTaskSimultaneously}"
                            }

                        }
                    }

                }
            }

            clusterMap
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = mapOf()
        )


    private companion object {
        private const val TIMELINE_UI_STATE_KEY = "TIMELINE_UI_STATE_KEY"
        private const val TASK_SCREEN_UI_STATE_KEY = "TASK_SCREEN_UI_STATE_KEY"
        private const val TIMEOUT_MILLIS = 5_000L
    }

}

@Parcelize
data class TimelineUiState(
    val scale: Float = 1.5f,
    val scrollValue: Int = 0,
    val selectedChipIDs: Set<Int> = emptySet(),
) : Parcelable

@Parcelize
data class TaskScreenUiState(
    val query: String = "",
    val expanded: Boolean = false,
) : Parcelable


data class TaskOnGraph(
    val id: Int,
    val name: String,
    val startMins: Int,
    val endMins: Int,
    var column: Int = 0,
)

data class Cluster(
    val list: MutableList<TaskOnGraph> = mutableListOf(),
    var biggestEndTime: Int = 0,
    var maxTaskSimultaneously: Int = 1,
)
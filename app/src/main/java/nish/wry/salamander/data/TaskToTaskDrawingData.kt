package nish.wry.salamander.data

import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import nish.wry.salamander.data.Constants.MINS_IN_A_DAY
import nish.wry.salamander.data.room.task.Task
import nish.wry.salamander.di.TaskRepository
import nish.wry.salamander.ui.taskTab.main.TaskDrawingData
import nish.wry.salamander.ui.taskTab.timeline.TimelineScope.taskData
import java.util.Calendar
import java.util.PriorityQueue

// TODO LocalDate.ofEpochDay()
class TaskToTaskDrawingData private constructor(private val repository: TaskRepository) {
    companion object {
        @Volatile
        private var instance: TaskToTaskDrawingData? = null

        fun getInstance(repository: TaskRepository): TaskToTaskDrawingData {
            return instance ?: synchronized(this) {
                instance ?: TaskToTaskDrawingData(repository).also { instance = it }
            }
        }
    }

    /**days are saved with respect to today,
     * ie at key=0 its today, at 1 its tomorrow, -1 is yesterday**/
    operator fun get(key: Int): Flow<List<TaskDrawingData>> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, key)

        val listOfTask: Flow<List<Task>>

        if (key in 0..1) {
            listOfTask = repository.getTaskForDayIncludingOffsetTask(cal)
        } else {
            val mask = Week.calenderToWeekEnum(cal).mask
            listOfTask = repository.getTaskForDay(mask, cal)
        }

        return flow {
            listOfTask.collect { collectedTasks ->
                val rawTask = convertTasksToRawTask(
                    taskList = collectedTasks,
                    givenCal = cal,
                    nextDay = key == 1
                )
                val clusters = splitRawTasksIntoClusters(rawTask)
                indexEachTaskInACLuster(clusters)
                val res = generateTaskDrawingData(clusters)
                emit(res)
            }
        }

    }

    /** we transform [Task] into [RawTask]
     * so that it is easier to work with as everything now has a time it starts, ends... **/
    private fun convertTasksToRawTask(
        taskList: List<Task>,
        givenCal: Calendar,
        nextDay: Boolean,
    ): MutableList<RawTask> {
        val currentCal = Calendar.getInstance()
        val curMins = currentCal[Calendar.MINUTE]
        val curHoursInMins = currentCal[Calendar.HOUR_OF_DAY] * 60
        val currentTimeInMins = curHoursInMins + curMins


        val week = Week.calenderDayToWeekEnum(givenCal[Calendar.DAY_OF_WEEK])
        val data = mutableListOf<RawTask>()

        taskList.forEach { task: Task ->
            // repeating task get caught first
            val mins = task.dateTime?.get(Calendar.MINUTE)
            val hrs = task.dateTime?.get(Calendar.HOUR_OF_DAY)
            val startMins = mins?.let { (hrs?.times(60))?.plus(it) }

            if (task.repeatOnDaysBitFlag and week > 0) {
                requireNotNull(task.dateTime)
                data.add(
                    RawTask(
                        id = task.id,
                        name = task.name,
                        startMins = startMins!!,
                        endMins = startMins + Constants.TASK_DURATION,
                        chipId = task.chipId
                    )
                )
            } else {
                if (task.dateTime != null) {
                    data.add(
                        RawTask(
                            id = task.id,
                            name = task.name,
                            startMins = startMins!!,
                            endMins = startMins + Constants.TASK_DURATION,
                            chipId = task.chipId
                        )
                    )
                } else {
                    require(task.offsetHours != null) { "both dateTime and offsetHours null for task id = ${task.id}, task = $task" }
                    var effectiveStartMins = (task.offsetHours * 60) + currentTimeInMins
                    if (nextDay) {
                        val remainingTimeOfToday = MINS_IN_A_DAY - (curHoursInMins + curMins)
                        effectiveStartMins -= remainingTimeOfToday
                    }
                    if (effectiveStartMins < MINS_IN_A_DAY) {
                        data.add(
                            RawTask(
                                id = task.id,
                                name = task.name,
                                startMins = effectiveStartMins,
                                endMins = effectiveStartMins + Constants.TASK_DURATION,
                                chipId = task.chipId
                            )
                        )
                    }
                }
            }
        }

        return data
    }

    // we make clusters using first map (safe with variable task end Times)
    private fun splitRawTasksIntoClusters(
        rawTaskList: MutableList<RawTask>,
    ): MutableList<Cluster> {
        // sort by starting time
        rawTaskList.sortBy { it.startMins }
        val clusterList = mutableListOf(Cluster())
        var lastClusterIndex = 0

        rawTaskList.forEach { rawTask: RawTask ->

            var lastCluster = clusterList[lastClusterIndex]

            // cluster empty or cluster's biggestEndTime is small
            if (lastCluster.list.isEmpty() || lastCluster.biggestEndTime > rawTask.startMins) {
                lastCluster.list.add(rawTask)
                if (lastCluster.biggestEndTime < rawTask.endMins) {
                    lastCluster.biggestEndTime = rawTask.endMins
                }
            } else {
                lastClusterIndex += 1
                clusterList.add(Cluster())
                lastCluster = clusterList[lastClusterIndex]
                lastCluster.list.add(rawTask)
                lastCluster.biggestEndTime = rawTask.endMins
            }
        }

        return clusterList
    }

    private fun indexEachTaskInACLuster(
        clusterList: List<Cluster>,
    ) {
        // assigning column no to each Task and calculate max no of clusters
        for (cluster in clusterList) {
            // if it has only offset task and those are not for today then this will be empty
            if (cluster.list.isEmpty()) continue

            // the task might be in a solo cluster
            var max = 1
            val heap = PriorityQueue<HeapItem>(
                /* initialCapacity = */ cluster.list.size,
                /* comparator = */ compareBy { it.endMins }
            )
            // this is safe as heap top will always be null on the first iteration
            var columnCount = -1

            cluster.list.forEach { rawTask: RawTask ->

                var reusableColumn: HeapItem? = null
                // see if the top of the min heap has a task that's over
                if (heap.isNotEmpty() && heap.peek()!!.endMins <= rawTask.startMins) {
                    reusableColumn = heap.poll()
                }
                if (reusableColumn == null) {
                    columnCount += 1
                }
                val heapItem = reusableColumn?.copy(endMins = rawTask.endMins) ?: HeapItem(
                    index = columnCount,
                    endMins = rawTask.endMins
                )
                heap.add(heapItem)
                rawTask.column = heapItem.index
                if (heap.size > max) {
                    max = heap.size
                }

            }

            cluster.maxTaskSimultaneously = max
        }
    }

    private fun generateTaskDrawingData(
        clusterList: List<Cluster>,
    ): List<TaskDrawingData> {
        val taskDrawingList: MutableList<TaskDrawingData> = mutableListOf()

        clusterList.forEach { cluster: Cluster ->
            cluster.list.forEach { rawTask: RawTask ->
                val modifier = Modifier.taskData(
                    index = rawTask.column,
                    maxSimultaneous = cluster.maxTaskSimultaneously,
                    startMins = rawTask.startMins,
                    endMins = rawTask.endMins
                )
                val taskDrawingData = TaskDrawingData(
                    id = rawTask.id,
                    chipId = rawTask.chipId,
                    name = rawTask.name,
                    modifier = modifier
                )
                taskDrawingList.add(taskDrawingData)
            }
        }

        return taskDrawingList
    }
}

private data class RawTask(
    val id: Int,
    val name: String,
    val startMins: Int,
    val endMins: Int,
    val chipId: Int,
    var column: Int = 0,
)

private data class Cluster(
    val list: MutableList<RawTask> = mutableListOf(),
    var biggestEndTime: Int = 0,
    var maxTaskSimultaneously: Int = 1,
)

private data class HeapItem(
    val index: Int,
    val endMins: Int,
)
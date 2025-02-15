package nish.wry.salamander.data

import androidx.compose.ui.Modifier
import nish.wry.salamander.data.room.Task
import nish.wry.salamander.ui.task.TaskDrawingData
import nish.wry.salamander.ui.task.TimelineScope.taskData
import java.util.Calendar
import java.util.PriorityQueue

object TaskToTaskDrawingData {
    private const val MINS_IN_A_DAY = 1440
    operator fun invoke(listOfTask: List<Task>) : Map<Int, List<TaskDrawingData>>{
        val rawTask = convertTasksToRawTask(listOfTask)
        val clusters = splitRawTasksIntoClusters(rawTask)
        countNoOfMaxSimultaneousTask(clusters)
        indexEachTaskInACLuster(clusters)
        return generateTaskDrawingData(clusters)
    }
    /** we transform [Task] into [RawTask]
     * so that it is easier to work with as everything now has a time it starts, ends... **/
    private fun convertTasksToRawTask(taskList: List<Task>): Map<Int, MutableList<RawTask>> {
        val cal: Calendar = Calendar.getInstance()
        val tomorrowCal = cal.clone() as Calendar

        tomorrowCal.add(Calendar.DAY_OF_YEAR, 1)

        val currentTimeInMins = (cal[Calendar.HOUR_OF_DAY] * 60) + cal[Calendar.MINUTE]

        val todayCalendarInt = cal[Calendar.DAY_OF_WEEK]

        val todayWeek = Week.calenderDayToWeekEnum(todayCalendarInt)
        val tomorrowWeek =
            Week.calenderDayToWeekEnum(Week.giveNextValidWeekdayNumber(todayCalendarInt))

        val map = mutableMapOf<Int, MutableList<RawTask>>()
        val valueTransform: (task: Task) -> RawTask = { task: Task ->
            val minutes: Int
            if (task.dateTime != null) {
                val dateTime = task.dateTime
                minutes = (dateTime[Calendar.HOUR_OF_DAY] * 60) + dateTime[Calendar.MINUTE]
            } else {
                require(task.offsetHours != null) { "both dateTime and offsetHours null for task id = ${task.id}, task = $task" }
                minutes =
                    if (task.offsetHours * 60 + currentTimeInMins < MINS_IN_A_DAY) currentTimeInMins + task.offsetHours * 60
                    else
                    // subtract from offset the remaining hours of today (its being added to tomorrow)
                        task.offsetHours * 60 - (MINS_IN_A_DAY - currentTimeInMins)
            }

            RawTask(
                id = task.id,
                name = task.name,
                startMins = minutes,
                endMins = minutes + Constants.TASK_DURATION,
                chipId = task.taskChipId
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
                        if (elem.offsetHours * 60 + currentTimeInMins < MINS_IN_A_DAY) {
                            cal[Calendar.DAY_OF_YEAR]
                        } else {
                            tomorrowCal[Calendar.DAY_OF_YEAR]
                        }
                    ) { ArrayList() }
                    list.add(valueTransform(elem))
                }
            }

        }
        return map
    }

    // we make clusters using first map (safe with variable task end Times)
    private fun splitRawTasksIntoClusters(map: Map<Int, MutableList<RawTask>>): Map<Int, MutableList<Cluster>> {
        val clusterMap = map.mapValues { entry: Map.Entry<Int, MutableList<RawTask>> ->
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

        return clusterMap
    }

    private fun countNoOfMaxSimultaneousTask(clusterMap: Map<Int, List<Cluster>>){
        // counting max no of simultaneous task for each cluster
        // (not safe with variable end time), works for const task lengths
        clusterMap.forEach { entry: Map.Entry<Int, List<Cluster>> ->
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
    }

    private fun indexEachTaskInACLuster(clusterMap: Map<Int, List<Cluster>>){
        // assigning column no to each Task
        clusterMap.forEach { entry: Map.Entry<Int, List<Cluster>> ->
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
                        require(column.index in 0..<cluster.maxTaskSimultaneously) {
                            "column index must be less than maxTaskSimultaneously, " +
                                    "columnIndex=${column.index}, maxTaskSimultaneously=${cluster.maxTaskSimultaneously}"
                        }

                    }
                }

            }
        }
    }

    private fun generateTaskDrawingData(clusterMap: Map<Int, MutableList<Cluster>>): Map<Int, List<TaskDrawingData>> {
        val finalMap = mutableMapOf<Int, MutableList<TaskDrawingData>>()
        clusterMap.forEach { entry: Map.Entry<Int, MutableList<Cluster>> ->
            entry.value.forEach { cluster: Cluster ->
                cluster.list.forEach { taskOnGraph ->
                    val modifier = Modifier.taskData(
                        index = taskOnGraph.column,
                        maxSimultaneous = cluster.maxTaskSimultaneously,
                        startMins = taskOnGraph.startMins,
                        endMins = taskOnGraph.endMins
                    )
                    val taskDrawingData = TaskDrawingData(
                        id = taskOnGraph.id,
                        chipId = taskOnGraph.chipId,
                        name = taskOnGraph.name,
                        modifier = modifier
                    )

                    if (finalMap[entry.key] == null)
                        finalMap[entry.key] = mutableListOf(taskDrawingData)
                    else
                        finalMap[entry.key]?.add(taskDrawingData)
                }
            }
        }
        return finalMap
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
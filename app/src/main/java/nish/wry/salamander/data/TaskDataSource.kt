package nish.wry.salamander.data

import kotlinx.coroutines.flow.Flow
import nish.wry.salamander.ui.taskTab.main.TaskDrawingData

// TODO
//  1. next we implement a prefetch and stuff
//  2. invalidate data when dates change(broadcast receiver)? or maybe just increment each key by 1, but really
//      invalidate for key 0 & 1 cuz offset hrs

class TaskDataSource private constructor(
    private val taskDrawingData: TaskToTaskDrawingData,
) {

    private val _state = mutableMapOf<Int, Flow<List<TaskDrawingData>>>()

    private fun fetch(key: Int): Flow<List<TaskDrawingData>> {
        return _state.getOrPut(key) { taskDrawingData[key] }
    }

    /**
     * we save days relative to today
     * day=0 is today, day=1 is tomorrow and day=-1 is yesterday
     * **/
    operator fun get(key: Int): Flow<List<TaskDrawingData>> {
        return _state[key] ?: fetch(key)
    }

    companion object {
        @Volatile
        private var instance: TaskDataSource? = null

        fun getInstance(taskDrawingData: TaskToTaskDrawingData): TaskDataSource {
            return instance ?: synchronized(this) {
                instance ?: TaskDataSource(taskDrawingData).also { instance = it }
            }
        }
    }

}
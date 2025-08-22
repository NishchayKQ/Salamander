package nish.wry.salamander.data

enum class Priority(val id: Int) {
    /**
     * low priority task with no notifications
     * **/
    Low(0),

    /**
     * normal priority task with notifications
     * **/
    Normal(1),

    /**
     * high priority task with full screen dismissable notification
     * **/
    Critical(2);

    companion object {
        /**
         * Convert a valid priority Int to [Priority], throws [IllegalArgumentException] if id doesn't belong to any valid Priority
         * **/
        fun priorityById(id: Int): Priority {
            return when (id) {
                0 -> Low
                1 -> Normal
                2 -> Critical
                else -> {
                    throw IllegalArgumentException("ID accepted are 0 to 2, got $id")
                }
            }
        }
    }
}
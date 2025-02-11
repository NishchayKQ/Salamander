package nish.wry.salamander.data

enum class Priority(val id: Int) {
    Low(0),
    Normal(1), // notification is enough
    Critical(2);

    companion object {
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
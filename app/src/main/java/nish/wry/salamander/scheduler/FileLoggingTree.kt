package nish.wry.salamander.scheduler

import android.content.Context
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * for logging stuff to a file in debug file, rn alarms & schedulers I'm logging**/
class FileLoggingTree(private val context: Context) : Timber.DebugTree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        try {
//            if (tag != Constants.TIMBER_LOG) return

            val fileName = "salamander_logs.txt"
            val file = File(context.getExternalFilesDir(null), fileName)

            val timestamp = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault()).format(Date())

            val logMessage = "$timestamp [$tag] $message\n"


            file.appendText(logMessage)
        } catch (_: Exception) {
            // Can't really log this failure to a file!
        }
    }
}
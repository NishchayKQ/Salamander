package nish.wry.salamander

import android.app.NotificationManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import nish.wry.salamander.data.Constants
import nish.wry.salamander.ui.theme.SalamanderTheme

@AndroidEntryPoint
class ReminderActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Essential for lockscreen wake up
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }


        val notificationId: Int = intent.getIntExtra(Constants.EXTRA_TASK_ID, -1)
        val stopAlarm = {
            if (notificationId != -1) {
                val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                nm.cancel(notificationId)
            }
        }

        setContent {
            SalamanderTheme {
                ReminderScreen(
                    taskName = intent.getStringExtra(Constants.EXTRA_TASK_NAME) ?: "Task",
                    onDismiss = {
                        stopAlarm()
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun ReminderScreen(taskName: String, onDismiss: () -> Unit) {
    val gradientBrush = Brush.linearGradient(
        // Analogous colors mirroring the dark-mode reminder style
        colors = listOf(
            Color(0xFF2C1B4D), // Soft Deep Purple (Top-Left)
            Color(0xFF120B24)  // Very Dark Indigo/Charcoal (Bottom-Right)
        ),
        start = Offset(0f, 0f),               // Top-Left corner
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY) // Bottom-Right corner
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradientBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = taskName,
                style = MaterialTheme.typography.displayMedium,
                textAlign = TextAlign.Center,
                color = Color.White,
            )

            Button(onClick = onDismiss, modifier = Modifier.padding(top = 40.dp)) {
                Text("Dismiss")
            }

        }
    }
}

@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ReminderScreenPreview() {
    ReminderScreen("Reminder 1") {

    }
}

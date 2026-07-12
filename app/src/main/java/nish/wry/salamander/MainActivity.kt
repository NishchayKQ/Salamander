package nish.wry.salamander

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import nish.wry.salamander.data.DateTimeTracker
import nish.wry.salamander.ui.SalamanderApp
import nish.wry.salamander.ui.theme.SalamanderTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var dateTimeTracker: DateTimeTracker

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val windowWidthSizeClass = calculateWindowSizeClass(this).widthSizeClass
            SalamanderTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SalamanderApp(windowSizeClass = windowWidthSizeClass)
                }
            }
        }
    }

    override fun onRestart() {
        super.onRestart()
        dateTimeTracker.refresh()
    }

    override fun onStop() {
        super.onStop()
        dateTimeTracker.stop()
    }
}



package kr.entropi.tasker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.android.AndroidEntryPoint
import kr.entropi.tasker.navigation.LocalNavHost
import kr.entropi.tasker.ui.navigation.Navigator
import kr.entropi.tasker.ui.theme.TaskerTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaskerTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    CompositionLocalProvider(LocalContext provides this) {
                        LocalNavHost { content ->
                            Navigator { content() }
                        }
                    }
                }
            }
        }
    }
}
package kr.entropi.tasker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kr.entropi.tasker.navigation.LocalNavController

@Composable
fun BackButton(modifier: Modifier = Modifier) {
    val navController = LocalNavController.current
    IconButton({ navController.popBackStack() }) {
        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
    }
}
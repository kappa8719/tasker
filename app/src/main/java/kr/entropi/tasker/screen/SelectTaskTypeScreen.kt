package kr.entropi.tasker.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kr.entropi.tasker.R
import kr.entropi.tasker.navigation.CreateTaskEntry
import kr.entropi.tasker.navigation.CreateTaskType
import kr.entropi.tasker.navigation.LocalNavController
import kr.entropi.tasker.ui.navigation.BackButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectTaskTypeScreen() {
    val navController = LocalNavController.current

    Scaffold(topBar = {
        TopAppBar(
            modifier = Modifier.shadow(1.dp),
            title = { Text(stringResource(R.string.tasks_create_select_type)) },
            navigationIcon = {
                BackButton()
            }
        )
    }) { padding ->
        Row(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(8.dp)
        ) {
            ElevatedButton(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp),
                onClick = {
                    navController.navigate(CreateTaskEntry(type = CreateTaskType.RemoteExecution))
                }
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Outlined.PlayArrow, contentDescription = null)
                    Text(stringResource(R.string.tasks_type_remote_execution))
                }
            }
        }
    }
}
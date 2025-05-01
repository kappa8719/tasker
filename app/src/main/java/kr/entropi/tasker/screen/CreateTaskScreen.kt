package kr.entropi.tasker.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kr.entropi.tasker.R
import kr.entropi.tasker.navigation.CreateTaskType
import kr.entropi.tasker.ui.navigation.BackButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(type: CreateTaskType) {
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.shadow(1.dp),
                title = {
                    Text(
                        stringResource(
                            R.string.tasks_create_type,
                            stringResource(type.displayNameId)
                        )
                    )
                },
                navigationIcon = {
                    BackButton()
                }
            )
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

        }
    }
}
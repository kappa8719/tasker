package kr.entropi.tasker.screen.machine

import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.byValue
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.entropi.tasker.machine.MachinesViewModel
import kr.entropi.tasker.machine.ShellMachine
import kr.entropi.tasker.navigation.LocalNavController
import kr.entropi.tasker.navigation.MachineListRoute
import kr.entropi.tasker.ui.navigation.BackButton
import rememberRequestPermission
import java.net.IDN
import java.net.InetAddress
import java.net.SocketException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class AddMachineValues(
    val alias: String = "",
    val host: String = "",
    val port: String = "22",
    val user: String = "",
    val password: String = ""
) {
    val isAliasValid get() = alias.isNotBlank()
    val isHostValid
        get() = isValidHost(host)
    val isUserValid get() = user.isNotBlank()

    val parsedPort get() = port.toUShortOrNull() ?: 22u

    val isFormValid get() = isAliasValid && isHostValid && isUserValid

    private fun isValidHost(input: String): Boolean {
        // Check for empty or too long
        if (input.isEmpty() || input.length > 253) return false

        // IPv4 regex
        val ipv4Regex = Regex("""^((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.|$)){4}$""")
        if (ipv4Regex.matches(input)) return true

        // Try IPv6 validation
        try {
            val addr = InetAddress.getByName(input)
            if (addr.hostAddress?.contains(":") == true) return true // IPv6
        } catch (e: Exception) {
            // Not an IP address
        }

        // Hostname validation
        val hostnameRegex =
            Regex("""^(?=.{1,253}$)(?!-)[A-Za-z0-9-]{1,63}(?<!-)(\.(?!-)[A-Za-z0-9-]{1,63}(?<!-))*$""")
        // Convert to ASCII for IDN (internationalized domain names)
        val asciiInput = try {
            IDN.toASCII(input)
        } catch (e: Exception) {
            return false
        }
        return hostnameRegex.matches(asciiInput)
    }

    fun testConnection(timeout: Duration = 5.seconds): TestConnectionResult {
        try {
            val jsch = JSch()
            val session = jsch.getSession(user, host, parsedPort.toInt())
            session.setPassword(password)
            session.setConfig("StrictHostKeyChecking", "no")

            session.connect(timeout.inWholeMilliseconds.toInt())
        } catch (e: Throwable) {
            return when (e) {
                is SocketException -> TestConnectionResult.FailedSocket(e)
                is JSchException -> TestConnectionResult.FailedSSH(e)
                else -> TestConnectionResult.FailedUnknown(e)
            }
        }

        return TestConnectionResult.Success
    }

    fun toShellMachine(): ShellMachine {
        return ShellMachine(
            alias = alias,
            host = host,
            port = parsedPort,
            user = user,
            password = password
        )
    }

    sealed interface TestConnectionResult {
        data object FailedPermission : TestConnectionResult
        data class FailedSocket(val exception: SocketException) : TestConnectionResult
        data class FailedSSH(val exception: JSchException) : TestConnectionResult
        data class FailedUnknown(val throwable: Throwable) : TestConnectionResult
        data object Success : TestConnectionResult
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMachineScreen() {
    val machinesViewModel = hiltViewModel<MachinesViewModel>()
    var formState by remember { mutableStateOf(AddMachineValues()) }
    val navController = LocalNavController.current

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.shadow(1.dp),
                title = {
                    Text("머신 추가")
                },
                navigationIcon = {
                    BackButton()
                }
            )
        },
        bottomBar = {
            HorizontalDivider()
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp, 8.dp, 8.dp, 20.dp), horizontalArrangement = Arrangement.End
            ) {
                Button({
                    if (formState.isFormValid) {
                        machinesViewModel.machines += formState.toShellMachine()
                        navController.popBackStack()
                    }
                }) {
                    Text("Add machine")
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            AddMachineForm(formState) { formState = it }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMachineForm(
    values: AddMachineValues,
    onValuesChange: (AddMachineValues) -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            values.alias,
            { onValuesChange(values.copy(alias = it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Alias") },
            singleLine = true,
            isError = values.alias.isBlank(),
            supportingText = {
                AnimatedVisibility(
                    values.alias.isBlank(),
                    enter = expandVertically(tween()),
                    exit = shrinkVertically(
                        tween()
                    )
                ) {
                    Text("Alias must be not blank")
                }
            }
        )
        Row(Modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                values.host,
                { onValuesChange(values.copy(host = it)) },
                modifier = Modifier.weight(2f),
                label = { Text("Host") },
                singleLine = true,
                isError = !values.isHostValid,
                supportingText = {
                    AnimatedVisibility(
                        !values.isHostValid,
                        enter = expandVertically(tween()),
                        exit = shrinkVertically(
                            tween()
                        )
                    ) {
                        Text("Failed to parse host")
                    }
                }
            )

            val portFieldState = rememberTextFieldState()
            LaunchedEffect(portFieldState.text) {
                onValuesChange(values.copy(port = portFieldState.text.toString()))
            }

            OutlinedTextField(
                state = portFieldState,
                modifier = Modifier.weight(1f),
                label = { Text("Port") },
                lineLimits = TextFieldLineLimits.SingleLine,
                inputTransformation = InputTransformation.byValue { current, proposed ->
                    if (proposed.isNotEmpty() && proposed.toString().toUShortOrNull() == null) {
                        current
                    } else {
                        proposed
                    }
                },
                isError = values.port.toUShortOrNull() == null,
                supportingText = {
                    AnimatedVisibility(
                        values.port.toUShortOrNull() == null,
                        enter = expandVertically(tween()),
                        exit = shrinkVertically(
                            tween()
                        )
                    ) {
                        Text("The port must be unsigned int")
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        HorizontalDivider()
        Text("Identity")
        OutlinedTextField(
            values.user,
            { onValuesChange(values.copy(user = it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("User") },
            singleLine = true,
            isError = values.user.isBlank(),
            supportingText = {
                AnimatedVisibility(
                    values.user.isBlank(),
                    enter = expandVertically(tween()),
                    exit = shrinkVertically(
                        tween()
                    )
                ) {
                    Text("User must be not blank")
                }
            }
        )
        OutlinedTextField(
            values.password,
            { onValuesChange(values.copy(password = it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        TestConnectionButton(values, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun TestConnectionButton(values: AddMachineValues, modifier: Modifier = Modifier) {
    var isTestingConnection by remember { mutableStateOf(false) }
    var isTestResultDialogVisible by remember { mutableStateOf(false) }
    var testResult by remember {
        mutableStateOf<AddMachineValues.TestConnectionResult?>(
            null
        )
    }
    val requestPermission = rememberRequestPermission()
    val coroutineScope = rememberCoroutineScope { Dispatchers.IO }

    if (isTestResultDialogVisible && testResult != null) {
        testResult!!

        AlertDialog(
            onDismissRequest = { isTestResultDialogVisible = false },
            confirmButton = {
                Button({ isTestResultDialogVisible = false }) {
                    Text("Confirm")
                }
            },
            title = {
                Text("${testResult!!::class.simpleName}")
            },
            icon = {
                Icon(
                    if (testResult is AddMachineValues.TestConnectionResult.Success) Icons.Outlined.CheckCircle else Icons.Outlined.Error,
                    null
                )
            },
            text = {
                Text("$testResult")
            }
        )
    }

    OutlinedButton(
        modifier = modifier,
        onClick = {
            if (isTestingConnection) return@OutlinedButton

            coroutineScope.launch {
                isTestingConnection = true

                if (!requestPermission.request(Manifest.permission.INTERNET)) {
                    testResult = AddMachineValues.TestConnectionResult.FailedPermission
                    isTestResultDialogVisible = true
                    isTestingConnection = false
                    return@launch
                }
                testResult = values.testConnection()
                isTestResultDialogVisible = true

                isTestingConnection = false
            }
        },
        enabled = !isTestingConnection
    ) {
        if (isTestingConnection) {
            Text("Testing...")
        } else {
            Text("Test Connection")
        }
    }

    AnimatedVisibility(
        testResult is AddMachineValues.TestConnectionResult.Success,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Text("Last test to the machine succeeded", color = Color.Gray)
    }
}
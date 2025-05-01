import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume

@Composable
fun rememberRequestPermission(): PermissionRequester {
    val context = LocalContext.current
    var continuation by remember { mutableStateOf<CancellableContinuation<Boolean>?>(null) }
    val mutex = remember { Mutex() }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        continuation?.resume(isGranted)
        continuation = null
    }

    fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context, permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    return remember {
        object : PermissionRequester {
            override suspend fun request(permission: String): Boolean {
                if (checkPermission(permission)) return true

                return mutex.withLock {
                    try {
                        suspendCancellableCoroutine { cont ->
                            continuation?.cancel(
                                CancellationException("New permission request started")
                            )
                            continuation = cont
                            launcher.launch(permission)
                        }
                    } finally {
                        continuation = null
                    }
                }
            }
        }
    }
}

interface PermissionRequester {
    /**
     * Suspends until permission request is completed
     * @return true if granted, false if denied
     */
    suspend fun request(permission: String): Boolean
}

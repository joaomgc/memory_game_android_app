package projeto_taes.projeto.notifications

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import projeto_taes.projeto.notifications.NotificationHelper

@Composable
fun NotificationDialog(onDismissRequest: () -> Unit, notificationHelper: NotificationHelper) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Notification Test") },
        text = { Text("Press 'Send' to test sending a system notification.") },
        confirmButton = {
            TextButton(
                onClick = {
                    //notificationHelper.sendNotification("Test Notification from MyApp")
                    onDismissRequest()
                }
            ) {
                Text("Send")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Dismiss")
            }
        }
    )
}
package projeto_taes.projeto.notifications

import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import projeto_taes.projeto.App

@Composable
fun NotificationIcon(onClick: () -> Unit) {
    if (App.TESTING) {
        // For easy tests
        TextButton(onClick = onClick) {
            Text("Bell")
        }
    } else {
        IconButton(onClick = onClick) {
            Icon(Icons.Filled.Notifications, contentDescription = "Open notifications")
        }
    }
}

@Composable
fun NotificationList(notifications: List<NotificationItem>, isVisible: Boolean, onClose: () -> Unit) {
    if (isVisible) {
        Dialog(onDismissRequest = onClose) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = onClose, indication = null, interactionSource = remember { MutableInteractionSource() })
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .padding(vertical = 32.dp)
                        .background(Color.White, shape = RoundedCornerShape(8.dp))
                ) {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.Close, contentDescription = "Close notifications")
                    }

                    if (notifications.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            itemsIndexed(notifications) { index, notification ->
                                NotificationView(notification = notification)
                                if (index < notifications.size - 1) {
                                    HorizontalDivider()
                                }
                            }
                        }
                    } else {
                        Text(
                            "No notifications available",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun NotificationView(notification: NotificationItem) {
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { showDialog = true }
    ) {
        Text(text = notification.title, style = MaterialTheme.typography.titleMedium)
        Text(text = notification.content, style = MaterialTheme.typography.bodyMedium)
    }

    if (showDialog) {
        NotificationDetailDialog(notification = notification, onDismissRequest = { showDialog = false })
    }
}

@Composable
fun NotificationDetailDialog(notification: NotificationItem, onDismissRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(notification.title, style = MaterialTheme.typography.headlineSmall) },
        text = { Text(notification.content, style = MaterialTheme.typography.bodyLarge) },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Close")
            }
        }
    )
}
package projeto_taes.projeto.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import projeto_taes.projeto.App
import projeto_taes.projeto.R

class NotificationHelper(private val context: Context) {
    private val notifications: SnapshotStateList<NotificationItem> = mutableStateListOf()

    companion object {
        private const val CHANNEL_ID = "game_notification_channel"
        private const val NOTIFICATION_ID = 1
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.channel_name)
            val descriptionText = context.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendSystemNotification(content: String, notificationId: Int) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Memory Game Alert")
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        val notification = builder.build()
        with(NotificationManagerCompat.from(context)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    notify(notificationId, notification)
                }
            } else {
                notify(notificationId, notification)
            }
        }
    }

    fun sendNotification(title: String, content: String, system: Boolean = true) {
        val newNotification = NotificationItem(
            id = notifications.size + 1,
            title = title,
            content = content
        )
        notifications.add(newNotification)
        if (system && !App.TESTING) {
            sendSystemNotification(newNotification.content, newNotification.id)
        }
    }

    fun getNotifications(): SnapshotStateList<NotificationItem> {
        return notifications
    }
}
package com.example.rlock.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.rlock.R
import com.example.rlock.model.AgendaBlock
import java.time.format.DateTimeFormatter
import android.annotation.SuppressLint

object RLockNotificationManager {

    private const val CHANNEL_ID = "rlock_active_agenda"
    private const val CHANNEL_NAME = "Active Agenda Block"

    fun createNotificationChannel(context: Context) {
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
            description = "Persistent notification for current agenda block"
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    @SuppressLint("MissingPermission")
    fun updateBlockNotification(context: Context, block: AgendaBlock) {
        val notificationManager = NotificationManagerCompat.from(context)
        val notificationId = block.id.hashCode()

        val allCompleted = block.subtasks.isNotEmpty() && block.subtasks.all { it.isCompleted }

        if (allCompleted || !block.isNotificationEnabled) {
            notificationManager.cancel(notificationId)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val completedCount = block.subtasks.count { it.isCompleted }
        val totalCount = block.subtasks.size
        
        val formatter = DateTimeFormatter.ofPattern("h:mm a")
        val startTime = block.startTime.format(formatter)
        val endTime = block.endTime.format(formatter)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("${block.title} ($startTime - $endTime)")
            .setContentText("Tasks completed: $completedCount/$totalCount")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}

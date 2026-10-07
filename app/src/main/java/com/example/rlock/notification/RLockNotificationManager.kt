package com.example.rlock.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.rlock.MainActivity
import com.example.rlock.R
import com.example.rlock.model.AgendaBlock
import com.example.rlock.model.CustomCategory
import java.time.format.DateTimeFormatter

object RLockNotificationManager {

    private const val CHANNEL_ID = "rlock_active_agenda"
    private const val CHANNEL_NAME = "Active Agenda Block"

    fun createNotificationChannel(context: Context) {
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
            description = "Persistent notification for current agenda block"
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    @SuppressLint("MissingPermission")
    fun updateBlockNotification(
        context: Context,
        block: AgendaBlock,
        categories: List<CustomCategory> = emptyList()
    ) {
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

        val category = categories.find { it.id == block.categoryId || it.name.equals(block.category, ignoreCase = true) }
            ?: CustomCategory.defaultCategories.find { it.id == block.categoryId || it.name.equals(block.category, ignoreCase = true) }

        val isAppt = block.categoryId.contains("appointment", ignoreCase = true) || block.categoryName.equals("Appointment", ignoreCase = true)
        val rawEmoji = category?.emoji ?: if (isAppt) "📅" else "📌"
        val categoryEmoji = if (rawEmoji.endsWith(" ")) rawEmoji else "$rawEmoji "
        val categoryName = category?.name ?: block.categoryName.ifBlank { if (isAppt) "Appointment" else "General" }

        val formatter = DateTimeFormatter.ofPattern("h:mm a")
        val startTime = block.startTime.format(formatter)
        val endTime = block.endTime.format(formatter)

        val title = "${categoryEmoji}${categoryName} • $startTime -$endTime"

        val completedCount = block.subtasks.count { it.isCompleted }
        val totalCount = block.subtasks.size
        val remainingSubtasks = block.subtasks.filter { !it.isCompleted }
        val nextSubtask = remainingSubtasks.firstOrNull()

        val summaryOrProgress = if (nextSubtask != null) {
            "Next: ${nextSubtask.name} ($completedCount/$totalCount completed)"
        } else if (totalCount > 0) {
            "$completedCount/$totalCount subtasks completed"
        } else {
            block.title.ifBlank { categoryName }
        }

        val bigText = if (remainingSubtasks.isNotEmpty()) {
            remainingSubtasks.joinToString("\n") { "☐ ${it.name}" }
        } else {
            summaryOrProgress
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_NAVIGATE_TO", "AGENDA")
            putExtra("EXTRA_BLOCK_ID", block.id)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val bigTextStyle = NotificationCompat.BigTextStyle()
            .setBigContentTitle(title)
            .setSummaryText(if (totalCount > 0) "$completedCount/$totalCount completed" else "Active Agenda")
            .bigText(bigText)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(summaryOrProgress)
            .setStyle(bigTextStyle)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}

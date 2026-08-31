package com.example.domain.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.domain.model.FocusState

object FocusNotificationHelper {
    const val CHANNEL_ID = "focus_timer_channel"
    const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Focus Timer Live Session",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live focus session status, active goal tracking, and timer progress"
                setShowBadge(false)
                enableVibration(false)
                setSound(null, null)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun updateNotification(
        context: Context,
        focusState: FocusState,
        timerText: String,
        activeGoalDescription: String?,
        isPomodoro: Boolean,
        subtitle: String,
        remainingOrElapsedSeconds: Long
    ) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val title = when (focusState) {
                FocusState.Working -> "🔥 Focusing • $timerText"
                FocusState.Break -> "☕ Rest Break • $timerText"
                FocusState.Procrastination -> "⚠️ Slacking Alert • $timerText"
                FocusState.Prompting -> "💬 Mind Dump Prompt • $timerText"
                FocusState.Idle -> "Focus Logger"
            }

            val contentText = if (!activeGoalDescription.isNullOrBlank()) {
                "🎯 Task: $activeGoalDescription ($subtitle)"
            } else {
                subtitle
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(contentText)
                .setSubText(if (isPomodoro) "Pomodoro" else "Stopwatch")
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setUsesChronometer(true)
            
            if (isPomodoro && (focusState == FocusState.Working || focusState == FocusState.Break)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    builder.setChronometerCountDown(true)
                }
                builder.setWhen(System.currentTimeMillis() + (remainingOrElapsedSeconds * 1000L))
            } else {
                builder.setWhen(System.currentTimeMillis() - (remainingOrElapsedSeconds * 1000L))
            }

            val manager = NotificationManagerCompat.from(context)
            manager.notify(NOTIFICATION_ID, builder.build())
        } catch (_: SecurityException) {
            // Notification permission might not be granted yet
        } catch (_: Exception) {
            // Fallback gracefully
        }
    }

    fun cancelNotification(context: Context) {
        try {
            val manager = NotificationManagerCompat.from(context)
            manager.cancel(NOTIFICATION_ID)
        } catch (_: Exception) {}
    }
}

package com.example.kodiwellness.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationHelper {

    const val CHANNEL_MEDICATIONS = "kodi_medications"
    const val CHANNEL_APPOINTMENTS = "kodi_appointments"
    const val CHANNEL_HYDRATION = "kodi_hydration"
    const val CHANNEL_REMINDERS = "kodi_reminders"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val medChannel = NotificationChannel(
                CHANNEL_MEDICATIONS,
                "Medication Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical alerts for prescription and vitamin schedules"
                enableVibration(true)
            }

            val apptChannel = NotificationChannel(
                CHANNEL_APPOINTMENTS,
                "Medical Appointments",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Upcoming doctor consultations and clinic visits"
                enableVibration(true)
            }

            val hydrationChannel = NotificationChannel(
                CHANNEL_HYDRATION,
                "Hydration Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily water drinking reminders and wellness cues"
            }

            val generalChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "General Health Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Custom reminders for workouts, sleep, and medical tests"
            }

            notificationManager.createNotificationChannels(
                listOf(medChannel, apptChannel, hydrationChannel, generalChannel)
            )
        }
    }

    fun showMedicationNotification(
        context: Context,
        notificationId: Int,
        medicationName: String,
        dosage: String
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val builder = NotificationCompat.Builder(context, CHANNEL_MEDICATIONS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Medication Reminder")
            .setContentText("It is time to take your $medicationName ($dosage).")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("It is time to take your $medicationName ($dosage). Remember to take with water as instructed by your healthcare provider.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        manager.notify(notificationId, builder.build())
    }

    fun showGeneralNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
        channelId: String = CHANNEL_REMINDERS
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        manager.notify(notificationId, builder.build())
    }
}

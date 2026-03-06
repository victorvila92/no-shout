package com.example.todeveu.notif

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.todeveu.R
import com.example.todeveu.service.VoiceMonitorService
import com.example.todeveu.ui.MainActivity

object NotificationHelper {
    private const val TAG = "NotificationHelper"
    const val CHANNEL_LISTENING = "voice_monitor_listening"
    const val CHANNEL_ALERT = "voice_monitor_alert"
    const val NOTIFICATION_LISTENING_ID = 1001
    const val NOTIFICATION_SHOUT_ID = 1002
    const val ACTION_SILENCE_5MIN = "com.example.todeveu.SILENCE_5MIN"
    const val ACTION_DISMISS = "com.example.todeveu.DISMISS"
    const val EXTRA_SILENCE_UNTIL = "silence_until"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_LISTENING,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply { setShowBadge(false) }
        )
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ALERT,
                context.getString(R.string.notification_shout_title),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setShowBadge(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build())
            }
        )
    }

    fun buildListeningNotification(context: Context): NotificationCompat.Builder {
        val open = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(context, CHANNEL_LISTENING)
            .setContentTitle(context.getString(R.string.notification_listening))
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(open)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
    }

    fun showShoutNotification(
        context: Context,
        vibrationEnabled: Boolean,
        silenceUntil: Long = 0L,
    ) {
        if (silenceUntil > System.currentTimeMillis()) {
            Log.d(TAG, "showShoutNotification: ignorat (en cooldown)")
            return
        }
        Log.i(TAG, "showShoutNotification: mostrant alerta 'Baixa el to'")
        if (vibrationEnabled) {
            val v = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
            else
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            v?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    it.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                else
                    @Suppress("DEPRECATION") it.vibrate(500)
            }
        }
        val silenceIntent = PendingIntent.getService(
            context, 0,
            Intent(context, VoiceMonitorService::class.java).setAction(ACTION_SILENCE_5MIN),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val dismissIntent = PendingIntent.getService(
            context, 0,
            Intent(context, VoiceMonitorService::class.java).setAction(ACTION_DISMISS),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val open = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val n = NotificationCompat.Builder(context, CHANNEL_ALERT)
            .setContentTitle(context.getString(R.string.notification_shout_title))
            .setContentText(context.getString(R.string.notification_shout_text))
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(open)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .addAction(android.R.drawable.ic_media_pause, "Silenciar 5 min", silenceIntent)
            .addAction(android.R.drawable.ic_delete, "Aturar", dismissIntent)
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(NOTIFICATION_SHOUT_ID, n.build())
    }

    fun cancelShoutNotification(context: Context) {
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(NOTIFICATION_SHOUT_ID)
    }
}

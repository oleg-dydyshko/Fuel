package serhij.korneluk.chemlabfuel

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat

class ReceiverNotification : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        sendNotif(ctx, intent.getBooleanExtra("reaktive", false))
    }

    private fun sendNotif(context: Context, reaktive: Boolean) {
        val notificationIntent = Intent(context, MainActivity::class.java)
        notificationIntent.putExtra("notifications", true)
        notificationIntent.putExtra("reaktive", reaktive)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, flags)
        val res = context.resources
        var channelId = "2020"
        if (reaktive) channelId = "2030"
        val builder = NotificationCompat.Builder(context, channelId)
        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.ic_launcher))
                .setAutoCancel(true)
                .setContentTitle("Истекает cрок")
                .setVibrate(longArrayOf(0, 1000))
        if (reaktive) builder.setContentText("Годности реактива") else builder.setContentText("Следующей аттестации, поверки, калибровки")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId("serhij.korneluk.chemlabfuel")
        }
        val notification = builder.build()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("serhij.korneluk.chemlabfuel", "chemlabfuel", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(205, notification)
    }
}
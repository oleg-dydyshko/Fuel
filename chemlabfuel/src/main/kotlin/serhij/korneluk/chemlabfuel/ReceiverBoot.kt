package serhij.korneluk.chemlabfuel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReceiverBoot : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != null && (intent.action == "android.intent.action.BOOT_COMPLETED" || intent.action == "android.intent.action.QUICKBOOT_POWERON" || intent.action == "com.htc.intent.action.QUICKBOOT_POWERON")) {
            val i = Intent(context, ReceiverSetAlarm::class.java)
            context.sendBroadcast(i)
        }
    }
}
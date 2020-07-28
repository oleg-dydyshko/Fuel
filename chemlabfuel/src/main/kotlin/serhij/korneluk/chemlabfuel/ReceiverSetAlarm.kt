package serhij.korneluk.chemlabfuel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class ReceiverSetAlarm : BroadcastReceiver() {
    private val testData = ArrayList<String>()
    private val reaktiveSpisok = ArrayList<ReaktiveSpisok>()
    private val inventarnySpisok = ArrayList<InventorySpisok>()
    private var toDataAlarm = 45L
    override fun onReceive(context: Context, intent: Intent) {
        val fuel = context.getSharedPreferences("fuel", Context.MODE_PRIVATE)
        when (fuel.getInt("notification", 0)) {
            0 -> toDataAlarm = 45L
            1 -> toDataAlarm = 30L
            2 -> toDataAlarm = 15L
            3 -> toDataAlarm = 10L
            4 -> toDataAlarm = 5L
            5 -> toDataAlarm = 0L
        }
        task(context)
    }

    private fun task(context: Context) {
        val mDatabase = FirebaseDatabase.getInstance().reference
        if (FirebaseAuth.getInstance().currentUser != null) {
            if (CremLabFuel.isNetworkAvailable(context)) {
                testData.clear()
                mDatabase.child("equipments").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (data in dataSnapshot.children) {
                            if (data.value is HashMap<*, *>) {
                                val hashMap = data.value as HashMap<*, *>
                                if (hashMap.size > 12) {
                                    var editedAt = hashMap["editedAt"]
                                    var editedBy = hashMap["editedBy"]
                                    if (hashMap["editedAt"] == null) editedAt = 0L
                                    if (hashMap["editedBy"] == null) editedBy = ""
                                    inventarnySpisok.add(InventorySpisok(context, hashMap["createdBy"] as String?, hashMap["data01"] as Long, hashMap["data02"] as String?, hashMap["data03"] as String?, hashMap["data04"] as String?, hashMap["data05"] as String?, hashMap["data06"] as String?, hashMap["data07"] as String?, hashMap["data08"] as String?, hashMap["data09"] as String?, hashMap["data10"] as String?, hashMap["data11"] as Long, hashMap["data12"] as String?, data.key, editedAt as Long, editedBy as String?))
                                }
                            }
                        }
                        checkAlarm(context)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
                mDatabase.child("reagents").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        reaktiveSpisok.clear()
                        for (data in dataSnapshot.children) {
                            val id = data.key ?: ""
                            val g = Calendar.getInstance() as GregorianCalendar
                            val srokToDay = g.timeInMillis
                            var data05b: Long = 0
                            var srok = 1 // Срок в норме
                            for (data2 in data.children) {
                                if (data2.value is HashMap<*, *>) {
                                    val hashMap = data2.value as HashMap<*, *>
                                    if (hashMap.size >= 12) {
                                        val data05 = data2.child("data05").value as String
                                        val d = data05.split("-").toTypedArray()
                                        if (d.size == 3) g[d[0].toInt(), d[1].toInt() - 1] = d[2].toInt() else g[d[0].toInt(), d[1].toInt() - 1] = 1
                                        g.add(Calendar.MONTH, (data2.child("data06").value as Long).toInt())
                                        data05b = g.timeInMillis
                                        srok = if (srokToDay < g.timeInMillis) {
                                            g.add(Calendar.DATE, (-toDataAlarm).toInt())
                                            if (srokToDay > g.timeInMillis) {
                                                0 // Истекает срок
                                            } else {
                                                1 // Срок в норме
                                            }
                                        } else {
                                            -1 // Срок истёк
                                        }
                                    }
                                }
                            }
                            reaktiveSpisok.add(ReaktiveSpisok(context, data05b, id.toInt(), srok))
                        }
                        checkAlarmReaktive(context)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
            }
        }
    }

    private fun checkAlarmReaktive(context: Context) {
        val c = Calendar.getInstance() as GregorianCalendar
        val realtime = c.timeInMillis
        c[c[Calendar.YEAR], c[Calendar.MONTH], c[Calendar.DATE], 8, 0] = 0
        val time = c.timeInMillis
        for (reaktiveSpisok in reaktiveSpisok) {
            removeAlarm(context, reaktiveSpisok.id * 100)
            if (toDataAlarm != 0L) {
                when (reaktiveSpisok.check) {
                    1 -> {
                        c.timeInMillis = reaktiveSpisok.data
                        c[c[Calendar.YEAR], c[Calendar.MONTH], c[Calendar.DATE], 8, 0] = 0
                        c.add(Calendar.DATE, (-toDataAlarm).toInt())
                        setAlarm(context, c, reaktiveSpisok.id * 100, true)
                    }
                    0 -> {
                        if (realtime > time) {
                            if (c[Calendar.DAY_OF_WEEK] == Calendar.FRIDAY) c.add(Calendar.DATE, 3) else c.add(Calendar.DATE, 1)
                        }
                        setAlarm(context, c, reaktiveSpisok.id * 100, true)
                    }
                }
            }
        }
    }

    private fun checkAlarm(context: Context) {
        val timer = toDataAlarm * 24L * 60L * 60L * 1000L
        val c = Calendar.getInstance() as GregorianCalendar
        val realtime = c.timeInMillis
        c[c[Calendar.YEAR], c[Calendar.MONTH], c[Calendar.DATE], 8, 0] = 0
        val time = c.timeInMillis
        for (inventarny_spisok_datum in inventarnySpisok) {
            val data01 = inventarny_spisok_datum.data01.toInt()
            removeAlarm(context, data01)
            if (toDataAlarm != 0L) {
                val data08 = inventarny_spisok_datum.data08
                if (data08 != null && data08 != "") {
                    val t1 = data08.split("-").toTypedArray()
                    c[t1[0].toInt(), t1[1].toInt() - 1, t1[2].toInt(), 8, 0] = 0
                    val timeset = c.timeInMillis
                    val timeres = timeset - time
                    if (timeres > -timer && timeres < timer) {
                        c.timeInMillis = time
                        if (realtime > time) {
                            if (c[Calendar.DAY_OF_WEEK] == Calendar.FRIDAY) c.add(Calendar.DATE, 3) else c.add(Calendar.DATE, 1)
                        }
                        setAlarm(context, c, data01, false)
                    } else if (timeres > timer) {
                        val calendar = GregorianCalendar()
                        val data09 = inventarny_spisok_datum.data09
                        val data10 = inventarny_spisok_datum.data10
                        if (data09 != null && data09 != "") {
                            val t2 = data09.split("-").toTypedArray()
                            calendar[t2[0].toInt(), t2[1].toInt() - 1] = t2[2].toInt()
                            val t2l = calendar.timeInMillis
                            if (data10 != null && data10 != "") {
                                val t3 = data10.split("-").toTypedArray()
                                calendar[t3[0].toInt(), t3[1].toInt() - 1] = t3[2].toInt()
                                val t3l = calendar.timeInMillis
                                if (t2l < t3l) {
                                    c.add(Calendar.DATE, (-toDataAlarm).toInt())
                                    setAlarm(context, c, data01, false)
                                }
                            }
                        } else {
                            c.add(Calendar.DATE, (-toDataAlarm).toInt())
                            setAlarm(context, c, data01, false)
                        }
                    }
                }
            }
        }
    }

    private fun removeAlarm(context: Context, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pIntent = PendingIntent.getBroadcast(context, requestCode, Intent(context, ReceiverNotification::class.java), 0)
        alarmManager.cancel(pIntent)
    }

    private fun setAlarm(context: Context, c: GregorianCalendar, requestCode: Int, reaktive: Boolean) {
        var testAlarm = true
        if (!reaktive) {
            val testDataLocal = c[Calendar.YEAR].toString() + "-" + c[Calendar.MONTH] + "-" + c[Calendar.DATE]
            for (i in testData.indices) {
                if (testData[i].contains(testDataLocal)) {
                    testAlarm = false
                    break
                }
            }
        }
        if (testAlarm) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ReceiverNotification::class.java)
            intent.putExtra("reaktive", reaktive)
            val pIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, c.timeInMillis, pIntent)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.timeInMillis, pIntent)
                else -> alarmManager[AlarmManager.RTC_WAKEUP, c.timeInMillis] = pIntent
            }
        }
        testData.add(c[Calendar.YEAR].toString() + "-" + c[Calendar.MONTH] + "-" + c[Calendar.DATE])
    }
}
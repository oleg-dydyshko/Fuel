package serhij.korneluk.chemlabfuel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import serhij.korneluk.chemlabfuel.databinding.SettingsActivityBinding
import java.util.*

class SettingsActivity : AppCompatActivity() {
    private val data = arrayOf("за 45 дней", "за 30 дней", "за 15 дней", "за 10 дней", "за 5 дней", "Никогда")
    private lateinit var binding: SettingsActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fuel = getSharedPreferences("fuel", Context.MODE_PRIVATE)
        val editor = fuel.edit()
        val fontsize = fuel.getInt("fontsize", 18)
        val notifi = fuel.getInt("notification", 0)
        binding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val adapter: ArrayAdapter<String> = ListAdapter(this)
        binding.spinner9.adapter = adapter
        binding.spinner9.setSelection(notifi)
        binding.spinner9.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                editor.putInt("notification", position)
                editor.apply()
                runAlarm()
            }

            override fun onNothingSelected(arg0: AdapterView<*>?) {}
        }
        binding.textsize.text = getString(R.string.text_size, fontsize)
        binding.textsize.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontsize.toFloat())
        binding.seekBar.max = 22 - 14
        binding.seekBar.progress = (fontsize - 14) / 2
        binding.seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                var progress1 = progress
                progress1 *= 2
                binding.textsize.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14 + progress1.toFloat())
                binding.textsize.text = getString(R.string.text_size, 14 + progress1)
                editor.putInt("fontsize", 14 + progress1)
                editor.apply()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        setTollbarTheme()
    }

    private fun setTollbarTheme() {
        binding.titleToolbar.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.titleToolbar.text = "Настройки"
    }

    override fun onBackPressed() {
        onSupportNavigateUp()
    }

    public override fun onResume() {
        super.onResume()
        overridePendingTransition(R.anim.alphain, R.anim.alphaout)
    }

    private inner class ListAdapter(context: Context) : ArrayAdapter<String>(context, R.layout.simple_list_item2, data) {
        override fun getView(position: Int, mView: View?, parent: ViewGroup): View {
            val root: View
            val viewHolder: ViewHolder
            if (mView == null) {
                val vi = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                root = vi.inflate(R.layout.simple_list_item2, parent, false)
                viewHolder = ViewHolder()
                root.tag = viewHolder
                viewHolder.text = root.findViewById(R.id.label)
            } else {
                root = mView
                viewHolder = root.tag as ViewHolder
            }
            viewHolder.text?.text = data[position]
            return root
        }
    }

    private class ViewHolder {
        var text: TextView? = null
    }

    companion object {
        private val testData = ArrayList<String>()
        private val reaktiveSpisok = ArrayList<ReaktiveSpisok>()
        private val inventarnySpisok = ArrayList<InventorySpisok>()
        private var toDataAlarm = 45

        fun runAlarm() {
            val context = ChemLabFuelApp.applicationContext()
            val fuel = context.getSharedPreferences("fuel", Context.MODE_PRIVATE)
            when (fuel.getInt("notification", 0)) {
                0 -> toDataAlarm = 45
                1 -> toDataAlarm = 30
                2 -> toDataAlarm = 15
                3 -> toDataAlarm = 10
                4 -> toDataAlarm = 5
                5 -> toDataAlarm = 0
            }
            val mDatabase = FirebaseDatabase.getInstance(ChemLabFuelApp.getApp()).reference
            if (FirebaseAuth.getInstance(ChemLabFuelApp.getApp()).currentUser != null) {
                if (ChemLabFuel.isNetworkAvailable()) {
                    testData.clear()
                    mDatabase.child("equipments").addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            inventarnySpisok.clear()
                            for (data in dataSnapshot.children) {
                                if (data.value is HashMap<*, *>) {
                                    val hashMap = data.value as HashMap<*, *>
                                    if (hashMap.size > 12) {
                                        val editedAt = hashMap["editedAt"] ?: 0L
                                        val editedBy = hashMap["editedBy"] ?: ""
                                        inventarnySpisok.add(InventorySpisok(context, hashMap["createdBy"] as String?, hashMap["data01"] as Long, hashMap["data02"] as String?, hashMap["data03"] as String?, hashMap["data04"] as String?, hashMap["data05"] as String?, hashMap["data06"] as String?, hashMap["data07"] as String?, hashMap["data08"] as String?, hashMap["data09"] as String?, hashMap["data10"] as String?, hashMap["data11"] as Long, hashMap["data12"] as String?, data.key, editedAt as Long, editedBy as String?))
                                    }
                                }
                            }
                            checkAlarm()
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
                                                g.add(Calendar.DATE, -toDataAlarm)
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
                            checkAlarmReaktive()
                        }

                        override fun onCancelled(databaseError: DatabaseError) {}
                    })
                }
            }
        }

        private fun checkAlarmReaktive() {
            val c = Calendar.getInstance() as GregorianCalendar
            c.set(Calendar.MILLISECOND, 0)
            val realtime = c.timeInMillis
            for (reaktiveSpisok in reaktiveSpisok) {
                removeAlarm(reaktiveSpisok.id * 100)
                if (toDataAlarm != 0) {
                    if (reaktiveSpisok.check == 1) {
                        c.timeInMillis = reaktiveSpisok.data
                        c.set(c[Calendar.YEAR], c[Calendar.MONTH], c[Calendar.DATE], 8, 0, 0)
                        c.set(Calendar.MILLISECOND, 0)
                        c.add(Calendar.DATE, -toDataAlarm)
                    }
                    if (realtime > c.timeInMillis) {
                        when (c[Calendar.DAY_OF_WEEK]) {
                            Calendar.FRIDAY -> c.add(Calendar.DATE, 3)
                            Calendar.SATURDAY -> c.add(Calendar.DATE, 2)
                            else -> c.add(Calendar.DATE, 1)
                        }
                    }
                    setAlarm(c, reaktiveSpisok.id * 100, true)
                }
            }
        }

        private fun checkAlarm() {
            val timer = toDataAlarm * 24 * 60 * 60 * 1000L
            val c = Calendar.getInstance() as GregorianCalendar
            c.set(Calendar.SECOND, 0)
            c.set(Calendar.MILLISECOND, 0)
            val realtime = c.timeInMillis
            for (inventarny_spisok_datum in inventarnySpisok) {
                val data01 = inventarny_spisok_datum.data01.toInt()
                removeAlarm(data01)
                if (toDataAlarm != 0) {
                    val data08 = inventarny_spisok_datum.data08 ?: ""
                    if (data08 != "") {
                        val t1 = data08.split("-").toTypedArray()
                        c.set(t1[0].toInt(), t1[1].toInt() - 1, t1[2].toInt(), 8, 0, 0)
                        c.set(Calendar.MILLISECOND, 0)
                        val timeres = realtime - c.timeInMillis
                        if (timeres > -timer && timeres < timer) {
                            if (realtime > c.timeInMillis) {
                                c.timeInMillis = realtime
                                c.set(Calendar.HOUR_OF_DAY, 8)
                                c.set(Calendar.MINUTE, 0)
                                c.set(Calendar.SECOND, 0)
                                when (c[Calendar.DAY_OF_WEEK]) {
                                    Calendar.FRIDAY -> c.add(Calendar.DATE, 3)
                                    Calendar.SATURDAY -> c.add(Calendar.DATE, 2)
                                    else -> c.add(Calendar.DATE, 1)
                                }
                            }
                            setAlarm(c, data01, false)
                        }
                    }
                }
            }
        }

        private fun removeAlarm(requestCode: Int) {
            val context = ChemLabFuelApp.applicationContext()
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE or 0
            } else {
                0
            }
            val pIntent = PendingIntent.getBroadcast(context, requestCode, Intent(context, ReceiverNotification::class.java), flags)
            alarmManager.cancel(pIntent)
        }

        private fun setAlarm(c: GregorianCalendar, requestCode: Int, reaktive: Boolean) {
            val context = ChemLabFuelApp.applicationContext()/*var testAlarm = true
            if (!reaktive) {
                val testDataLocal = c[Calendar.YEAR].toString() + "-" + c[Calendar.MONTH] + "-" + c[Calendar.DATE]
                for (i in testData.indices) {
                    if (testData[i].contains(testDataLocal)) {
                        testAlarm = false
                        break
                    }
                }
            }*/ //if (testAlarm) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ReceiverNotification::class.java)
            intent.putExtra("reaktive", reaktive)
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags)
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms() -> alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, c.timeInMillis, pIntent)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, c.timeInMillis, pIntent)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.timeInMillis, pIntent)
                else -> alarmManager[AlarmManager.RTC_WAKEUP, c.timeInMillis] = pIntent
            } //}
            testData.add(c[Calendar.YEAR].toString() + "-" + c[Calendar.MONTH] + "-" + c[Calendar.DATE])
        }
    }
}
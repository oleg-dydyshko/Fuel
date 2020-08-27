package serhij.korneluk.chemlabfuel

import android.content.Context
import android.content.Intent
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
import kotlinx.android.synthetic.main.settings_activity.*

class SettingsActivity : AppCompatActivity() {
    private val data = arrayOf("за 45 дней", "за 30 дней", "за 15 дней", "за 10 дней", "за 5 дней", "Никогда")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fuel = getSharedPreferences("fuel", Context.MODE_PRIVATE)
        val editor = fuel.edit()
        val fontsize = fuel.getInt("fontsize", 18)
        val notifi = fuel.getInt("notification", 0)
        setContentView(R.layout.settings_activity)
        val adapter: ArrayAdapter<String> = ListAdapter(this)
        spinner9.adapter = adapter
        spinner9.setSelection(notifi)
        spinner9.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                editor.putInt("notification", position)
                editor.apply()
                sendBroadcast(Intent(this@SettingsActivity, ReceiverSetAlarm::class.java))
            }

            override fun onNothingSelected(arg0: AdapterView<*>?) {}
        }
        textsize.text = getString(R.string.text_size, fontsize)
        textsize.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontsize.toFloat())
        seekBar.max = 22 - 14
        seekBar.progress = (fontsize - 14) / 2
        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                var progress1 = progress
                progress1 *= 2
                textsize.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14 + progress1.toFloat())
                textsize.text = getString(R.string.text_size, 14 + progress1)
                editor.putInt("fontsize", 14 + progress1)
                editor.apply()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        setTollbarTheme()
    }

    private fun setTollbarTheme() {
        title_toolbar.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title_toolbar.text = "Настройки"
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
}
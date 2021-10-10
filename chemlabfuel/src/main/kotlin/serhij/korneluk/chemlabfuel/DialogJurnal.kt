package serhij.korneluk.chemlabfuel

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class DialogJurnal : DialogFragment() {
    private lateinit var alert: AlertDialog
    private lateinit var jur: ArrayList<ArrayList<String>>
    private lateinit var listAdapter: ArrayAdapter<ArrayList<String>>

    fun updateJurnalRasxoda(position: Int, t0: String, t1: String, t2: String, t3: String, t4: String, t5: String) {
        jur[position][0] = t0
        jur[position][1] = t1
        jur[position][2] = t2
        jur[position][3] = t3
        jur[position][4] = t4
        jur[position][5] = t5
        listAdapter.notifyDataSetChanged()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        activity?.let {
            val gson = Gson()
            val type = object : TypeToken<ArrayList<ArrayList<String>>>() {}.type
            jur = gson.fromJson(arguments?.getString("jurnal") ?: "", type)
            val builder = AlertDialog.Builder(it)
            val linearLayout = LinearLayout(it)
            linearLayout.orientation = LinearLayout.VERTICAL
            val textViewZaglavie = TextView(it)
            textViewZaglavie.setBackgroundColor(ContextCompat.getColor(it, R.color.colorPrimary))
            textViewZaglavie.setPadding(10, 10, 10, 10)
            textViewZaglavie.text = getString(R.string.gurnal_rasxoda)
            textViewZaglavie.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            textViewZaglavie.setTypeface(null, Typeface.BOLD)
            textViewZaglavie.setTextColor(ContextCompat.getColor(it, R.color.colorIcons))
            linearLayout.addView(textViewZaglavie)
            val listView = ListView(it)
            listAdapter = ListAdapter(it)
            listView.adapter = listAdapter
            listView.onItemClickListener = OnItemClickListener { _: AdapterView<*>?, _: View?, i: Int, _: Long ->
                val jurnal = gson.toJson(jur)
                val rasxod = DialodReaktRasxod.getInstance(arguments?.getInt("groupposition") ?: 0, arguments?.getInt("childposition") ?: 0, arguments?.getInt("izmerenie") ?: 0, jur[i][5], jurnal, i, arguments?.getString("ostatok", "") ?: "0,0")
                rasxod.show(childFragmentManager, "rasxod")
            }
            linearLayout.addView(listView)
            builder.setView(linearLayout)
            builder.setPositiveButton(getString(R.string.good)) { dialog: DialogInterface, _: Int -> dialog.cancel() }
            alert = builder.create()
            alert.setOnShowListener {
                val btnPositive = alert.getButton(Dialog.BUTTON_POSITIVE)
                btnPositive.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            }
        }
        return alert
    }

    private inner class ListAdapter(context: Activity) : ArrayAdapter<ArrayList<String>>(context, R.layout.simple_list_item2, jur) {
        private val fuel: SharedPreferences = context.getSharedPreferences("fuel", Context.MODE_PRIVATE)

        @SuppressLint("SetTextI18n")
        override fun getView(position: Int, mView: View?, parent: ViewGroup): View {
            val root: View
            val viewHolder: ViewHolder
            if (mView == null) {
                val vi = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                root = vi.inflate(R.layout.simple_list_item2, parent, false)
                viewHolder = ViewHolder()
                root.tag = viewHolder
                viewHolder.text = root.findViewById(R.id.label)
            } else {
                root = mView
                viewHolder = root.tag as ViewHolder
            }
            val createBy = jur[position][5]
            var fnG = ""
            var lnG = ""
            for (i2 in ChemLabFuel.users.indices) {
                if (ChemLabFuel.users[i2][0].contains(createBy)) {
                    fnG = ChemLabFuel.users[i2][1]
                    lnG = ChemLabFuel.users[i2][2]
                    break
                }
            }
            viewHolder.text?.text = jur[position][0] + "\n" + jur[position][1] + " " + jur[position][2] + "\nПлотность: " + jur[position][3] + "\nНа цель: " + jur[position][4] + "\nЗапись внёс: " + fnG + " " + lnG
            viewHolder.text?.textSize = fuel.getInt("fontsize", 18).toFloat()
            return root
        }
    }

    private class ViewHolder {
        var text: TextView? = null
    }

    companion object {
        fun getInstance(groupPosition: Int, childposition: Int, izmerenie: Int, jurnalText: String?, user: String?, ostatok: String): DialogJurnal {
            val jurnal = DialogJurnal()
            val bundle = Bundle()
            bundle.putInt("groupposition", groupPosition)
            bundle.putInt("childposition", childposition)
            bundle.putInt("izmerenie", izmerenie)
            bundle.putString("jurnal", jurnalText)
            bundle.putString("user", user)
            bundle.putString("ostatok", ostatok)
            jurnal.arguments = bundle
            return jurnal
        }
    }
}
package serhij.korneluk.chemlabfuel

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.dialog_reakt_rasxod.*
import java.math.BigDecimal
import java.util.*

class DialodReaktRasxod : DialogFragment() {
    private lateinit var alert: AlertDialog
    private var groupPosition = 0
    private var childposition = 0
    private var izmerenie = 0
    private lateinit var c: GregorianCalendar
    private var user = ""
    private val edIzmerenia = arrayOf("килограммах", "миллиграммах", "литрах", "миллилитрах")
    private val edIzmerenia2 = arrayOf("килограмм(-а)", "миллиграмм(-а)", "литр(-а)", "миллилитр(-а)")
    private var jurnal = ""
    private var position = 0
    private lateinit var jur: ArrayList<ArrayList<String>>
    private var listiner: UpdateJurnal? = null
    private lateinit var rootView: View

    internal interface UpdateJurnal {
        fun updateJurnalRasxoda(position: Int, t0: String, t1: String, t2: String, t3: String, t4: String, t5: String)
    }

    internal fun setUpdateJurnal(listiner: UpdateJurnal) {
        this.listiner = listiner
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (arguments != null) {
            groupPosition = arguments?.getInt("groupposition", 1) ?: 1
            childposition = arguments?.getInt("childposition", 1) ?: 1
            izmerenie = arguments?.getInt("izmerenie", 0) ?: 0
            user = arguments?.getString("user", "") ?: ""
            jurnal = arguments?.getString("jurnal", "") ?: ""
            position = arguments?.getInt("position", 0) ?: 0
        }
        kolkast.text = kolkast.text.toString() + " в " + edIzmerenia[izmerenie]
        c = Calendar.getInstance() as GregorianCalendar
        setData(1, c[Calendar.YEAR], c[Calendar.MONTH], c[Calendar.DATE])
        button1.setOnClickListener {
            val t1 = textView1e.text.toString().split("-").toTypedArray()
            c = GregorianCalendar(t1[0].toInt(), t1[1].toInt() - 1, t1[2].toInt())
            fragmentManager?.let {
                val data: DialogData = DialogData.getInstance(c.timeInMillis, 1, textView1.text.toString(), 2)
                data.show(it, "data")
            }
        }
        textViewTitle.text = CremLabFuel.ReaktiveSpisok[groupPosition]?.get(childposition)?.get(13)
        textView3e.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                send()
                return@setOnEditorActionListener true
            }
            false
        }
        textView2e.addTextChangedListener(MyTextWatcher(textView2e))
        val gson = Gson()
        val type = object : TypeToken<ArrayList<ArrayList<String?>?>?>() {}.type
        if (jurnal != "") {
            jur = gson.fromJson<ArrayList<ArrayList<String>>>(jurnal, type)
            textView1e.text = jur[position][0]
            textView2e.setText(jur[position][1])
            textView4e.setText(jur[position][3])
            textView3e.setText(jur[position][4])
        } else {
            jur = gson.fromJson<ArrayList<ArrayList<String>>>(CremLabFuel.ReaktiveSpisok[groupPosition]?.get(childposition)?.get(16), type)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return rootView
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        activity?.let { activity ->
            rootView = View.inflate(activity, R.layout.dialog_reakt_rasxod, null)
            val builder = AlertDialog.Builder(activity)
            builder.setPositiveButton(getString(R.string.save)) { _: DialogInterface?, _: Int -> send() }
            builder.setNegativeButton(getString(R.string.cansel)) { dialog: DialogInterface, _: Int -> dialog.cancel() }
            builder.setView(rootView)
            alert = builder.create()
            alert.setOnShowListener {
                val btnPositive = alert.getButton(Dialog.BUTTON_POSITIVE)
                btnPositive.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                val btnNegative = alert.getButton(Dialog.BUTTON_NEGATIVE)
                btnNegative.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            }
        }
        return alert
    }

    private fun send() {
        if (textView2e.text.toString().trim() != "" && textView3e.text.toString() != "") {
            if (textView4e.text.toString().trim() == "") textView4e.setText("1")
            val correkt: BigDecimal
            if (jurnal == "") {
                correkt = BigDecimal(CremLabFuel.ReaktiveSpisok[groupPosition]?.get(childposition)?.get(9))
                val subJurnal = ArrayList<String>()
                subJurnal.add(textView1e.text.toString())
                subJurnal.add(BigDecimal(textView2e.text.toString().trim().replace(",", ".")).toString().replace(".", ","))
                subJurnal.add(edIzmerenia2[izmerenie])
                subJurnal.add(BigDecimal(textView4e.text.toString().trim().replace(",", ".")).toString().replace(".", ","))
                subJurnal.add(textView3e.text.toString())
                subJurnal.add(user)
                jur.add(subJurnal)
            } else {
                correkt = BigDecimal(CremLabFuel.ReaktiveSpisok[groupPosition]?.get(childposition)?.get(9)).add(BigDecimal(jur[position][1].replace(",", ".")))
                jur[position][0] = textView1e.text.toString()
                jur[position][1] = BigDecimal(textView2e.text.toString().trim().replace(",", ".")).toString().replace(".", ",")
                jur[position][2] = edIzmerenia2[izmerenie]
                jur[position][3] = BigDecimal(textView4e.text.toString().trim().replace(",", ".")).toString().replace(".", ",")
                jur[position][4] = textView3e.text.toString()
                jur[position][5] = user
            }
            val nomerProdukta = groupPosition.toString()
            val nomerPartii = childposition.toString()
            val ras = BigDecimal(textView2e.text.toString().trim().replace(",", "."))
            val rasxod = correkt.subtract(ras)
            val mDatabase = FirebaseDatabase.getInstance().reference
            mDatabase.child("reagents").child(nomerProdukta).child(nomerPartii).child("data09").setValue(rasxod.toDouble())
            mDatabase.child("reagents").child(nomerProdukta).child(nomerPartii).child("data11").setValue(jur)
            listiner?.updateJurnalRasxoda(position, jur[position][0], jur[position][1], jur[position][2], jur[position][3], jur[position][4], jur[position][5])
        } else {
            activity?.let {
                CremLabFuel.setToast(it, getString(R.string.error))
            }
        }
        dialog?.cancel()
    }

    fun setData(textview: Int, year: Int, month: Int, dayOfMonth: Int) {
        var zero = ""
        var zero2 = ""
        if (month < 9) zero = "0"
        if (dayOfMonth < 10) zero2 = "0"
        if (textview == 1) {
            if (year == 0) textView1e.text = "" else textView1e.text = getString(R.string.set_date, year, zero, month + 1, zero2, dayOfMonth)
        }
    }

    private inner class MyTextWatcher internal constructor(private val textView: EditText) : TextWatcher {
        private var editPosition = 0
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            editPosition = start + count
        }

        override fun afterTextChanged(s: Editable) {
            var edit = s.toString()
            edit = edit.replace(".", ",")
            textView.removeTextChangedListener(this)
            textView.setText(edit)
            textView.setSelection(editPosition)
            textView.addTextChangedListener(this)
        }

    }

    companion object {
        fun getInstance(groupPosition: Int, childposition: Int, izmerenie: Int, user: String?): DialodReaktRasxod {
            val opisanie = DialodReaktRasxod()
            val bundle = Bundle()
            bundle.putInt("groupposition", groupPosition)
            bundle.putInt("childposition", childposition)
            bundle.putInt("izmerenie", izmerenie)
            bundle.putString("user", user)
            opisanie.arguments = bundle
            return opisanie
        }

        fun getInstance(groupPosition: Int, childposition: Int, izmerenie: Int, user: String?, jurnal: String?, position: Int): DialodReaktRasxod {
            val opisanie = DialodReaktRasxod()
            val bundle = Bundle()
            bundle.putInt("groupposition", groupPosition)
            bundle.putInt("childposition", childposition)
            bundle.putInt("izmerenie", izmerenie)
            bundle.putString("user", user)
            bundle.putString("jurnal", jurnal)
            bundle.putInt("position", position)
            opisanie.arguments = bundle
            return opisanie
        }
    }
}
package serhij.korneluk.chemlabfuel

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.dialog_opisanie_edit_reakt.*
import java.util.*

class DialodOpisanieEditReakt : DialogFragment() {
    private lateinit var alert: AlertDialog
    private var user = ""
    private var title = ""
    private var groupPosition = 0
    private var childposition = 0
    private var edIzmerenia = 0
    private val data = arrayOf("Килограмм", "Миллиграмм", "Литры", "Миллилитры")
    private var listiner: ListUpdateListiner? = null
    private lateinit var rootView: View

    internal interface ListUpdateListiner {
        fun updateList()
    }

    internal fun setListUpdateListiner(listiner: ListUpdateListiner) {
        this.listiner = listiner
    }

    fun setData(textview: Int, year: Int, month: Int, dayOfMonth: Int) {
        var zero = ""
        var zero2 = ""
        if (month < 9) zero = "0"
        if (dayOfMonth < 10) zero2 = "0"
        when (textview) {
            3 -> if (year == 0) textView3e.text = "" else textView3e.text = getString(R.string.set_date, year, zero, month + 1, zero2, dayOfMonth)
            8 -> if (year == 0) textView8e.setText("") else textView8e.setText(getString(R.string.set_date, year, zero, month + 1, zero2, dayOfMonth))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let { activity ->
            val god = arrayOf("Год", "Месяц")
            textView2e.addTextChangedListener(MyTextWatcher(textView2e))
            spinner9.adapter = ListAdapter(activity, god)
            spinner11e.adapter = ListAdapter(activity, data)
            spinner11e.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {}
                override fun onNothingSelected(arg0: AdapterView<*>?) {}
            }
            textView12e.addTextChangedListener(MyTextWatcher(textView12e))
            textView13e.addTextChangedListener(MyTextWatcher(textView13e))
            button3.setOnClickListener {
                val c: GregorianCalendar
                c = if (textView3e.text.toString() == "") {
                    Calendar.getInstance() as GregorianCalendar
                } else {
                    val t1 = textView3e.text.toString().split("-").toTypedArray()
                    GregorianCalendar(t1[0].toInt(), t1[1].toInt() - 1, t1[2].toInt())
                }
                val data: DialogData = DialogData.getInstance(c.timeInMillis, 3, textView3.text.toString(), 2)
                fragmentManager?.let {
                    data.show(it, "data")
                }
            }
            button8.setOnClickListener {
                val c: GregorianCalendar
                c = if (textView8e.text.toString() == "") {
                    Calendar.getInstance() as GregorianCalendar
                } else {
                    val t1 = textView8e.text.toString().split("-").toTypedArray()
                    if (t1.size == 3) GregorianCalendar(t1[0].toInt(), t1[1].toInt() - 1, t1[2].toInt()) else GregorianCalendar(t1[0].toInt(), t1[1].toInt() - 1, 1)
                }
                val data: DialogData = DialogData.getInstance(c.timeInMillis, 8, textView8.text.toString(), 2)
                fragmentManager?.let {
                    data.show(it, "data")
                }
            }
            user = arguments?.getString("user", "") ?: ""
            title = arguments?.getString("title", "") ?: ""
            groupPosition = arguments?.getInt("groupposition", 1) ?: 1
            childposition = arguments?.getInt("childposition", 1) ?: 1
            val minostatok = arguments?.getString("minostatok", "") ?: ""
            edIzmerenia = arguments?.getInt("ed_izmerenia", 0) ?: 0
            if (add) {
                textViewTitle.setText(R.string.add)
                textView2e.setText(title)
                textView13e.setText(minostatok)
                textView13e.imeOptions = EditorInfo.IME_ACTION_GO
                textView13e.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
                    if (actionId == EditorInfo.IME_ACTION_GO) {
                        send()
                        return@setOnEditorActionListener true
                    }
                    false
                }
                spinner11e.setSelection(edIzmerenia)
            } else {
                textView12.setText(R.string.kol_na_ost)
                spinner9.visibility = View.GONE
                textViewTitle.text = CremLabFuel.ReaktiveSpisok[groupPosition]?.get(childposition)?.get(13)
                textView2e.setText(CremLabFuel.ReaktiveSpisok[groupPosition]?.get(childposition)?.get(13))
                textView3e.text = CremLabFuel.ReaktiveSpisok[groupPosition]?.get(childposition)?.get(1)
                textView5e.setText(CremLabFuel.ReaktiveSpisok[groupPosition]?.get(childposition)?.get(2))
                textView6e.setText(CremLabFuel.ReaktiveSpisok[groupPosition]?.get(childposition)?.get(3))
                textView7e.setText(CremLabFuel.ReaktiveSpisok[groupPosition]?.get(childposition)?.get(4))
                textView8e.setText(CremLabFuel.ReaktiveSpisok[groupPosition]?.get(childposition)?.get(5))
                textView9e.setText(CremLabFuel.ReaktiveSpisok[groupPosition]?.get(childposition)?.get(6).toString())
                textView10e.setText(CremLabFuel.ReaktiveSpisok[groupPosition]?.get(childposition)?.get(7))
                spinner11e.setSelection(CremLabFuel.ReaktiveSpisok[groupPosition]?.get(childposition)?.get(8)?.toInt()
                        ?: 0)
                textView12e.setText(CremLabFuel.ReaktiveSpisok[groupPosition]?.get(childposition)?.get(9).toString())
                textView13e.setText(CremLabFuel.ReaktiveSpisok[groupPosition]?.get(childposition)?.get(10).toString())
                textView14e.setText(CremLabFuel.ReaktiveSpisok[groupPosition]?.get(childposition)?.get(17).toString())
            }
            textView2e.setSelection(textView2e.text.length)
            textView5e.setSelection(textView5e.text.length)
            textView6e.setSelection(textView6e.text.length)
            textView7e.setSelection(textView7e.text.length)
            textView9e.setSelection(textView9e.text.length)
            textView10e.setSelection(textView10e.text.length)
            textView12e.setSelection(textView12e.text.length)
            textView13e.setSelection(textView13e.text.length)
            textView14e.setSelection(textView14e.text.length)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return rootView
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        activity?.let { activity ->
            val builder = AlertDialog.Builder(activity)
            rootView = View.inflate(activity, R.layout.dialog_opisanie_edit_reakt, null)
            builder.setView(rootView)
            builder.setPositiveButton(getString(R.string.save)) { _: DialogInterface?, _: Int -> send() }
            builder.setNegativeButton(getString(R.string.cansel)) { dialog: DialogInterface, _: Int -> dialog.cancel() }
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

    @SuppressLint("SetTextI18n")
    private fun send() {
        if (textView6e.text.toString().trim() == "") {
            textView6e.setText(R.string.no)
        }
        if (textView8e.text.toString().trim() == "") {
            val calendar = Calendar.getInstance()
            val month = calendar[Calendar.MONTH] + 1
            var zero = ""
            if (month < 10) zero = "0"
            textView8e.setText(calendar[Calendar.YEAR].toString() + "-" + zero + month)
        }
        if (textView9e.text.toString().trim() == "") {
            textView9e.setText("1")
        }
        if (textView10e.text.toString().trim() == "") {
            textView10e.setText(R.string.obychnye)
        }
        if (textView12e.text.toString().trim() == "") {
            textView12e.setText("0")
        }
        if (textView13e.text.toString().trim() == "") {
            textView13e.setText("0")
        }
        val mDatabase = FirebaseDatabase.getInstance().reference
        val g = Calendar.getInstance() as GregorianCalendar
        var nomerProdukta = groupPosition.toString()
        var nomerPartii = childposition.toString()
        var text9 = java.lang.Long.valueOf(textView9e.text.toString().trim())
        if (add && spinner9.selectedItemPosition == 0) {
            text9 *= 12
        }
        if (add) {
            if (CremLabFuel.ReaktiveSpisok.size != 0) {
                for ((_, value) in CremLabFuel.ReaktiveSpisok.entries) {
                    for ((_, value2) in value.entries) {
                        var name = "no"
                        for ((key, value1) in value2.entries) {
                            if (key == 13) {
                                name = value1
                            }
                            if (key == 14) {
                                if (title == "") {
                                    groupPosition = value1.toInt() + 1
                                    nomerProdukta = groupPosition.toString()
                                } else if (textView2e.text.toString().trim().contains(name)) {
                                    groupPosition = value1.toInt()
                                    nomerProdukta = groupPosition.toString()
                                }
                            }
                            if (key == 15) {
                                if (textView2e.text.toString().trim().contains(name)) {
                                    childposition = value1.toInt() + 1
                                    nomerPartii = childposition.toString()
                                }
                            }
                        }
                    }
                }
            }
            mDatabase.child("reagents").child(nomerProdukta).child(nomerPartii).child("createdAt").setValue(g.timeInMillis)
            mDatabase.child("reagents").child(nomerProdukta).child(nomerPartii).child("createdBy").setValue(user)
        }
        mDatabase.child("reagents").child(nomerProdukta).child("name").setValue(textView2e.text.toString().trim())
        mDatabase.child("reagents").child(nomerProdukta).child(nomerPartii).child("data01").setValue(textView3e.text.toString().trim())
        mDatabase.child("reagents").child(nomerProdukta).child(nomerPartii).child("data02").setValue(textView5e.text.toString().trim())
        mDatabase.child("reagents").child(nomerProdukta).child(nomerPartii).child("data03").setValue(textView6e.text.toString().trim())
        mDatabase.child("reagents").child(nomerProdukta).child(nomerPartii).child("data04").setValue(textView7e.text.toString().trim())
        mDatabase.child("reagents").child(nomerProdukta).child(nomerPartii).child("data05").setValue(textView8e.text.toString().trim())
        mDatabase.child("reagents").child(nomerProdukta).child(nomerPartii).child("data06").setValue(text9)
        mDatabase.child("reagents").child(nomerProdukta).child(nomerPartii).child("data07").setValue(textView10e.text.toString().trim())
        mDatabase.child("reagents").child(nomerProdukta).child(nomerPartii).child("data08").setValue(spinner11e.selectedItemPosition.toLong())
        mDatabase.child("reagents").child(nomerProdukta).child(nomerPartii).child("data09").setValue(java.lang.Double.valueOf(textView12e.text.toString().trim().replace(",", ".")))
        mDatabase.child("reagents").child(nomerProdukta).child(nomerPartii).child("data10").setValue(java.lang.Double.valueOf(textView13e.text.toString().trim().replace(",", ".")))
        mDatabase.child("reagents").child(nomerProdukta).child(nomerPartii).child("data12").setValue(textView14e.text.toString().trim())
        if (!add) {
            mDatabase.child("reagents").child(nomerProdukta).child(nomerPartii).child("editedAt").setValue(g.timeInMillis)
            mDatabase.child("reagents").child(nomerProdukta).child(nomerPartii).child("editedBy").setValue(user)
        }
        activity?.let {
            it.sendBroadcast(Intent(it, ReceiverSetAlarm::class.java))
        }
        listiner?.updateList()
        dialog?.cancel()
    }

    private inner class MyTextWatcher(private val textView: EditText) : TextWatcher {
        private var editPosition = 0
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            editPosition = start + count
        }

        override fun afterTextChanged(s: Editable) {
            var edit = s.toString()
            if (textView.id == R.id.textView2e && edit != "") {
                textViewTitle.text = edit
            } else {
                edit = edit.replace(".", ",")
                textView.removeTextChangedListener(this)
                textView.setText(edit)
                textView.setSelection(editPosition)
                textView.addTextChangedListener(this)
            }
        }

    }

    private inner class ListAdapter(context: Context, private val dataA: Array<String>) : ArrayAdapter<String?>(context, R.layout.simple_list_item2, dataA) {
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
            viewHolder.text?.text = dataA[position]
            return root
        }

    }

    private class ViewHolder {
        var text: TextView? = null
    }

    companion object {
        private var add = false
        fun getInstance(user: String?, groupPosition: Int, childposition: Int): DialodOpisanieEditReakt {
            val opisanie = DialodOpisanieEditReakt()
            val bundle = Bundle()
            bundle.putString("user", user)
            bundle.putInt("groupposition", groupPosition)
            bundle.putInt("childposition", childposition)
            opisanie.arguments = bundle
            add = false
            return opisanie
        }

        fun getInstance(user: String?, title: String?, minostatok: String?, ed_izmerenia: Int): DialodOpisanieEditReakt {
            val opisanie = DialodOpisanieEditReakt()
            val bundle = Bundle()
            bundle.putString("user", user)
            bundle.putString("title", title)
            bundle.putString("minostatok", minostatok)
            bundle.putInt("ed_izmerenia", ed_izmerenia)
            opisanie.arguments = bundle
            add = true
            return opisanie
        }
    }
}
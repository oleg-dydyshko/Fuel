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
import serhij.korneluk.chemlabfuel.databinding.DialogOpisanieEditReaktBinding
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
    private var _binding: DialogOpisanieEditReaktBinding? = null
    private val binding get() = _binding!!

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
            3 -> if (year == 0) binding.textView3e.text = "" else binding.textView3e.text = getString(R.string.set_date, year, zero, month + 1, zero2, dayOfMonth)
            8 -> if (year == 0) binding.textView8e.setText("") else binding.textView8e.setText(getString(R.string.set_date, year, zero, month + 1, zero2, dayOfMonth))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        activity?.let {
            val builder = AlertDialog.Builder(it)
            _binding = DialogOpisanieEditReaktBinding.inflate(LayoutInflater.from(it))
            builder.setView(binding.root)
            val god = arrayOf("Год", "Месяц")
            binding.textView2e.addTextChangedListener(MyTextWatcher(binding.textView2e))
            binding.spinner9.adapter = ListAdapter(it, god)
            binding.spinner11e.adapter = ListAdapter(it, data)
            binding.spinner11e.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {}
                override fun onNothingSelected(arg0: AdapterView<*>?) {}
            }
            binding.textView12e.addTextChangedListener(MyTextWatcher(binding.textView12e))
            binding.textView13e.addTextChangedListener(MyTextWatcher(binding.textView13e))
            binding.button3.setOnClickListener {
                val c = if (binding.textView3e.text.toString() == "") {
                    Calendar.getInstance() as GregorianCalendar
                } else {
                    val t1 = binding.textView3e.text.toString().split("-").toTypedArray()
                    GregorianCalendar(t1[0].toInt(), t1[1].toInt() - 1, t1[2].toInt())
                }
                val data: DialogData = DialogData.getInstance(c.timeInMillis, 3, binding.textView3.text.toString(), 2)
                data.show(childFragmentManager, "data")
            }
            binding.button8.setOnClickListener {
                val c = if (binding.textView8e.text.toString() == "") {
                    Calendar.getInstance() as GregorianCalendar
                } else {
                    val t1 = binding.textView8e.text.toString().split("-").toTypedArray()
                    if (t1.size == 3) GregorianCalendar(t1[0].toInt(), t1[1].toInt() - 1, t1[2].toInt()) else GregorianCalendar(t1[0].toInt(), t1[1].toInt() - 1, 1)
                }
                val data: DialogData = DialogData.getInstance(c.timeInMillis, 8, binding.textView8.text.toString(), 2)
                data.show(childFragmentManager, "data")
            }
            user = arguments?.getString("user", "") ?: ""
            title = arguments?.getString("title", "") ?: ""
            groupPosition = arguments?.getInt("groupposition", 1) ?: 1
            childposition = arguments?.getInt("childposition", 1) ?: 1
            val minostatok = arguments?.getString("minostatok", "") ?: ""
            edIzmerenia = arguments?.getInt("ed_izmerenia", 0) ?: 0
            if (add) {
                binding.textViewTitle.setText(R.string.add)
                binding.textView2e.setText(title)
                binding.textView13e.setText(minostatok)
                binding.textView13e.imeOptions = EditorInfo.IME_ACTION_GO
                binding.textView13e.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
                    if (actionId == EditorInfo.IME_ACTION_GO) {
                        send()
                        return@setOnEditorActionListener true
                    }
                    false
                }
                binding.spinner11e.setSelection(edIzmerenia)
            } else {
                binding.textView12.setText(R.string.kol_na_ost)
                binding.spinner9.visibility = View.GONE
                binding.textViewTitle.text = CremLabFuel.ReaktiveSpisok[groupPosition]?.get(childposition)?.get(13)
                binding.textView2e.setText(CremLabFuel.ReaktiveSpisok[groupPosition]?.get(childposition)?.get(13))
                binding.textView3e.text = CremLabFuel.ReaktiveSpisok[groupPosition]?.get(childposition)?.get(1)
                binding.textView5e.setText(CremLabFuel.ReaktiveSpisok[groupPosition]?.get(childposition)?.get(2))
                binding.textView6e.setText(CremLabFuel.ReaktiveSpisok[groupPosition]?.get(childposition)?.get(3))
                binding.textView7e.setText(CremLabFuel.ReaktiveSpisok[groupPosition]?.get(childposition)?.get(4))
                binding.textView8e.setText(CremLabFuel.ReaktiveSpisok[groupPosition]?.get(childposition)?.get(5))
                binding.textView9e.setText(CremLabFuel.ReaktiveSpisok[groupPosition]?.get(childposition)?.get(6).toString())
                binding.textView10e.setText(CremLabFuel.ReaktiveSpisok[groupPosition]?.get(childposition)?.get(7))
                binding.spinner11e.setSelection(CremLabFuel.ReaktiveSpisok[groupPosition]?.get(childposition)?.get(8)?.toInt() ?: 0)
                binding.textView12e.setText(CremLabFuel.ReaktiveSpisok[groupPosition]?.get(childposition)?.get(9).toString())
                binding.textView13e.setText(CremLabFuel.ReaktiveSpisok[groupPosition]?.get(childposition)?.get(10).toString())
                binding.textView14e.setText(CremLabFuel.ReaktiveSpisok[groupPosition]?.get(childposition)?.get(17).toString())
            }
            binding.textView2e.setSelection(binding.textView2e.text.length)
            binding.textView5e.setSelection(binding.textView5e.text.length)
            binding.textView6e.setSelection(binding.textView6e.text.length)
            binding.textView7e.setSelection(binding.textView7e.text.length)
            binding.textView9e.setSelection(binding.textView9e.text.length)
            binding.textView10e.setSelection(binding.textView10e.text.length)
            binding.textView12e.setSelection(binding.textView12e.text.length)
            binding.textView13e.setSelection(binding.textView13e.text.length)
            binding.textView14e.setSelection(binding.textView14e.text.length)
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
        if (binding.textView6e.text.toString().trim() == "") {
            binding.textView6e.setText(R.string.no)
        }
        if (binding.textView8e.text.toString().trim() == "") {
            val calendar = Calendar.getInstance()
            val month = calendar[Calendar.MONTH] + 1
            var zero = ""
            if (month < 10) zero = "0"
            binding.textView8e.setText(calendar[Calendar.YEAR].toString() + "-" + zero + month)
        }
        if (binding.textView9e.text.toString().trim() == "") {
            binding.textView9e.setText("1")
        }
        if (binding.textView10e.text.toString().trim() == "") {
            binding.textView10e.setText(R.string.obychnye)
        }
        if (binding.textView12e.text.toString().trim() == "") {
            binding.textView12e.setText("0")
        }
        if (binding.textView13e.text.toString().trim() == "") {
            binding.textView13e.setText("0")
        }
        val mDatabase = FirebaseDatabase.getInstance().reference
        val g = Calendar.getInstance() as GregorianCalendar
        var nomerProdukta = groupPosition.toString()
        var nomerPartii = childposition.toString()
        var text9 = java.lang.Long.valueOf(binding.textView9e.text.toString().trim())
        if (add && binding.spinner9.selectedItemPosition == 0) {
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
                                } else if (binding.textView2e.text.toString().trim().contains(name)) {
                                    groupPosition = value1.toInt()
                                    nomerProdukta = groupPosition.toString()
                                }
                            }
                            if (key == 15) {
                                if (binding.textView2e.text.toString().trim().contains(name)) {
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
        mDatabase.child("reagents").child(nomerProdukta).child("name").setValue(binding.textView2e.text.toString().trim())
        mDatabase.child("reagents").child(nomerProdukta).child(nomerPartii).child("data01").setValue(binding.textView3e.text.toString().trim())
        mDatabase.child("reagents").child(nomerProdukta).child(nomerPartii).child("data02").setValue(binding.textView5e.text.toString().trim())
        mDatabase.child("reagents").child(nomerProdukta).child(nomerPartii).child("data03").setValue(binding.textView6e.text.toString().trim())
        mDatabase.child("reagents").child(nomerProdukta).child(nomerPartii).child("data04").setValue(binding.textView7e.text.toString().trim())
        mDatabase.child("reagents").child(nomerProdukta).child(nomerPartii).child("data05").setValue(binding.textView8e.text.toString().trim())
        mDatabase.child("reagents").child(nomerProdukta).child(nomerPartii).child("data06").setValue(text9)
        mDatabase.child("reagents").child(nomerProdukta).child(nomerPartii).child("data07").setValue(binding.textView10e.text.toString().trim())
        mDatabase.child("reagents").child(nomerProdukta).child(nomerPartii).child("data08").setValue(binding.spinner11e.selectedItemPosition.toLong())
        mDatabase.child("reagents").child(nomerProdukta).child(nomerPartii).child("data09").setValue(java.lang.Double.valueOf(binding.textView12e.text.toString().trim().replace(",", ".")))
        mDatabase.child("reagents").child(nomerProdukta).child(nomerPartii).child("data10").setValue(java.lang.Double.valueOf(binding.textView13e.text.toString().trim().replace(",", ".")))
        mDatabase.child("reagents").child(nomerProdukta).child(nomerPartii).child("data12").setValue(binding.textView14e.text.toString().trim())
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
                binding.textViewTitle.text = edit
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
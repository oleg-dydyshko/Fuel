package serhij.korneluk.chemlabfuel

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.firebase.database.FirebaseDatabase
import serhij.korneluk.chemlabfuel.databinding.DialogOpisanieEditBinding
import java.util.*

class DialodOpisanieEdit : DialogFragment() {
    private lateinit var alert: AlertDialog
    private var data8 = ""
    private var uid: String = ""
    private var add = true
    private var size = -1L
    private var user = ""
    private var data9Konservacia: String = ""
    private var data10Razkonservacia: String = ""
    private var data7Oldcheck = ""
    private var data6Periodcheck: String = ""
    private var _binding: DialogOpisanieEditBinding? = null
    private val binding get() = _binding!!

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun setData(textview: Int, year: Int, month: Int, dayOfMonth: Int) {
        var zero = ""
        var zero2 = ""
        if (month < 9) zero = "0"
        if (dayOfMonth < 10) zero2 = "0"
        when (textview) {
            7 -> if (year == 0) binding.textView7e.text = "" else binding.textView7e.text = getString(R.string.set_date, year, zero, month + 1, zero2, dayOfMonth)
            9 -> if (year == 0) binding.textView9e.text = "" else binding.textView9e.text = getString(R.string.set_date, year, zero, month + 1, zero2, dayOfMonth)
            10 -> if (year == 0) binding.textView10e.text = "" else binding.textView10e.text = getString(R.string.set_date, year, zero, month + 1, zero2, dayOfMonth)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        activity?.let { activity ->
            _binding = DialogOpisanieEditBinding.inflate(LayoutInflater.from(activity))
            val builder = AlertDialog.Builder(activity)
            builder.setView(binding.root)
            val g = Calendar.getInstance() as GregorianCalendar
            var zero = ""
            if (g[Calendar.DATE] < 10) zero = "0"
            var zero2 = ""
            if (g[Calendar.MONTH] < 9) zero2 = "0"
            binding.textView7e.hint = g[Calendar.YEAR].toString() + "-" + zero2 + (g[Calendar.MONTH] + 1) + "-" + zero + g[Calendar.DATE]
            binding.textView12e.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    send()
                    return@setOnEditorActionListener true
                }
                false
            }
            binding.button7.setOnClickListener {
                val c = if (binding.textView7e.text.toString() == "") {
                    Calendar.getInstance() as GregorianCalendar
                } else {
                    val t1 = binding.textView7e.text.toString().split("-").toTypedArray()
                    GregorianCalendar(t1[0].toInt(), t1[1].toInt() - 1, t1[2].toInt())
                }
                val data: DialogData = DialogData.getInstance(c.timeInMillis, 7, binding.textView7.text.toString(), 1)
                fragmentManager?.let {
                    data.show(it, "data")
                }
            }
            binding.button9.setOnClickListener {
                val c = if (binding.textView9e.text.toString() == "") {
                    Calendar.getInstance() as GregorianCalendar
                } else {
                    val t1 = binding.textView9e.text.toString().split("-").toTypedArray()
                    GregorianCalendar(t1[0].toInt(), t1[1].toInt() - 1, t1[2].toInt())
                }
                val data: DialogData = DialogData.getInstance(c.timeInMillis, 9, binding.textView9.text.toString(), 1)
                fragmentManager?.let {
                    data.show(it, "data")
                }
            }
            binding.button10.setOnClickListener {
                val c = if (binding.textView10e.text.toString() == "") {
                    Calendar.getInstance() as GregorianCalendar
                } else {
                    val t1 = binding.textView10e.text.toString().split("-").toTypedArray()
                    GregorianCalendar(t1[0].toInt(), t1[1].toInt() - 1, t1[2].toInt())
                }
                val data: DialogData = DialogData.getInstance(c.timeInMillis, 10, binding.textView10.text.toString(), 1)
                fragmentManager?.let {
                    data.show(it, "data")
                }
            }
            user = arguments?.getString("user", "") ?: ""
            size = arguments?.getLong("size", -1L) ?: -1L
            uid = arguments?.getString("uid", "") ?: ""
            data9Konservacia = arguments?.getString("data9", "") ?: ""
            data10Razkonservacia = arguments?.getString("data10", "") ?: ""
            data7Oldcheck = arguments?.getString("data7", "") ?: ""
            data6Periodcheck = arguments?.getString("data6", "") ?: ""
            val data2 = arguments?.getString("data2", "") ?: ""
            val data3 = arguments?.getString("data3", "") ?: ""
            val data4 = arguments?.getString("data4", "") ?: ""
            val data5 = arguments?.getString("data5", "") ?: ""
            val data12 = arguments?.getString("data12", "") ?: ""
            if (size == -1L) add = false
            if (add) binding.textViewTitle.setText(R.string.add) else binding.textViewTitle.text = data2
            binding.textView2e.setText(data2)
            binding.textView3e.setText(data3)
            binding.textView4e.setText(data4)
            binding.textView5e.setText(data5)
            binding.textView6e.setText(data6Periodcheck)
            binding.textView7e.text = data7Oldcheck
            binding.textView9e.text = data9Konservacia
            binding.textView10e.text = data10Razkonservacia
            binding.textView12e.setText(data12)
            binding.textView2e.setSelection(binding.textView2e.text.length)
            binding.textView3e.setSelection(binding.textView3e.text.length)
            binding.textView4e.setSelection(binding.textView4e.text.length)
            binding.textView5e.setSelection(binding.textView5e.text.length)
            binding.textView6e.setSelection(binding.textView6e.text.length)
            binding.textView12e.setSelection(binding.textView12e.text.length)
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

    private fun send() {
        data9Konservacia = binding.textView9e.text.toString().trim()
        data10Razkonservacia = binding.textView10e.text.toString().trim()
        data7Oldcheck = binding.textView7e.text.toString().trim()
        data6Periodcheck = binding.textView6e.text.toString().trim()
        if (!(data7Oldcheck != "" && data7Oldcheck.contains("-"))) {
            val g = Calendar.getInstance() as GregorianCalendar
            var zero = ""
            if (g[Calendar.DATE] < 10) zero = "0"
            var zero2 = ""
            if (g[Calendar.MONTH] < 9) zero2 = "0"
            data7Oldcheck = g[Calendar.YEAR].toString() + "-" + zero2 + (g[Calendar.MONTH] + 1) + "-" + zero + g[Calendar.DATE]
        }
        val c = Calendar.getInstance() as GregorianCalendar
        c.add(Calendar.YEAR, 20)
        var data11 = c.timeInMillis
        if (data6Periodcheck != "") {
            if (data9Konservacia != "") {
                val tk = data9Konservacia.split("-").toTypedArray()
                c[tk[0].toInt(), tk[1].toInt() - 1] = tk[2].toInt()
                val start = c.timeInMillis
                if (data10Razkonservacia != "") {
                    val tr = data10Razkonservacia.split("-").toTypedArray()
                    c[tr[0].toInt(), tr[1].toInt() - 1] = tr[2].toInt()
                    val end = c.timeInMillis
                    if (start > end) {
                        c.timeInMillis = start
                        c.add(Calendar.YEAR, 20)
                        data11 = c.timeInMillis
                        var zero = ""
                        if (c[Calendar.DATE] < 10) zero = "0"
                        var zero2 = ""
                        if (c[Calendar.MONTH] < 9) zero2 = "0"
                        val t1 = data7Oldcheck.split("-").toTypedArray()
                        c[t1[0].toInt(), t1[1].toInt() - 1] = t1[2].toInt()
                        c.add(Calendar.MONTH, data6Periodcheck.toInt())
                        data8 = c[Calendar.YEAR].toString() + "-" + zero2 + (c[Calendar.MONTH] + 1) + "-" + zero + c[Calendar.DATE]
                    } else {
                        val t1 = data7Oldcheck.split("-").toTypedArray()
                        c[t1[0].toInt(), t1[1].toInt() - 1] = t1[2].toInt()
                        c.add(Calendar.MONTH, data6Periodcheck.toInt())
                        var zero = ""
                        if (c[Calendar.DATE] < 10) zero = "0"
                        var zero2 = ""
                        if (c[Calendar.MONTH] < 9) zero2 = "0"
                        data8 = c[Calendar.YEAR].toString() + "-" + zero2 + (c[Calendar.MONTH] + 1) + "-" + zero + c[Calendar.DATE]
                        data11 = c.timeInMillis
                    }
                } else {
                    c.timeInMillis = start
                    c.add(Calendar.YEAR, 20)
                    data11 = c.timeInMillis
                    val t1 = data7Oldcheck.split("-").toTypedArray()
                    c[t1[0].toInt(), t1[1].toInt() - 1] = t1[2].toInt()
                    c.add(Calendar.MONTH, data6Periodcheck.toInt())
                    var zero = ""
                    if (c[Calendar.DATE] < 10) zero = "0"
                    var zero2 = ""
                    if (c[Calendar.MONTH] < 9) zero2 = "0"
                    data8 = c[Calendar.YEAR].toString() + "-" + zero2 + (c[Calendar.MONTH] + 1) + "-" + zero + c[Calendar.DATE]
                }
            } else {
                val t1 = data7Oldcheck.split("-").toTypedArray()
                c[t1[0].toInt(), t1[1].toInt() - 1] = t1[2].toInt()
                c.add(Calendar.MONTH, data6Periodcheck.toInt())
                var zero = ""
                if (c[Calendar.DATE] < 10) zero = "0"
                var zero2 = ""
                if (c[Calendar.MONTH] < 9) zero2 = "0"
                data8 = c[Calendar.YEAR].toString() + "-" + zero2 + (c[Calendar.MONTH] + 1) + "-" + zero + c[Calendar.DATE]
                data11 = c.timeInMillis
            }
        }
        val mDatabase = FirebaseDatabase.getInstance().reference
        val g = Calendar.getInstance() as GregorianCalendar
        if (add) {
            uid = mDatabase.child("equipments").push().key ?: ""
            mDatabase.child("equipments").child(uid).child("createdAt").setValue(g.timeInMillis)
            mDatabase.child("equipments").child(uid).child("createdBy").setValue(user)
            mDatabase.child("equipments").child(uid).child("data01").setValue(size + 1)
        }
        mDatabase.child("equipments").child(uid).child("data02").setValue(binding.textView2e.text.toString().trim())
        mDatabase.child("equipments").child(uid).child("data03").setValue(binding.textView3e.text.toString().trim())
        mDatabase.child("equipments").child(uid).child("data04").setValue(binding.textView4e.text.toString().trim())
        mDatabase.child("equipments").child(uid).child("data05").setValue(binding.textView5e.text.toString().trim())
        mDatabase.child("equipments").child(uid).child("data06").setValue(data6Periodcheck)
        mDatabase.child("equipments").child(uid).child("data07").setValue(data7Oldcheck)
        mDatabase.child("equipments").child(uid).child("data08").setValue(data8)
        mDatabase.child("equipments").child(uid).child("data09").setValue(data9Konservacia)
        mDatabase.child("equipments").child(uid).child("data10").setValue(data10Razkonservacia)
        mDatabase.child("equipments").child(uid).child("data11").setValue(data11)
        mDatabase.child("equipments").child(uid).child("data12").setValue(binding.textView12e.text.toString().trim())
        if (!add) {
            mDatabase.child("equipments").child(uid).child("editedAt").setValue(g.timeInMillis)
            mDatabase.child("equipments").child(uid).child("editedBy").setValue(user)
        }
        activity?.let {
            it.sendBroadcast(Intent(it, ReceiverSetAlarm::class.java))
        }
        dialog?.cancel()
    }

    companion object {
        fun getInstance(user: String?, uid: String?, data2: String?, data3: String?, data4: String?, data5: String?, data6: String?, data7: String?, data8: String?, data9: String?, data10: String?, data12: String?): DialodOpisanieEdit {
            val opisanie = DialodOpisanieEdit()
            val bundle = Bundle()
            bundle.putString("user", user)
            bundle.putString("uid", uid)
            bundle.putString("data2", data2)
            bundle.putString("data3", data3)
            bundle.putString("data4", data4)
            bundle.putString("data5", data5)
            bundle.putString("data6", data6)
            bundle.putString("data7", data7)
            bundle.putString("data8", data8)
            bundle.putString("data9", data9)
            bundle.putString("data10", data10)
            bundle.putString("data12", data12)
            opisanie.arguments = bundle
            return opisanie
        }

        fun getInstance(user: String?, size: Long): DialodOpisanieEdit {
            val opisanie = DialodOpisanieEdit()
            val bundle = Bundle()
            bundle.putString("user", user)
            bundle.putLong("size", size)
            opisanie.arguments = bundle
            return opisanie
        }
    }
}
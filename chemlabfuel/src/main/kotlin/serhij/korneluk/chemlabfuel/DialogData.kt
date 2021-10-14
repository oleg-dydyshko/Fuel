package serhij.korneluk.chemlabfuel

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.CalendarView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import serhij.korneluk.chemlabfuel.databinding.DialogDataBinding
import java.util.*

class DialogData : DialogFragment() {
    private lateinit var alert: AlertDialog
    private lateinit var builder: AlertDialog.Builder
    private var listiner: DialogDataListiner? = null
    private var _binding: DialogDataBinding? = null
    private val binding get() = _binding!!

    internal interface DialogDataListiner {
        fun setData(textview: Int, year: Int, month: Int, dayOfMonth: Int)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Activity) {
            listiner = try {
                context as DialogDataListiner
            } catch (e: ClassCastException) {
                throw ClassCastException("$activity must implement DialogDataListiner")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        activity?.let {
            _binding = DialogDataBinding.inflate(LayoutInflater.from(it))
            binding.title.text = arguments?.getString("title") ?: ""
            binding.calendarView.date = arguments?.getLong("data") ?: 0L
            binding.today.setOnClickListener {
                val c = Calendar.getInstance() as GregorianCalendar
                binding.calendarView.date = c.timeInMillis
            }
            binding.textView1.setOnClickListener {
                val c = GregorianCalendar()
                c.timeInMillis = binding.calendarView.date
                c.add(Calendar.YEAR, -1)
                binding.calendarView.date = c.timeInMillis
            }
            binding.textView2.setOnClickListener {
                val c = GregorianCalendar()
                c.timeInMillis = binding.calendarView.date
                c.add(Calendar.YEAR, 1)
                binding.calendarView.date = c.timeInMillis
            }
            val textview = arguments?.getInt("textview") ?: 0
            binding.calendarView.setOnDateChangeListener { _: CalendarView?, year: Int, month: Int, dayOfMonth: Int ->
                listiner?.setData(textview, year, month, dayOfMonth)
                dialog?.cancel()
            }
            builder = AlertDialog.Builder(it)
            builder.setView(binding.root)
            if (textview == 7 || textview == 9 || textview == 10) {
                builder.setNeutralButton("Удалить дату") { _: DialogInterface?, _: Int ->
                    listiner?.setData(arguments?.getInt("textview") ?: 0, 0, 0, 0)
                }
                builder.setPositiveButton("Отмена") { dialog: DialogInterface, _: Int ->
                    dialog.cancel()
                }
            }
            if (textview == 3 || textview == 1 || textview == 8) {
                builder.setPositiveButton("Отмена") { dialog: DialogInterface, _: Int ->
                    dialog.cancel()
                }
            }
            alert = builder.create()
            alert.setOnShowListener {
                val btnPositive = alert.getButton(Dialog.BUTTON_POSITIVE)
                btnPositive.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                val btnNegative = alert.getButton(Dialog.BUTTON_NEGATIVE)
                btnNegative.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                val btnNeutral = alert.getButton(Dialog.BUTTON_NEUTRAL)
                btnNeutral.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            }
        }
        return alert
    }

    companion object {
        fun getInstance(data: Long, textview: Int, title: String?): DialogData {
            val opisanie = DialogData()
            val bundle = Bundle()
            bundle.putLong("data", data)
            bundle.putInt("textview", textview)
            bundle.putString("title", title)
            opisanie.arguments = bundle
            return opisanie
        }
    }
}
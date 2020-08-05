package serhij.korneluk.chemlabfuel

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_data.*
import java.util.*

class DialogData : DialogFragment() {
    private lateinit var alert: AlertDialog
    private lateinit var builder: AlertDialog.Builder
    private var listiner: DialogDataListiner? = null
    private lateinit var rootView: View
    private var fragment = 1

    internal interface DialogDataListiner {
        fun setData(textview: Int, year: Int, month: Int, dayOfMonth: Int, fragment: Int)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Activity) {
            listiner = try {
                context as DialogDataListiner
            } catch (e: ClassCastException) {
                throw ClassCastException("$context must implement DialogDataListiner")
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        title.text = arguments?.getString("title") ?: ""
        calendarView.date = arguments?.getLong("data") ?: 0L
        fragment = arguments?.getInt("fragment") ?: 1
        today.setOnClickListener {
            val c = Calendar.getInstance() as GregorianCalendar
            calendarView.date = c.timeInMillis
        }
        textView1.setOnClickListener {
            val c = GregorianCalendar()
            c.timeInMillis = calendarView.date
            c.add(Calendar.YEAR, -1)
            calendarView.date = c.timeInMillis
        }
        textView2.setOnClickListener {
            val c = GregorianCalendar()
            c.timeInMillis = calendarView.date
            c.add(Calendar.YEAR, 1)
            calendarView.date = c.timeInMillis
        }
        val textview = arguments?.getInt("textview") ?: 0
        calendarView.setOnDateChangeListener { _: CalendarView?, year: Int, month: Int, dayOfMonth: Int ->
            listiner?.setData(textview, year, month, dayOfMonth, fragment)
            dialog?.cancel()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return rootView
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        activity?.let {
            rootView = View.inflate(it, R.layout.dialog_data, null)
            builder = AlertDialog.Builder(it)
            builder.setView(rootView)
            val textview = arguments?.getInt("textview") ?: 0
            if (textview == 7 || textview == 9 || textview == 10) {
                builder.setNeutralButton("Удалить дату") { _: DialogInterface?, _: Int ->
                    listiner?.setData(arguments?.getInt("textview") ?: 0, 0, 0, 0, fragment)
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
        fun getInstance(data: Long, textview: Int, title: String?, fragment: Int): DialogData {
            val opisanie = DialogData()
            val bundle = Bundle()
            bundle.putLong("data", data)
            bundle.putInt("textview", textview)
            bundle.putString("title", title)
            bundle.putInt("fragment", fragment)
            opisanie.arguments = bundle
            return opisanie
        }
    }
}
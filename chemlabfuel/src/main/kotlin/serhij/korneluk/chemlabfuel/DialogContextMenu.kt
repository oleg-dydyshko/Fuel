package serhij.korneluk.chemlabfuel

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment

class DialogContextMenu : DialogFragment() {
    private lateinit var alert: AlertDialog
    private var mListener: DialogContextMenuListener? = null

    internal interface DialogContextMenuListener {
        fun onDialogEditPosition(position: Int)
        fun onDialogDeliteClick(position: Int)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Activity) {
            mListener = try {
                context as DialogContextMenuListener
            } catch (e: ClassCastException) {
                throw ClassCastException("$activity must implement DialogContextMenuListener")
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        activity?.let {
            val builder = AlertDialog.Builder(it)
            val linearLayout = LinearLayout(it)
            linearLayout.orientation = LinearLayout.VERTICAL
            val textViewZaglavie = TextView(it)
            textViewZaglavie.setBackgroundColor(ContextCompat.getColor(it, R.color.colorPrimary))
            textViewZaglavie.setPadding(10, 10, 10, 10)
            textViewZaglavie.text = arguments?.getString("name", "")?: ""
            textViewZaglavie.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            textViewZaglavie.setTypeface(null, Typeface.BOLD)
            textViewZaglavie.setTextColor(ContextCompat.getColor(it, R.color.colorIcons))
            linearLayout.addView(textViewZaglavie)
            val textView = TextView(it)
            textView.setPadding(10, 20, 10, 20)
            textView.setText(R.string.edit)
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            textView.setTextColor(ContextCompat.getColor(it, R.color.colorPrimary_text))
            textView.setOnClickListener {
                dialog?.cancel()
                mListener?.onDialogEditPosition(arguments?.getInt("position", 0)?: 0)
            }
            linearLayout.addView(textView)
            val textView2 = TextView(it)
            textView2.setPadding(10, 20, 10, 20)
            textView2.setText(R.string.delite)
            textView2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            textView2.setTextColor(ContextCompat.getColor(it, R.color.colorPrimary_text))
            textView2.setOnClickListener {
                dialog?.cancel()
                mListener?.onDialogDeliteClick(arguments?.getInt("position", 0)?: 0)
            }
            linearLayout.addView(textView2)
            builder.setView(linearLayout)
            alert = builder.create()
        }
        return alert
    }

    companion object {
        fun getInstance(position: Int, name: String?): DialogContextMenu {
            val instance = DialogContextMenu()
            val args = Bundle()
            args.putInt("position", position)
            args.putString("name", name)
            instance.arguments = args
            return instance
        }
    }
}
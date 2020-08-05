package serhij.korneluk.chemlabfuel

import android.app.Dialog
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment

class DialogContextMenuReakt : DialogFragment() {
    private lateinit var alert: AlertDialog
    private var mListener: DialogContextMenuReaktListener? = null

    internal interface DialogContextMenuReaktListener {
        fun onDialogAddPartia(groupPosition: Int)
        fun onDialogRashod(groupPosition: Int, childPosition: Int)
        fun onDialogJurnal(groupPosition: Int, childPosition: Int)
        fun onDialogEdit(groupPosition: Int, childPosition: Int)
        fun onDialogRemove(groupPosition: Int, childPosition: Int)
    }

    internal fun setDialogContextMenuReaktListener(mListener: DialogContextMenuReaktListener) {
        this.mListener = mListener
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
            textView.setText(R.string.add_partia)
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            textView.setTextColor(ContextCompat.getColor(it, R.color.colorPrimary_text))
            textView.setOnClickListener {
                dialog?.cancel()
                mListener?.onDialogAddPartia(arguments?.getInt("groupPosition", 0)?: 0)
            }
            linearLayout.addView(textView)
            val textView2 = TextView(it)
            textView2.setPadding(10, 20, 10, 20)
            textView2.setText(R.string.spisanie)
            textView2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            textView2.setTextColor(ContextCompat.getColor(it, R.color.colorPrimary_text))
            textView2.setOnClickListener {
                dialog?.cancel()
                mListener?.onDialogRashod(arguments?.getInt("groupPosition", 0)?: 0, arguments?.getInt("childPosition", 0)?: 0)
            }
            linearLayout.addView(textView2)
            val textView3 = TextView(it)
            textView3.setPadding(10, 20, 10, 20)
            textView3.setText(R.string.gurnal_rasxoda)
            textView3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            textView3.setTextColor(ContextCompat.getColor(it, R.color.colorPrimary_text))
            textView3.setOnClickListener {
                dialog?.cancel()
                mListener?.onDialogJurnal(arguments?.getInt("groupPosition", 0)?: 0, arguments?.getInt("childPosition", 0)?: 0)
            }
            linearLayout.addView(textView3)
            val textView4 = TextView(it)
            textView4.setPadding(10, 20, 10, 20)
            textView4.setText(R.string.edit)
            textView4.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            textView4.setTextColor(ContextCompat.getColor(it, R.color.colorPrimary_text))
            textView4.setOnClickListener {
                dialog?.cancel()
                mListener?.onDialogEdit(arguments?.getInt("groupPosition", 0)?: 0, arguments?.getInt("childPosition", 0)?: 0)
            }
            linearLayout.addView(textView4)
            val textView5 = TextView(it)
            textView5.setPadding(10, 20, 10, 20)
            textView5.setText(R.string.delite)
            textView5.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            textView5.setTextColor(ContextCompat.getColor(it, R.color.colorPrimary_text))
            textView5.setOnClickListener {
                dialog?.cancel()
                mListener?.onDialogRemove(arguments?.getInt("groupPosition", 0)?: 0, arguments?.getInt("childPosition", 0)?: 0)
            }
            linearLayout.addView(textView5)
            builder.setView(linearLayout)
            alert = builder.create()
        }
        return alert
    }

    companion object {
        fun getInstance(groupPosition: Int, childPosition: Int, name: String?): DialogContextMenuReakt {
            val instance = DialogContextMenuReakt()
            val args = Bundle()
            args.putInt("groupPosition", groupPosition)
            args.putInt("childPosition", childPosition)
            args.putString("name", name)
            instance.arguments = args
            return instance
        }
    }
}
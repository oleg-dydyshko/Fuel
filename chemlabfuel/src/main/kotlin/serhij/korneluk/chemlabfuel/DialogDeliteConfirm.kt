package serhij.korneluk.chemlabfuel

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment

class DialogDeliteConfirm : DialogFragment() {
    private lateinit var alert: AlertDialog
    private var listiner: DialogDeliteConfirmlistiner? = null

    internal interface DialogDeliteConfirmlistiner {
        fun deliteData(groupPosition: Int, position: Int)
        fun deliteData(position: Int)
    }

    internal fun setDialogDeliteConfirmlistiner(listiner: DialogDeliteConfirmlistiner) {
        this.listiner = listiner
    }

    /*override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Activity) {
            listiner = try {
                context as DialogDeliteConfirmlistiner
            } catch (e: ClassCastException) {
                throw ClassCastException("$context must implement DialogDeliteConfirmlistiner")
            }
        }
    }*/

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        activity?.let {
            val ad = AlertDialog.Builder(it)
            val linearLayout = LinearLayout(it)
            linearLayout.orientation = LinearLayout.VERTICAL
            val textViewZaglavie = TextView(it)
            textViewZaglavie.setBackgroundColor(ContextCompat.getColor(it, R.color.colorPrimary))
            textViewZaglavie.setPadding(10, 10, 10, 10)
            textViewZaglavie.setText(R.string.remove)
            textViewZaglavie.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            textViewZaglavie.setTypeface(null, Typeface.BOLD)
            textViewZaglavie.setTextColor(ContextCompat.getColor(it, R.color.colorIcons))
            linearLayout.addView(textViewZaglavie)
            val textView = TextView(it)
            textView.setPadding(10, 10, 10, 10)
            textView.text = getString(R.string.remove_conform, arguments?.getString("title") ?: "")
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            textView.setTextColor(ContextCompat.getColor(it, R.color.colorPrimary_text))
            linearLayout.addView(textView)
            ad.setView(linearLayout)
            ad.setPositiveButton(getString(R.string.delite)) { _: DialogInterface?, _: Int ->
                val groupPosition = arguments?.getInt("groupPosition", -1) ?: -1
                val position = arguments?.getInt("position") ?: 0
                if (groupPosition == -1)
                    listiner?.deliteData(position)
                else
                    listiner?.deliteData(groupPosition, position)
            }
            ad.setNegativeButton(getString(R.string.cansel)) { dialog: DialogInterface, _: Int -> dialog.cancel() }
            alert = ad.create()
            alert.setOnShowListener {
                val btnPositive = alert.getButton(Dialog.BUTTON_POSITIVE)
                btnPositive.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                val btnNegative = alert.getButton(Dialog.BUTTON_NEGATIVE)
                btnNegative.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            }
        }
        return alert
    }

    companion object {
        fun getInstance(title: String?, groupPosition: Int, position: Int): DialogDeliteConfirm {
            val opisanie = DialogDeliteConfirm()
            val bundle = Bundle()
            bundle.putString("title", title)
            bundle.putInt("groupPosition", groupPosition)
            bundle.putInt("position", position)
            opisanie.arguments = bundle
            return opisanie
        }
    }
}
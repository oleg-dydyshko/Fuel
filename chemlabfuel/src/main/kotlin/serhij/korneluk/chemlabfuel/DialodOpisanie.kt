package serhij.korneluk.chemlabfuel

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.LinearLayout.VERTICAL
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment

class DialodOpisanie : DialogFragment() {
    private lateinit var alert:AlertDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        activity?.let {
            val builder = AlertDialog.Builder(it)
            val scrollView = ScrollView(it)
            val linearLayout = LinearLayout(it)
            linearLayout.orientation = VERTICAL
            val textViewZaglavie = TextView(it)
            textViewZaglavie.setBackgroundColor(ContextCompat.getColor(it, R.color.colorPrimary))
            textViewZaglavie.setPadding(10, 10, 10, 10)
            textViewZaglavie.text = arguments?.getString("title")?: ""
            textViewZaglavie.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            textViewZaglavie.setTypeface(null, Typeface.BOLD)
            textViewZaglavie.setTextColor(ContextCompat.getColor(it, R.color.colorIcons))
            linearLayout.addView(textViewZaglavie)
            val textView = TextView(it)
            textView.setPadding(10, 10, 10, 10)
            textView.text = ChemLabFuel.fromHtml(arguments?.getString("string")?: "")
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            textView.setTextColor(ContextCompat.getColor(it, R.color.colorPrimary_text))
            linearLayout.addView(scrollView)
            val linearLayout1 = LinearLayout(it)
            linearLayout1.orientation = VERTICAL
            linearLayout1.addView(textView)
            scrollView.addView(linearLayout1)
            builder.setPositiveButton(getString(R.string.good)) { dialog: DialogInterface, _: Int -> dialog.cancel() }
            builder.setView(linearLayout)
            alert = builder.create()
            alert.setOnShowListener {
                val btnPositive = alert.getButton(Dialog.BUTTON_POSITIVE)
                btnPositive.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            }
        }
        return alert
    }

    companion object {
        fun getInstance(title: String?, string: String?): DialodOpisanie {
            val opisanie = DialodOpisanie()
            val bundle = Bundle()
            bundle.putString("title", title)
            bundle.putString("string", string)
            opisanie.arguments = bundle
            return opisanie
        }
    }
}
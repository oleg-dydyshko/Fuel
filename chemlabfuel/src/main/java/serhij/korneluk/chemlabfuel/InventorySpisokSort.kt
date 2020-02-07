package serhij.korneluk.chemlabfuel

import android.content.Context
import android.content.SharedPreferences
import java.util.*

class InventorySpisokSort internal constructor(context: Context) : Comparator<InventorySpisok> {
    private val fuel: SharedPreferences = context.getSharedPreferences("fuel", Context.MODE_PRIVATE)
    override fun compare(o1: InventorySpisok, o2: InventorySpisok): Int {
        val sort = fuel.getInt("sort", 0)
        if (sort == 1) return o1.data02?.toLowerCase(Locale.getDefault())?.compareTo(o2.data02?.toLowerCase(Locale.getDefault())?: "")?: 0
        if (sort == 2) {
            if (o1.data11 < o2.data11) {
                return -1
            } else if (o1.data11 > o2.data11) {
                return 1
            }
            return 0
        }
        if (sort == 0) {
            var zero = ""
            if (o1.data01 < 10) zero = "0"
            var zeroO = ""
            if (o2.data01 < 10) zeroO = "0"
            return (zero + o1.data01 + ". " + o1.data02?.toLowerCase(Locale.getDefault())).compareTo(zeroO + o2.data01 + ". " + o2.data02?.toLowerCase(Locale.getDefault()))
        }
        return 0
    }
}
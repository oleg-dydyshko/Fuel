package serhij.korneluk.chemlabfuel

import android.content.Context
import android.content.SharedPreferences
import java.util.*

class ReaktiveSpisokSort internal constructor(context: Context) : Comparator<ReaktiveSpisok> {
    private val fuel: SharedPreferences = context.getSharedPreferences("fuel", Context.MODE_PRIVATE)
    override fun compare(o1: ReaktiveSpisok, o2: ReaktiveSpisok): Int {
        val sort = fuel.getInt("sort", 0)
        if (sort == 1) return o1.string.toLowerCase(Locale.getDefault()).compareTo(o2.string.toLowerCase(Locale.getDefault()))
        if (sort == 2) {
            if (o1.data < o2.data) {
                return -1
            } else if (o1.data > o2.data) {
                return 1
            }
            return 0
        }
        if (sort == 0) {
            var zero = ""
            if (o1.id < 10) zero = "0"
            var zeroO = ""
            if (o2.id < 10) zeroO = "0"
            return (zero + o1.id + ". " + o1.string.toLowerCase(Locale.getDefault())).compareTo(zeroO + o2.id + ". " + o2.string.toLowerCase(Locale.getDefault()))
        }
        return 0
    }
}
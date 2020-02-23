package serhij.korneluk.chemlabfuel

import android.content.Context
import android.content.SharedPreferences
import java.util.*

class InventorySpisok(val context: Context, val createdBy: String?, val data01: Long, val data02: String?, val data03: String?, val data04: String?, val data05: String?, val data06: String?, val data07: String?, val data08: String?, val data09: String?, val data10: String?, val data11: Long, val data12: String?, val uid: String?, val editedAt: Long, val editedBy: String?) : Comparable<InventorySpisok> {
    private val fuel: SharedPreferences = context.getSharedPreferences("fuel", Context.MODE_PRIVATE)
    override fun compareTo(other: InventorySpisok): Int {
        val sort = fuel.getInt("sort", 0)
        if (sort == 1) return this.data02?.toLowerCase(Locale.getDefault())?.compareTo(other.data02?.toLowerCase(Locale.getDefault())?: "")?: 0
        if (sort == 2) {
            if (this.data11 < other.data11) {
                return -1
            } else if (this.data11 > other.data11) {
                return 1
            }
            return 0
        }
        if (sort == 0) {
            var zero = ""
            if (this.data01 < 10) zero = "0"
            var zeroO = ""
            if (other.data01 < 10) zeroO = "0"
            return (zero + this.data01 + ". " + this.data02?.toLowerCase(Locale.getDefault())).compareTo(zeroO + other.data01 + ". " + other.data02?.toLowerCase(Locale.getDefault()))
        }
        return 0
    }
}
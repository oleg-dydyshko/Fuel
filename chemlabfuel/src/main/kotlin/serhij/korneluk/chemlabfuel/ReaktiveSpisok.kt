package serhij.korneluk.chemlabfuel

import android.content.Context
import android.content.SharedPreferences
import java.math.BigDecimal
import java.util.*

class ReaktiveSpisok : Comparable<ReaktiveSpisok> {
    val data: Long
    val id: Int
    val string: String
    val ostatok: BigDecimal?
    val minostatok: BigDecimal?
    val edIzmerenia: Int
    val check: Int
    val arrayList: ArrayList<String>?
    private val fuel: SharedPreferences

    constructor(context: Context, data: Long, id: Int, string: String, ostatok: BigDecimal?, minostatok: BigDecimal?, ed_izmerenia: Int, arrayList: ArrayList<String>?) {
        this.data = data
        this.id = id
        this.string = string
        this.ostatok = ostatok
        this.minostatok = minostatok
        this.edIzmerenia = ed_izmerenia
        check = 1
        this.arrayList = arrayList
        fuel = context.getSharedPreferences("fuel", Context.MODE_PRIVATE)
    }

    constructor(context: Context, data: Long, id: Int, check: Int) {
        this.data = data
        this.id = id
        string = ""
        ostatok = null
        minostatok = null
        edIzmerenia = 0
        this.check = check
        arrayList = null
        fuel = context.getSharedPreferences("fuel", Context.MODE_PRIVATE)
    }

    override fun compareTo(other: ReaktiveSpisok): Int {
        val sort = fuel.getInt("sort", 0)
        if (sort == 1) return this.string.toLowerCase(Locale.getDefault()).compareTo(other.string.toLowerCase(Locale.getDefault()))
        if (sort == 2) {
            if (this.data < other.data) {
                return -1
            } else if (this.data > other.data) {
                return 1
            }
            return 0
        }
        if (sort == 0) {
            var zero = ""
            if (this.id < 10) zero = "0"
            var zeroO = ""
            if (other.id < 10) zeroO = "0"
            return (zero + this.id + ". " + this.string.toLowerCase(Locale.getDefault())).compareTo(zeroO + other.id + ". " + other.string.toLowerCase(Locale.getDefault()))
        }
        return 0
    }
}
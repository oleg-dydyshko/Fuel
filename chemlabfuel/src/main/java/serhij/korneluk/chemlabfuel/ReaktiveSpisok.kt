package serhij.korneluk.chemlabfuel

import java.math.BigDecimal
import java.util.*

class ReaktiveSpisok {
    val data: Long
    val id: Int
    val string: String
    val ostatok: BigDecimal?
    val minostatok: BigDecimal?
    val edIzmerenia: Int
    val check: Int
    val arrayList: ArrayList<String>?

    constructor(data: Long, id: Int, string: String, ostatok: BigDecimal?, minostatok: BigDecimal?, ed_izmerenia: Int, arrayList: ArrayList<String>?) {
        this.data = data
        this.id = id
        this.string = string
        this.ostatok = ostatok
        this.minostatok = minostatok
        this.edIzmerenia = ed_izmerenia
        check = 1
        this.arrayList = arrayList
    }

    constructor(data: Long, id: Int, check: Int) {
        this.data = data
        this.id = id
        string = ""
        ostatok = null
        minostatok = null
        edIzmerenia = 0
        this.check = check
        arrayList = null
    }
}
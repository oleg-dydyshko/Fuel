package serhij.korneluk.chemlabfuel

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.util.TypedValue
import android.view.*
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ExpandableListView.OnChildClickListener
import android.widget.TabHost.TabSpec
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.cremlabfuel.*
import serhij.korneluk.chemlabfuel.DialodOpisanieEditReakt.ListUpdateListiner
import serhij.korneluk.chemlabfuel.DialodReaktRasxod.UpdateJurnal
import serhij.korneluk.chemlabfuel.DialogContextMenu.DialogContextMenuListener
import serhij.korneluk.chemlabfuel.DialogContextMenuReakt.DialogContextMenuReaktListener
import serhij.korneluk.chemlabfuel.DialogData.DialogDataListiner
import serhij.korneluk.chemlabfuel.DialogDeliteConfirm.DialogDeliteConfirmlistiner
import java.math.BigDecimal
import java.util.*

class CremLabFuel : AppCompatActivity(), OnItemClickListener, OnItemLongClickListener, DialogContextMenuListener, DialogDeliteConfirmlistiner, DialogDataListiner, ListUpdateListiner, OnChildClickListener, UpdateJurnal, DialogContextMenuReaktListener {
    private var inventarnySpisok: ArrayList<InventorySpisok> = ArrayList()
    private lateinit var arrayAdapter: ListAdapter
    private lateinit var arrayAdapter2: ListAdapterReakt
    private var edit: DialodOpisanieEdit? = null
    private var editReakt: DialodOpisanieEditReakt? = null
    private var rasxod: DialodReaktRasxod? = null
    private var jurnal: DialogJurnal? = null
    private var userEdit = ""
    private lateinit var mAuth: FirebaseAuth
    private val spisokGroup = ArrayList<ReaktiveSpisok>()
    private val edIzmerenia = arrayOf("кг.", "мг.", "л.", "мл.")

    override fun onDialogAddPartia(groupPosition: Int) {
        editReakt = DialodOpisanieEditReakt.getInstance(userEdit, spisokGroup[groupPosition].string, spisokGroup[groupPosition].minostatok.toString(), spisokGroup[groupPosition].edIzmerenia)
        editReakt?.show(supportFragmentManager, "edit")
        edit = null
        rasxod = null
        jurnal = null
    }

    override fun onDialogRashod(groupPosition: Int, childPosition: Int) {
        val arrayList = seash(groupPosition, childPosition)
        rasxod = DialodReaktRasxod.getInstance(arrayList[14].toInt(), arrayList[15].toInt(), arrayList[8].toInt(), userEdit)
        rasxod?.show(supportFragmentManager, "rasxod")
        edit = null
        editReakt = null
        jurnal = null
    }

    override fun onDialogJurnal(groupPosition: Int, childPosition: Int) {
        val arrayList = seash(groupPosition, childPosition)
        jurnal = DialogJurnal.getInstance(arrayList[14].toInt(), arrayList[15].toInt(), arrayList[8].toInt(), arrayList[16], userEdit)
        jurnal?.show(supportFragmentManager, "jurnal")
        edit = null
        editReakt = null
        rasxod = null
    }

    override fun onDialogEdit(groupPosition: Int, childPosition: Int) {
        val arrayList = seash(groupPosition, childPosition)
        editReakt = DialodOpisanieEditReakt.getInstance(userEdit, arrayList[14].toInt(), arrayList[15].toInt())
        editReakt?.show(supportFragmentManager, "edit")
        edit = null
        rasxod = null
        jurnal = null
    }

    override fun onDialogRemove(groupPosition: Int, childPosition: Int) {
        var spisokGroupSt = spisokGroup[groupPosition].arrayList?.get(childPosition) ?: ""
        val t1 = spisokGroupSt.indexOf(" <")
        if (t1 != -1) spisokGroupSt = spisokGroupSt.substring(0, t1)
        val confirm: DialogDeliteConfirm = DialogDeliteConfirm.getInstance(spisokGroup[groupPosition].string + " " + spisokGroupSt, groupPosition, childPosition)
        confirm.show(supportFragmentManager, "confirm")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cremlabfuel)
        val fuel = getSharedPreferences("fuel", Context.MODE_PRIVATE)
        mAuth = FirebaseAuth.getInstance()
        arrayAdapter = ListAdapter()
        listView.adapter = arrayAdapter
        listView.onItemClickListener = this
        listView.onItemLongClickListener = this
        arrayAdapter2 = ListAdapterReakt()
        listView2.setAdapter(arrayAdapter2)
        listView2.setOnChildClickListener(this)
        listView2.setOnItemLongClickListener { _: AdapterView<*>?, _: View?, i: Int, _: Long ->
            val packedPosition = listView2.getExpandableListPosition(i)
            val itemType = ExpandableListView.getPackedPositionType(packedPosition)
            val groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition)
            val childPosition = ExpandableListView.getPackedPositionChild(packedPosition)
            if (itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                val arrayList = seash(groupPosition, childPosition)
                val reakt: DialogContextMenuReakt = DialogContextMenuReakt.getInstance(groupPosition, childPosition, ReaktiveSpisok[arrayList[14].toInt()]?.get(arrayList[15].toInt())?.get(13))
                reakt.show(supportFragmentManager, "reakt")
                return@setOnItemLongClickListener true
            }
            false
        }
        tabhost.setup()
        var tabSpec: TabSpec = tabhost.newTabSpec("tag1")
        tabSpec.setIndicator(getString(R.string.oborudovanie))
        tabSpec.setContent(R.id.tab1)
        tabhost.addTab(tabSpec)
        tabSpec = tabhost.newTabSpec("tag2")
        tabSpec.setIndicator(getString(R.string.reaktivy))
        tabSpec.setContent(R.id.tab2)
        tabhost.addTab(tabSpec)
        tabhost.setOnTabChangedListener { tabId: String ->
            val editor = fuel.edit()
            if (tabId.contains("tag1")) editor.putBoolean("oborudovanie", true) else editor.putBoolean("oborudovanie", false)
            editor.apply()
            invalidateOptionsMenu()
        }
        if (fuel.getBoolean("oborudovanie", true)) {
            tabhost.setCurrentTabByTag("tag1")
        } else {
            tabhost.setCurrentTabByTag("tag2")
        }
        setTollbarTheme()
        updateUI()
    }

    private fun setTollbarTheme() {
        title_toolbar.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        setSupportActionBar(toolbar)
        title_toolbar.setText(R.string.app_main)
    }

    public override fun onResume() {
        super.onResume()
        overridePendingTransition(R.anim.alphain, R.anim.alphaout)
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        if (parent.id == R.id.listView) {
            var fn: String? = ""
            var ln: String? = ""
            var fnG: String? = ""
            var lnG: String? = ""
            var zero = ""
            var zero2 = ""
            var editedString = ""
            if (inventarnySpisok[position].editedBy != "") {
                val edited = inventarnySpisok[position].editedAt
                val editedBy = inventarnySpisok[position].editedBy
                for (i in users.indices) {
                    if (users[i][0].contains(editedBy ?: "")) {
                        fn = users[i][1]
                        ln = users[i][2]
                        break
                    }
                }
                val c = GregorianCalendar()
                c.timeInMillis = edited
                if (c[Calendar.DATE] < 10) zero = "0"
                if (c[Calendar.MONTH] < 9) zero2 = "0"
                editedString = " Изменено " + zero + c[Calendar.DATE] + "." + zero2 + (c[Calendar.MONTH] + 1) + "." + c[Calendar.YEAR] + " " + fn + " " + ln
            }
            val createBy = inventarnySpisok[position].createdBy
            for (i in users.indices) {
                if (users[i][0].contains(createBy ?: "")) {
                    fnG = users[i][1]
                    lnG = users[i][2]
                    break
                }
            }
            val data02 = inventarnySpisok[position].data02
            val builder = "<strong>Марка, тип</strong><br>" + inventarnySpisok[position].data03 + "<br><br>" +
                    "<strong>Заводской номер (инв. номер)</strong><br>" + inventarnySpisok[position].data04 + "<br><br>" +
                    "<strong>Год выпуска (ввода в эксплуатацию)</strong><br>" + inventarnySpisok[position].data05 + "<br><br>" +
                    "<strong>Периодичность метролог. аттестации, поверки, калибровки, мес.</strong><br>" + inventarnySpisok[position].data06 + "<br><br>" +
                    "<strong>Дата последней аттестации, поверки, калибровки</strong><br>" + inventarnySpisok[position].data07 + "<br><br>" +
                    "<strong>Дата следующей аттестации, поверки, калибровки</strong><br>" + inventarnySpisok[position].data08 + "<br><br>" +
                    "<strong>Дата консервации</strong><br>" + inventarnySpisok[position].data09 + "<br><br>" +
                    "<strong>Дата расконсервации</strong><br>" + inventarnySpisok[position].data10 + "<br><br>" +
                    "<strong>Ответственный</strong><br>" + fnG + " " + lnG + "<br><br>" +
                    "<strong>Примечания</strong><br>" + inventarnySpisok[position].data12 + editedString
            val opisanie: DialodOpisanie = DialodOpisanie.getInstance(data02, builder)
            opisanie.show(supportFragmentManager, "opisanie")
        }
    }

    private fun seash(groupPosition: Int, childPosition: Int): ArrayList<String> {
        val group = spisokGroup[groupPosition].string
        val t1 = spisokGroup[groupPosition].arrayList?.get(childPosition)?.indexOf(".") ?: 0
        val child = spisokGroup[groupPosition].arrayList?.get(childPosition)?.substring(0, t1) ?: ""
        val arrayList = ArrayList<String>()
        var end = false
        for ((key, value) in ReaktiveSpisok) {
            for ((_, value2) in value) {
                arrayList.clear()
                for ((key1, value1) in value2) {
                    if (key1 == 0) arrayList.add(value1)
                    if (key1 == 1) arrayList.add(value1)
                    if (key1 == 2) arrayList.add(value1)
                    if (key1 == 3) arrayList.add(value1)
                    if (key1 == 4) arrayList.add(value1)
                    if (key1 == 5) arrayList.add(value1)
                    if (key1 == 6) arrayList.add(value1)
                    if (key1 == 7) arrayList.add(value1)
                    if (key1 == 8) arrayList.add(value1)
                    if (key1 == 9) arrayList.add(value1)
                    if (key1 == 10) arrayList.add(value1)
                    if (key1 == 11) arrayList.add(value1)
                    if (key1 == 12) arrayList.add(value1)
                    if (key1 == 13) arrayList.add(value1)
                    if (key1 == 14) arrayList.add(value1)
                    if (key1 == 15) arrayList.add(value1)
                    if (key1 == 16) arrayList.add(value1)
                    if (key1 == 17) arrayList.add(value1)
                }
                if (group.contains(arrayList[13]) && child.contains(arrayList[15])) {
                    arrayList.add(key.toString())
                    end = true
                    break
                }
            }
            if (end) break
        }
        return arrayList
    }

    override fun onChildClick(parent: ExpandableListView, v: View, groupPosition: Int, childPosition: Int, id: Long): Boolean {
        val arrayList = seash(groupPosition, childPosition)
        var fn: String? = ""
        var ln: String? = ""
        var fnG: String? = ""
        var lnG: String? = ""
        var zero = ""
        var zero2 = ""
        var editedString = ""
        if (arrayList[12] != "") {
            val edited = arrayList[11].toLong()
            val editedBy = arrayList[12]
            for (i in users.indices) {
                if (users[i][0].contains(editedBy)) {
                    fn = users[i][1]
                    ln = users[i][2]
                    break
                }
            }
            val c = GregorianCalendar()
            c.timeInMillis = edited
            if (c[Calendar.DATE] < 10) zero = "0"
            if (c[Calendar.MONTH] < 9) zero2 = "0"
            editedString = zero + c[Calendar.DATE] + "." + zero2 + (c[Calendar.MONTH] + 1) + "." + c[Calendar.YEAR] + " " + fn + " " + ln
        }
        val createBy = arrayList[0]
        for (i in users.indices) {
            if (users[i][0].contains(createBy)) {
                fnG = users[i][1]
                lnG = users[i][2]
                break
            }
        }
        val izmerenie = arrayOf("Килограмм", "Миллиграмм", "Литры", "Миллилитры")
        val data02 = arrayList[13]
        val builder = "<strong>Партия</strong><br>" + arrayList[15] + "<br><br>" +
                "<strong>Дата получения</strong><br>" + arrayList[1] + "<br><br>" +
                "<strong>Поставщик</strong><br>" + arrayList[2] + "<br><br>" +
                "<strong>Притензии</strong><br>" + arrayList[3] + "<br><br>" +
                "<strong>Квалификация</strong><br>" + arrayList[4] + "<br><br>" +
                "<strong>Партия</strong><br>" + arrayList[17] + "<br><br>" +
                "<strong>Дата изготовления</strong><br>" + arrayList[5] + "<br><br>" +
                "<strong>Срок хранения</strong><br>" + arrayList[6] + "<br><br>" +
                "<strong>Условия хранения</strong><br>" + arrayList[7] + "<br><br>" +
                "<strong>Единица измерения</strong><br>" + izmerenie[arrayList[8].toInt()] + "<br><br>" +
                "<strong>Количество на остатке</strong><br>" + arrayList[9].replace(".", ",") + "<br><br>" +
                "<strong>Минимальное количество</strong><br>" + arrayList[10].replace(".", ",") + "<br><br>" +
                "<strong>Ответственный</strong><br>" + fnG + " " + lnG + "<br><br>" +
                "<strong>Изменено</strong><br>" + editedString
        val opisanie: DialodOpisanie = DialodOpisanie.getInstance(data02, builder)
        opisanie.show(supportFragmentManager, "opisanie")
        return false
    }

    override fun onItemLongClick(parent: AdapterView<*>?, view: View, position: Int, id: Long): Boolean {
        val menu: DialogContextMenu = DialogContextMenu.getInstance(position, inventarnySpisok[position].data02)
        menu.show(supportFragmentManager, "menu")
        return true
    }

    override fun updateList() {
        arrayAdapter2.notifyDataSetChanged()
    }

    override fun updateJurnalRasxoda(position: Int, t0: String, t1: String, t2: String, t3: String, t4: String, t5: String) {
        if (jurnal != null) jurnal?.updateJurnalRasxoda(position, t0, t1, t2, t3, t4, t5)
    }

    override fun onDialogEditPosition(position: Int) {
        if (isNetworkAvailable(this)) {
            edit = DialodOpisanieEdit.getInstance(userEdit, inventarnySpisok[position].uid, inventarnySpisok[position].data02, inventarnySpisok[position].data03, inventarnySpisok[position].data04, inventarnySpisok[position].data05, inventarnySpisok[position].data06, inventarnySpisok[position].data07, inventarnySpisok[position].data08, inventarnySpisok[position].data09, inventarnySpisok[position].data10, inventarnySpisok[position].data12)
            edit?.show(supportFragmentManager, "edit")
        } else {
            val internet = DialogNoInternet()
            internet.show(supportFragmentManager, "internet")
        }
    }

    override fun onDialogDeliteClick(position: Int) {
        if (isNetworkAvailable(this)) {
            val confirm: DialogDeliteConfirm = DialogDeliteConfirm.getInstance(inventarnySpisok[position].data02, -1, position)
            confirm.show(supportFragmentManager, "confirm")
        } else {
            val internet = DialogNoInternet()
            internet.show(supportFragmentManager, "internet")
        }
    }

    override fun deliteData(groupPosition: Int, position: Int) {
        val mDatabase = FirebaseDatabase.getInstance().reference
        if (groupPosition == -1) {
            inventarnySpisok[position].uid?.let { mDatabase.child("equipments").child(it).removeValue() }
        } else {
            val arrayList = seash(groupPosition, position)
            if (ReaktiveSpisok[arrayList[18].toInt()]?.size == 1) mDatabase.child("reagents").child(arrayList[14]).removeValue() else mDatabase.child("reagents").child(arrayList[14]).child(arrayList[15]).removeValue()
        }
    }

    override fun setData(textview: Int, year: Int, month: Int, dayOfMonth: Int) {
        edit?.setData(textview, year, month, dayOfMonth)
        editReakt?.setData(textview, year, month, dayOfMonth)
        rasxod?.setData(textview, year, month, dayOfMonth)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        val currentUser = mAuth.currentUser
        menu.findItem(R.id.exit).isVisible = currentUser != null
        val fuel = getSharedPreferences("fuel", Context.MODE_PRIVATE)
        val sort = fuel.getInt("sort", 0)
        if (sort == 0) {
            menu.findItem(R.id.sortdate).isChecked = false
            menu.findItem(R.id.sorttime).isChecked = false
        }
        if (sort == 1) {
            menu.findItem(R.id.sortdate).isChecked = true
            menu.findItem(R.id.sorttime).isChecked = false
        }
        if (sort == 2) {
            menu.findItem(R.id.sorttime).isChecked = true
            menu.findItem(R.id.sortdate).isChecked = false
        }
        if (fuel.getBoolean("oborudovanie", true)) {
            menu.findItem(R.id.add).isVisible = true
            menu.findItem(R.id.add_reakt).isVisible = false
        } else {
            menu.findItem(R.id.add_reakt).isVisible = true
            menu.findItem(R.id.add).isVisible = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.add) {
            if (isNetworkAvailable(this)) {
                edit = DialodOpisanieEdit.getInstance(userEdit, inventarnySpisok.size.toLong())
                edit?.show(supportFragmentManager, "edit")
                editReakt = null
                rasxod = null
            } else {
                val internet = DialogNoInternet()
                internet.show(supportFragmentManager, "internet")
            }
        }
        if (id == R.id.add_reakt) {
            if (isNetworkAvailable(this)) {
                editReakt = DialodOpisanieEditReakt.getInstance(userEdit, "", "", 0)
                editReakt?.show(supportFragmentManager, "edit")
                edit = null
                rasxod = null
            } else {
                val internet = DialogNoInternet()
                internet.show(supportFragmentManager, "internet")
            }
        }
        if (id == R.id.exit) {
            mAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        if (id == R.id.sortdate) {
            val fuel = getSharedPreferences("fuel", Context.MODE_PRIVATE)
            val editor = fuel.edit()
            if (item.isChecked) {
                editor.putInt("sort", 0)
                editor.apply()
                inventarnySpisok.sort()
                spisokGroup.sort()
            } else {
                editor.putInt("sort", 1)
                editor.apply()
                inventarnySpisok.sort()
                spisokGroup.sort()
            }
            arrayAdapter.notifyDataSetChanged()
            arrayAdapter2.notifyDataSetChanged()
            invalidateOptionsMenu()
        }
        if (id == R.id.sorttime) {
            val fuel = getSharedPreferences("fuel", Context.MODE_PRIVATE)
            val editor = fuel.edit()
            if (item.isChecked) {
                editor.putInt("sort", 0)
                editor.apply()
                inventarnySpisok.sort()
                spisokGroup.sort()
            } else {
                editor.putInt("sort", 2)
                editor.apply()
                inventarnySpisok.sort()
                spisokGroup.sort()
            }
            arrayAdapter.notifyDataSetChanged()
            arrayAdapter2.notifyDataSetChanged()
            invalidateOptionsMenu()
        }
        if (id == R.id.settings) {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateUI() {
        loading.visibility = View.VISIBLE
        invalidateOptionsMenu()
        if (isNetworkAvailable(this)) {
            val fuel = getSharedPreferences("fuel", Context.MODE_PRIVATE)
            val mDatabase = FirebaseDatabase.getInstance().reference
            mDatabase.child("equipments").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    inventarnySpisok.clear()
                    for (data in dataSnapshot.children) {
                        if (data.value is HashMap<*, *>) {
                            val hashMap = data.value as HashMap<*, *>
                            if (hashMap.size > 12) {
                                var editedAt = hashMap["editedAt"]
                                var editedBy = hashMap["editedBy"]
                                if (hashMap["editedAt"] == null) editedAt = 0L
                                if (hashMap["editedBy"] == null) editedBy = ""
                                inventarnySpisok.add(InventorySpisok(this@CremLabFuel, hashMap["createdBy"] as String?, hashMap["data01"] as Long, hashMap["data02"] as String?, hashMap["data03"] as String?, hashMap["data04"] as String?, hashMap["data05"] as String?, hashMap["data06"] as String?, hashMap["data07"] as String?, hashMap["data08"] as String?, hashMap["data09"] as String?, hashMap["data10"] as String?, hashMap["data11"] as Long, hashMap["data12"] as String?, data.key, editedAt as Long, editedBy as String?))
                            }
                        }
                    }
                    inventarnySpisok.sort()
                    arrayAdapter.notifyDataSetChanged()
                    loading.visibility = View.GONE
                    sendBroadcast(Intent(this@CremLabFuel, ReceiverSetAlarm::class.java))
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
            mDatabase.child("reagents").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    ReaktiveSpisok.clear()
                    spisokGroup.clear()
                    val gson = Gson()
                    for (data in dataSnapshot.children) {
                        val name = data.child("name").value as String?
                        val id = data.key?: ""
                        var ostatokSum = BigDecimal.ZERO
                        var minostatok = BigDecimal.ZERO
                        val spisokN = LinkedHashMap<Int, LinkedHashMap<Int, String>>()
                        val spisokChild = ArrayList<String>()
                        val g = Calendar.getInstance() as GregorianCalendar
                        val srokToDay = g.timeInMillis
                        var data05b: Long = 0
                        var data08 = 0
                        for (data2 in data.children) {
                            var srok = ""
                            var i = 0
                            if (data2.value is HashMap<*, *>) {
                                val hashMap = data2.value as HashMap<*, *>
                                if (hashMap.size >= 12) {
                                    val spisoks = LinkedHashMap<Int, String>()
                                    var editedAt = data2.child("editedAt").value
                                    var editedBy = data2.child("editedBy").value
                                    if (editedAt == null) editedAt = 0L
                                    if (editedBy == null) editedBy = ""
                                    var data11 = data2.child("data11").value
                                    if (data11 == null) data11 = ArrayList<Any>()
                                    var data12 = data2.child("data12").value
                                    if (data12 == null) data12 = ""
                                    spisoks[i] = data2.child("createdBy").value as String //0
                                    i++
                                    spisoks[i] = data2.child("data01").value as String //1
                                    i++
                                    spisoks[i] = data2.child("data02").value as String //2
                                    i++
                                    spisoks[i] = data2.child("data03").value as String //3
                                    i++
                                    spisoks[i] = data2.child("data04").value as String //4
                                    i++
                                    spisoks[i] = data2.child("data05").value as String //5
                                    i++
                                    spisoks[i] = data2.child("data06").value.toString() //6
                                    i++
                                    spisoks[i] = data2.child("data07").value as String //7
                                    i++
                                    spisoks[i] = data2.child("data08").value.toString() //8
                                    i++
                                    spisoks[i] = data2.child("data09").value.toString() //9
                                    i++
                                    spisoks[i] = data2.child("data10").value.toString() //10
                                    i++
                                    spisoks[i] = editedAt.toString() //11
                                    i++
                                    spisoks[i] = editedBy as String //12
                                    i++
                                    spisoks[i] = name?: "" //13
                                    i++
                                    spisoks[i] = id //14
                                    i++
                                    spisoks[i] = data2.key?: "" //15
                                    i++
                                    spisoks[i] = gson.toJson(data11) //16
                                    i++
                                    spisoks[i] = data12.toString() //17
                                    data2.key?.let {
                                        spisokN[it.toInt()] = spisoks
                                    }
                                    val data05 = data2.child("data05").value as String
                                    val d = data05.split("-").toTypedArray()
                                    if (d.size == 3) g[d[0].toInt(), d[1].toInt() - 1] = d[2].toInt() else g[d[0].toInt(), d[1].toInt() - 1] = 1
                                    data05b = g.timeInMillis
                                    g.add(Calendar.MONTH, (data2.child("data06").value as Long).toInt())
                                    val ostatok: BigDecimal = if (data2.child("data09").value is Double) BigDecimal.valueOf(data2.child("data09").value as Double) else BigDecimal.valueOf((data2.child("data09").value as Long).toDouble())
                                    if (srokToDay < g.timeInMillis) {
                                        ostatokSum = ostatokSum.add(ostatok)
                                        g.add(Calendar.DATE, -45)
                                        if (srokToDay > g.timeInMillis) srok = " <font color=#9a2828>Истекает срок</font>"
                                    } else {
                                        srok = " <font color=#9a2828>Срок истёк</font>"
                                    }
                                    minostatok = if (data2.child("data10").value is Double) BigDecimal.valueOf(data2.child("data10").value as Double) else BigDecimal.valueOf((data2.child("data10").value as Long).toDouble())
                                    data08 = (data2.child("data08").value as Long).toInt()
                                    spisokChild.add(data2.key + "." + srok + " <!---->Остаток: " + ostatok.toString().replace(".", ",") + " " + edIzmerenia[data08])
                                }
                            }
                        }
                        spisokGroup.add(ReaktiveSpisok(this@CremLabFuel, data05b, id.toInt(), name?: "", ostatokSum, minostatok, data08, spisokChild))
                        ReaktiveSpisok[id.toInt()] = spisokN
                    }
                    if (intent.extras?.getBoolean("reaktive", false) == true) {
                        tabhost.setCurrentTabByTag("tag2")
                        for (i in 0 until arrayAdapter2.groupCount) {
                            listView2.expandGroup(i)
                        }
                    }
                    spisokGroup.sort()
                    arrayAdapter2.notifyDataSetChanged()
                    loading.visibility = View.GONE
                    sendBroadcast(Intent(this@CremLabFuel, ReceiverSetAlarm::class.java))
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
            mDatabase.child("users").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (data in dataSnapshot.children) {
                        val key = data.key?: ""
                        if (mAuth.uid?.contains(key) == true) {
                            userEdit = key
                        }
                        for (data2 in data.children) {
                            if (data2.value is HashMap<*, *>) {
                                val hashMap = data2.value as HashMap<*, *>
                                val firstname = hashMap["firstName"] as String
                                val lastname = hashMap["lastName"] as String
                                val user = ArrayList<String>()
                                user.add(key)
                                user.add(firstname)
                                user.add(lastname)
                                users.add(user)
                            }
                        }
                    }
                    val gson = Gson()
                    val editor = fuel.edit()
                    editor.putString("users", gson.toJson(users))
                    editor.apply()
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        } else {
            val fuel = getSharedPreferences("fuel", Context.MODE_PRIVATE)
            val g = fuel.getString("fuel_data", "")
            if (g != "") {
                val gson = Gson()
                val type = object : TypeToken<Array<InventorySpisok?>?>() {}.type
                inventarnySpisok = gson.fromJson<ArrayList<InventorySpisok>>(g, type)
                val us = fuel.getString("users", "")
                val type2 = object : TypeToken<ArrayList<ArrayList<String?>?>?>() {}.type
                users.addAll(gson.fromJson<Collection<ArrayList<String>>>(us, type2))
                arrayAdapter.notifyDataSetChanged()
                val intent = Intent(this@CremLabFuel, ReceiverSetAlarm::class.java)
                sendBroadcast(intent)
            } else {
                val internet = DialogNoInternet()
                internet.show(supportFragmentManager, "internet")
            }
            loading.visibility = View.GONE
        }
    }

    private inner class ListAdapter internal constructor() : ArrayAdapter<InventorySpisok>(this@CremLabFuel, R.layout.simple_list_item, inventarnySpisok) {
        private val fuel: SharedPreferences = getSharedPreferences("fuel", Context.MODE_PRIVATE)
        override fun getView(position: Int, mView: View?, parent: ViewGroup): View {
            val root: View
            val viewHolder: ViewHolder
            if (mView == null) {
                val vi = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                root = vi.inflate(R.layout.simple_list_item, parent, false)
                viewHolder = ViewHolder()
                root.tag = viewHolder
                viewHolder.text = root.findViewById(R.id.label)
                viewHolder.buttonPopup = root.findViewById(R.id.button_popup)
            } else {
                root = mView
                viewHolder = root.tag as ViewHolder
            }
            viewHolder.buttonPopup?.setOnClickListener { showPopupMenu(viewHolder.buttonPopup, position) }
            val g = Calendar.getInstance() as GregorianCalendar
            val real = g.timeInMillis
            var dataLong = ""
            val data8 = inventarnySpisok[position].data08
            if (data8 != null && data8 != "") {
                val t1 = data8.split("-").toTypedArray()
                val calendar = GregorianCalendar()
                val data09 = inventarnySpisok[position].data09
                val data10 = inventarnySpisok[position].data10
                if (data09 != null && data09 != "") {
                    val t2 = data09.split("-").toTypedArray()
                    calendar[t2[0].toInt(), t2[1].toInt() - 1] = t2[2].toInt()
                    val t2l = calendar.timeInMillis
                    if (data10 != null && data10 != "") {
                        val t3 = data10.split("-").toTypedArray()
                        calendar[t3[0].toInt(), t3[1].toInt() - 1] = t3[2].toInt()
                        val t3l = calendar.timeInMillis
                        if (t2l < t3l) {
                            g[t1[0].toInt(), t1[1].toInt() - 1] = t1[2].toInt()
                            if (g.timeInMillis - real >= 0L && g.timeInMillis - real < 45L * 24L * 60L * 60L * 1000L) {
                                val dat = g.timeInMillis - real
                                g.timeInMillis = dat
                                dataLong = "<br><font color=#9a2828>Осталось " + (g[Calendar.DAY_OF_YEAR] - 1) + " дней(-я)</font>"
                            } else if (g.timeInMillis - real < 0L) {
                                dataLong = "<br><font color=#9a2828>Просрочено</font>"
                            }
                        }
                    }
                } else {
                    g[t1[0].toInt(), t1[1].toInt() - 1] = t1[2].toInt()
                    if (g.timeInMillis - real >= 0L && g.timeInMillis - real < 45L * 24L * 60L * 60L * 1000L) {
                        val dat = g.timeInMillis - real
                        g.timeInMillis = dat
                        dataLong = "<br><font color=#9a2828>Осталось " + (g[Calendar.DAY_OF_YEAR] - 1) + " дней(-я)</font>"
                    } else if (g.timeInMillis - real < 0L) {
                        dataLong = "<br><font color=#9a2828>Просрочено</font>"
                    }
                }
            }
            viewHolder.text?.text = fromHtml(inventarnySpisok[position].data01.toString() + ". " + inventarnySpisok[position].data02 + dataLong)
            viewHolder.text?.textSize = fuel.getInt("fontsize", 18).toFloat()
            return root
        }

        private fun showPopupMenu(view: View?, position: Int) {
            val popup = PopupMenu(this@CremLabFuel, view)
            val infl = popup.menuInflater
            infl.inflate(R.menu.popup, popup.menu)
            for (i in 0 until popup.menu.size()) {
                val item = popup.menu.getItem(i)
                val spanString = SpannableString(popup.menu.getItem(i).title.toString())
                val end = spanString.length
                spanString.setSpan(AbsoluteSizeSpan(18, true), 0, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                item.title = spanString
            }
            popup.setOnMenuItemClickListener { menuItem: MenuItem ->
                popup.dismiss()
                if (menuItem.itemId == R.id.menu_redoktor) {
                    onDialogEditPosition(position)
                    return@setOnMenuItemClickListener true
                }
                if (menuItem.itemId == R.id.menu_remove) {
                    onDialogDeliteClick(position)
                    return@setOnMenuItemClickListener true
                }
                false
            }
            popup.show()
        }

    }

    private inner class ListAdapterReakt internal constructor() : BaseExpandableListAdapter() {
        private val fuel: SharedPreferences = getSharedPreferences("fuel", Context.MODE_PRIVATE)
        override fun getGroupCount(): Int {
            return spisokGroup.size
        }

        override fun getChildrenCount(groupPosition: Int): Int {
            return spisokGroup[groupPosition].arrayList?.size?: 0
        }

        override fun getGroup(groupPosition: Int): Any {
            return spisokGroup[groupPosition]
        }

        override fun getChild(groupPosition: Int, childPosition: Int): Any {
            return spisokGroup[groupPosition].arrayList?.get(childPosition)?: ""
        }

        override fun getGroupId(groupPosition: Int): Long {
            return groupPosition.toLong()
        }

        override fun getChildId(groupPosition: Int, childPosition: Int): Long {
            return childPosition.toLong()
        }

        override fun hasStableIds(): Boolean {
            return true
        }

        override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
            return true
        }

        override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup): View {
            val root: View
            val group: ViewHolderGroup
            if (convertView == null) {
                val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                root = inflater.inflate(R.layout.group_view, parent, false)
                group = ViewHolderGroup()
                root.tag = group
                group.text = root.findViewById(R.id.textGroup)
            } else {
                root = convertView
                group = root.tag as ViewHolderGroup
            }
            group.text?.textSize = fuel.getInt("fontsize", 18).toFloat()
            var ostatok = " (Остаток: " + spisokGroup[groupPosition].ostatok.toString().replace(".", ",") + " " + edIzmerenia[spisokGroup[groupPosition].edIzmerenia] + ")"
            val compare = spisokGroup[groupPosition].ostatok?.compareTo(spisokGroup[groupPosition].minostatok)
            if (spisokGroup[groupPosition].ostatok == BigDecimal.ZERO) ostatok = " <font color=#9a2828>Срок истёк</font>" else if (compare != null && compare <= 0) ostatok = " (<font color=#9a2828>Остаток: " + spisokGroup[groupPosition].ostatok.toString().replace(".", ",") + " " + edIzmerenia[spisokGroup[groupPosition].edIzmerenia] + "</font>)"
            group.text?.text = fromHtml(spisokGroup[groupPosition].id.toString() + ". " + spisokGroup[groupPosition].string + ostatok)
            return root
        }

        override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup): View {
            val root: View
            val viewHolder: ViewHolder
            if (convertView == null) {
                val vi = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                root = vi.inflate(R.layout.simple_list_item3, parent, false)
                viewHolder = ViewHolder()
                root.tag = viewHolder
                viewHolder.text = root.findViewById(R.id.label)
                viewHolder.buttonPopup = root.findViewById(R.id.button_popup)
            } else {
                root = convertView
                viewHolder = root.tag as ViewHolder
            }
            viewHolder.buttonPopup?.setOnClickListener { showPopupMenu(viewHolder.buttonPopup, groupPosition, childPosition) }
            viewHolder.text?.text = fromHtml(spisokGroup[groupPosition].arrayList?.get(childPosition)?: "")
            viewHolder.text?.textSize = fuel.getInt("fontsize", 18).toFloat()
            return root
        }

        private fun showPopupMenu(view: View?, groupPosition: Int, childposition: Int) {
            val popup = PopupMenu(this@CremLabFuel, view)
            val infl = popup.menuInflater
            infl.inflate(R.menu.popup_reaktive, popup.menu)
            for (i in 0 until popup.menu.size()) {
                val item = popup.menu.getItem(i)
                val spanString = SpannableString(popup.menu.getItem(i).title.toString())
                val end = spanString.length
                spanString.setSpan(AbsoluteSizeSpan(18, true), 0, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                item.title = spanString
            }
            popup.setOnMenuItemClickListener { menuItem: MenuItem ->
                popup.dismiss()
                if (menuItem.itemId == R.id.menu_add) {
                    editReakt = DialodOpisanieEditReakt.getInstance(userEdit, spisokGroup[groupPosition].string, spisokGroup[groupPosition].minostatok.toString(), spisokGroup[groupPosition].edIzmerenia)
                    editReakt?.show(supportFragmentManager, "edit")
                    edit = null
                    rasxod = null
                    jurnal = null
                    return@setOnMenuItemClickListener true
                }
                if (menuItem.itemId == R.id.menu_minus) {
                    val arrayList = seash(groupPosition, childposition)
                    rasxod = DialodReaktRasxod.getInstance(arrayList[14].toInt(), arrayList[15].toInt(), arrayList[8].toInt(), userEdit)
                    rasxod?.show(supportFragmentManager, "rasxod")
                    edit = null
                    editReakt = null
                    jurnal = null
                }
                if (menuItem.itemId == R.id.menu_jurnal) {
                    val arrayList = seash(groupPosition, childposition)
                    jurnal = DialogJurnal.getInstance(arrayList[14].toInt(), arrayList[15].toInt(), arrayList[8].toInt(), arrayList[16], userEdit)
                    jurnal?.show(supportFragmentManager, "jurnal")
                    edit = null
                    editReakt = null
                    rasxod = null
                }
                if (menuItem.itemId == R.id.menu_redoktor) {
                    val arrayList = seash(groupPosition, childposition)
                    editReakt = DialodOpisanieEditReakt.getInstance(userEdit, arrayList[14].toInt(), arrayList[15].toInt())
                    editReakt?.show(supportFragmentManager, "edit")
                    edit = null
                    rasxod = null
                    jurnal = null
                    return@setOnMenuItemClickListener true
                }
                if (menuItem.itemId == R.id.menu_remove) {
                    var spisokGroupSt = spisokGroup[groupPosition].arrayList?.get(childposition)?: ""
                    val t1 = spisokGroupSt.indexOf(" <")
                    if (t1 != -1) spisokGroupSt = spisokGroupSt.substring(0, t1)
                    val confirm: DialogDeliteConfirm = DialogDeliteConfirm.getInstance(spisokGroup[groupPosition].string + " " + spisokGroupSt, groupPosition, childposition)
                    confirm.show(supportFragmentManager, "confirm")
                    return@setOnMenuItemClickListener true
                }
                false
            }
            popup.show()
        }

    }

    private class ViewHolderGroup {
        var text: TextView? = null
    }

    private class ViewHolder {
        var text: TextView? = null
        var buttonPopup: ImageView? = null
    }

    companion object {
        val users = ArrayList<ArrayList<String>>()
        val ReaktiveSpisok = LinkedHashMap<Int, LinkedHashMap<Int, LinkedHashMap<Int, String>>>()

        @Suppress("DEPRECATION")
        fun fromHtml(html: String): Spanned {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
            } else {
                Html.fromHtml(html)
            }
        }

        @Suppress("DEPRECATION")
        fun isNetworkAvailable(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val nw = connectivityManager.activeNetwork ?: return false
                val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
                return when {
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    else -> false
                }
            } else {
                val activeNetworkInfo = connectivityManager.activeNetworkInfo ?: return false
                return activeNetworkInfo.isConnectedOrConnecting
            }
        }
    }
}
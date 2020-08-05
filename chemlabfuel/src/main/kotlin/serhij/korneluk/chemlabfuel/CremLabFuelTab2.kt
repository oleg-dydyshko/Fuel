package serhij.korneluk.chemlabfuel

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import kotlinx.android.synthetic.main.cremlabfuel_tab2.*
import java.math.BigDecimal
import java.util.*

class CremLabFuelTab2 : Fragment(), ExpandableListView.OnChildClickListener, DialogContextMenuReakt.DialogContextMenuReaktListener, DialodOpisanieEditReakt.ListUpdateListiner, DialodReaktRasxod.UpdateJurnal, DialogDeliteConfirm.DialogDeliteConfirmlistiner {

    private var edit: DialodOpisanieEdit? = null
    private var rasxod: DialodReaktRasxod? = null
    private var jurnal: DialogJurnal? = null
    private var editReakt: DialodOpisanieEditReakt? = null
    private val spisokGroup = ArrayList<ReaktiveSpisok>()
    private lateinit var arrayAdapter2: ListAdapterReakt
    private val edIzmerenia = arrayOf("кг.", "мг.", "л.", "мл.")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.cremlabfuel_tab2, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.let { activity ->
            arrayAdapter2 = ListAdapterReakt(activity)
            listView2.setAdapter(arrayAdapter2)
            listView2.setOnChildClickListener(this)
            listView2.setOnItemLongClickListener { _: AdapterView<*>?, _: View?, i: Int, _: Long ->
                val packedPosition = listView2.getExpandableListPosition(i)
                val itemType = ExpandableListView.getPackedPositionType(packedPosition)
                val groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition)
                val childPosition = ExpandableListView.getPackedPositionChild(packedPosition)
                if (itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    fragmentManager?.let {
                        val arrayList = seash(groupPosition, childPosition)
                        val reakt: DialogContextMenuReakt = DialogContextMenuReakt.getInstance(groupPosition, childPosition, CremLabFuel.ReaktiveSpisok[arrayList[14].toInt()]?.get(arrayList[15].toInt())?.get(13))
                        reakt.setDialogContextMenuReaktListener(this)
                        reakt.show(it, "reakt")
                    }
                    return@setOnItemLongClickListener true
                }
                false
            }
        }
        updateUI()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        activity?.let {
            val fuel = it.getSharedPreferences("fuel", Context.MODE_PRIVATE)
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
        }
    }

    fun updateSort() {
        spisokGroup.sort()
        arrayAdapter2.notifyDataSetChanged()
        activity?.invalidateOptionsMenu()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        activity?.let { activity ->
            val id = item.itemId
            if (id == R.id.add_reakt) {
                fragmentManager?.let {
                    if (CremLabFuel.isNetworkAvailable(activity)) {
                        editReakt = DialodOpisanieEditReakt.getInstance(CremLabFuel.userEdit, "", "", 0)
                        editReakt?.setListUpdateListiner(this)
                        editReakt?.show(it, "edit")
                        edit = null
                        rasxod = null
                    } else {
                        val internet = DialogNoInternet()
                        internet.show(it, "internet")
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun deliteData( position: Int, groupPosition: Int) {
        val mDatabase = FirebaseDatabase.getInstance().reference
        val arrayList = seash(groupPosition, position)
        if (CremLabFuel.ReaktiveSpisok[arrayList[18].toInt()]?.size == 1) mDatabase.child("reagents").child(arrayList[14]).removeValue() else mDatabase.child("reagents").child(arrayList[14]).child(arrayList[15]).removeValue()
    }

    override fun onDialogAddPartia(groupPosition: Int) {
        fragmentManager?.let {
            editReakt = DialodOpisanieEditReakt.getInstance(CremLabFuel.userEdit, spisokGroup[groupPosition].string, spisokGroup[groupPosition].minostatok.toString(), spisokGroup[groupPosition].edIzmerenia)
            editReakt?.setListUpdateListiner(this)
            editReakt?.show(it, "edit")
            edit = null
            rasxod = null
            jurnal = null
        }
    }

    override fun onDialogRashod(groupPosition: Int, childPosition: Int) {
        fragmentManager?.let {
            val arrayList = seash(groupPosition, childPosition)
            rasxod = DialodReaktRasxod.getInstance(arrayList[14].toInt(), arrayList[15].toInt(), arrayList[8].toInt(), CremLabFuel.userEdit)
            rasxod?.setUpdateJurnal(this)
            rasxod?.show(it, "rasxod")
            edit = null
            editReakt = null
            jurnal = null
        }
    }

    override fun onDialogJurnal(groupPosition: Int, childPosition: Int) {
        fragmentManager?.let {
            val arrayList = seash(groupPosition, childPosition)
            jurnal = DialogJurnal.getInstance(arrayList[14].toInt(), arrayList[15].toInt(), arrayList[8].toInt(), arrayList[16], CremLabFuel.userEdit)
            jurnal?.show(it, "jurnal")
            edit = null
            editReakt = null
            rasxod = null
        }
    }

    override fun onDialogEdit(groupPosition: Int, childPosition: Int) {
        fragmentManager?.let {
            val arrayList = seash(groupPosition, childPosition)
            editReakt = DialodOpisanieEditReakt.getInstance(CremLabFuel.userEdit, arrayList[14].toInt(), arrayList[15].toInt())
            editReakt?.setListUpdateListiner(this)
            editReakt?.show(it, "edit")
            edit = null
            rasxod = null
            jurnal = null
        }
    }

    override fun onDialogRemove(groupPosition: Int, childPosition: Int) {
        fragmentManager?.let {
            var spisokGroupSt = spisokGroup[groupPosition].arrayList?.get(childPosition) ?: ""
            val t1 = spisokGroupSt.indexOf(" <")
            if (t1 != -1) spisokGroupSt = spisokGroupSt.substring(0, t1)
            val confirm: DialogDeliteConfirm = DialogDeliteConfirm.getInstance(spisokGroup[groupPosition].string + " " + spisokGroupSt, groupPosition, childPosition)
            confirm.setDialogDeliteConfirmlistiner(this)
            confirm.show(it, "confirm")
        }
    }

    fun setData(textview: Int, year: Int, month: Int, dayOfMonth: Int) {
        edit?.setData(textview, year, month, dayOfMonth)
        editReakt?.setData(textview, year, month, dayOfMonth)
        rasxod?.setData(textview, year, month, dayOfMonth)
    }

    override fun updateJurnalRasxoda(position: Int, t0: String, t1: String, t2: String, t3: String, t4: String, t5: String) {
        if (jurnal != null) jurnal?.updateJurnalRasxoda(position, t0, t1, t2, t3, t4, t5)
    }

    private fun seash(groupPosition: Int, childPosition: Int): ArrayList<String> {
        val group = spisokGroup[groupPosition].string
        val t1 = spisokGroup[groupPosition].arrayList?.get(childPosition)?.indexOf(".") ?: 0
        val child = spisokGroup[groupPosition].arrayList?.get(childPosition)?.substring(0, t1) ?: ""
        val arrayList = ArrayList<String>()
        var end = false
        for ((key, value) in CremLabFuel.ReaktiveSpisok) {
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

    override fun updateList() {
        arrayAdapter2.notifyDataSetChanged()
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
            for (i in CremLabFuel.users.indices) {
                if (CremLabFuel.users[i][0].contains(editedBy)) {
                    fn = CremLabFuel.users[i][1]
                    ln = CremLabFuel.users[i][2]
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
        for (i in CremLabFuel.users.indices) {
            if (CremLabFuel.users[i][0].contains(createBy)) {
                fnG = CremLabFuel.users[i][1]
                lnG = CremLabFuel.users[i][2]
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
        fragmentManager?.let {
            val opisanie: DialodOpisanie = DialodOpisanie.getInstance(data02, builder)
            opisanie.show(it, "opisanie")
        }
        return false
    }

    private fun updateUI() {
        activity?.let { activity ->
            loading.visibility = View.VISIBLE
            activity.invalidateOptionsMenu()
            if (CremLabFuel.isNetworkAvailable(activity)) {
                val mDatabase = FirebaseDatabase.getInstance().reference
                mDatabase.child("reagents").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        CremLabFuel.ReaktiveSpisok.clear()
                        spisokGroup.clear()
                        val gson = Gson()
                        for (data in dataSnapshot.children) {
                            val name = data.child("name").value as String?
                            val id = data.key ?: ""
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
                                        spisoks[i] = name ?: "" //13
                                        i++
                                        spisoks[i] = id //14
                                        i++
                                        spisoks[i] = data2.key ?: "" //15
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
                            spisokGroup.add(ReaktiveSpisok(activity, data05b, id.toInt(), name
                                    ?: "", ostatokSum, minostatok, data08, spisokChild))
                            CremLabFuel.ReaktiveSpisok[id.toInt()] = spisokN
                        }
                        spisokGroup.sort()
                        arrayAdapter2.notifyDataSetChanged()
                        loading.visibility = View.GONE
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
            } else {
                fragmentManager?.let {
                    val internet = DialogNoInternet()
                    internet.show(it, "internet")
                }
                loading.visibility = View.GONE
            }
        }
    }

    fun setExpandGroup() {
        for (i in 0 until arrayAdapter2.groupCount) {
            listView2.expandGroup(i)
        }
    }

    private inner class ListAdapterReakt(myContext: Context) : BaseExpandableListAdapter() {
        private val mContext = myContext
        private val fuel: SharedPreferences = mContext.getSharedPreferences("fuel", Context.MODE_PRIVATE)
        override fun getGroupCount(): Int {
            return spisokGroup.size
        }

        override fun getChildrenCount(groupPosition: Int): Int {
            return spisokGroup[groupPosition].arrayList?.size ?: 0
        }

        override fun getGroup(groupPosition: Int): Any {
            return spisokGroup[groupPosition]
        }

        override fun getChild(groupPosition: Int, childPosition: Int): Any {
            return spisokGroup[groupPosition].arrayList?.get(childPosition) ?: ""
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
                val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
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
            group.text?.text = CremLabFuel.fromHtml(spisokGroup[groupPosition].id.toString() + ". " + spisokGroup[groupPosition].string + ostatok)
            return root
        }

        override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup): View {
            val root: View
            val viewHolder: ViewHolder
            if (convertView == null) {
                val vi = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
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
            viewHolder.text?.text = CremLabFuel.fromHtml(spisokGroup[groupPosition].arrayList?.get(childPosition)
                    ?: "")
            viewHolder.text?.textSize = fuel.getInt("fontsize", 18).toFloat()
            return root
        }

        private fun showPopupMenu(view: View?, groupPosition: Int, childposition: Int) {
            val popup = PopupMenu(mContext, view)
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
                fragmentManager?.let {
                    if (menuItem.itemId == R.id.menu_add) {
                        editReakt = DialodOpisanieEditReakt.getInstance(CremLabFuel.userEdit, spisokGroup[groupPosition].string, spisokGroup[groupPosition].minostatok.toString(), spisokGroup[groupPosition].edIzmerenia)
                        editReakt?.setListUpdateListiner(this@CremLabFuelTab2)
                        editReakt?.show(it, "edit")
                        edit = null
                        rasxod = null
                        jurnal = null
                        return@setOnMenuItemClickListener true
                    }
                    if (menuItem.itemId == R.id.menu_minus) {
                        val arrayList = seash(groupPosition, childposition)
                        rasxod = DialodReaktRasxod.getInstance(arrayList[14].toInt(), arrayList[15].toInt(), arrayList[8].toInt(), CremLabFuel.userEdit)
                        rasxod?.setUpdateJurnal(this@CremLabFuelTab2)
                        rasxod?.show(it, "rasxod")
                        edit = null
                        editReakt = null
                        jurnal = null
                    }
                    if (menuItem.itemId == R.id.menu_jurnal) {
                        val arrayList = seash(groupPosition, childposition)
                        jurnal = DialogJurnal.getInstance(arrayList[14].toInt(), arrayList[15].toInt(), arrayList[8].toInt(), arrayList[16], CremLabFuel.userEdit)
                        jurnal?.show(it, "jurnal")
                        edit = null
                        editReakt = null
                        rasxod = null
                    }
                    if (menuItem.itemId == R.id.menu_redoktor) {
                        val arrayList = seash(groupPosition, childposition)
                        editReakt = DialodOpisanieEditReakt.getInstance(CremLabFuel.userEdit, arrayList[14].toInt(), arrayList[15].toInt())
                        editReakt?.setListUpdateListiner(this@CremLabFuelTab2)
                        editReakt?.show(it, "edit")
                        edit = null
                        rasxod = null
                        jurnal = null
                        return@setOnMenuItemClickListener true
                    }
                    if (menuItem.itemId == R.id.menu_remove) {
                        var spisokGroupSt = spisokGroup[groupPosition].arrayList?.get(childposition)
                                ?: ""
                        val t1 = spisokGroupSt.indexOf(" <")
                        if (t1 != -1) spisokGroupSt = spisokGroupSt.substring(0, t1)
                        val confirm: DialogDeliteConfirm = DialogDeliteConfirm.getInstance(spisokGroup[groupPosition].string + " " + spisokGroupSt, groupPosition, childposition)
                        confirm.setDialogDeliteConfirmlistiner(this@CremLabFuelTab2)
                        confirm.show(it, "confirm")
                        return@setOnMenuItemClickListener true
                    }
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
}
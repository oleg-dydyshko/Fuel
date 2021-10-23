package serhij.korneluk.chemlabfuel

import android.content.Context
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
import serhij.korneluk.chemlabfuel.databinding.CremlabfuelTab2Binding
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

class ChemLabFuelTab2 : Fragment(), ExpandableListView.OnChildClickListener, DialodOpisanieEditReakt.ListUpdateListiner, DialogDeliteConfirm.DialogDeliteConfirmlistiner {

    private val spisokGroup = ArrayList<ReaktiveSpisok>()
    private lateinit var arrayAdapter2: ListAdapterReakt
    private val edIzmerenia: Array<out String>
        get() = ChemLabFuelApp.applicationContext().resources.getStringArray(R.array.izmerenie_smoll)
    private var _binding: CremlabfuelTab2Binding? = null
    private val binding get() = _binding!!
    private var progressBarTab2Listener: ProgressBarTab2Listener? = null

    interface ProgressBarTab2Listener {
        fun onProgress(visibility: Int)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        progressBarTab2Listener?.onProgress(View.GONE)
        _binding = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = CremlabfuelTab2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let { activity ->
            arrayAdapter2 = ListAdapterReakt(activity)
            binding.listView2.setAdapter(arrayAdapter2)
            binding.listView2.setOnChildClickListener(this)
            binding.listView2.setOnItemLongClickListener { _: AdapterView<*>?, _: View?, i: Int, _: Long ->
                val packedPosition = binding.listView2.getExpandableListPosition(i)
                val itemType = ExpandableListView.getPackedPositionType(packedPosition)
                val groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition)
                val childPosition = ExpandableListView.getPackedPositionChild(packedPosition)
                if (itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    val arrayList = seash(groupPosition, childPosition)
                    val reakt: DialogContextMenuReakt = DialogContextMenuReakt.getInstance(groupPosition, childPosition, ChemLabFuel.ReaktiveSpisok[arrayList[14].toInt()]?.get(arrayList[15].toInt())?.get(13))
                    reakt.show(childFragmentManager, "reakt")
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
        activity?.let {
            val id = item.itemId
            if (id == R.id.add_reakt) {
                if (ChemLabFuel.isNetworkAvailable()) {
                    val editReakt = DialodOpisanieEditReakt.getInstance(ChemLabFuel.userEdit, "", "", 0, 1)
                    editReakt.setListUpdateListiner(this)
                    editReakt.show(childFragmentManager, "edit")
                } else {
                    val internet = DialogNoInternet()
                    internet.show(childFragmentManager, "internet")
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun deliteData(position: Int, groupPosition: Int) {
        val mDatabase = FirebaseDatabase.getInstance(ChemLabFuelApp.getApp()).reference
        val arrayList = seash(groupPosition, position)
        if (ChemLabFuel.ReaktiveSpisok[arrayList[19].toInt()]?.size == 1) mDatabase.child("reagents").child(arrayList[14]).removeValue() else mDatabase.child("reagents").child(arrayList[14]).child(arrayList[15]).removeValue()
    }

    fun onDialogAddPartia(groupPosition: Int) {
        val arrayList = seash(groupPosition)
        val editReakt = DialodOpisanieEditReakt.getInstance(ChemLabFuel.userEdit, spisokGroup[groupPosition].string, spisokGroup[groupPosition].minostatok.toString(), spisokGroup[groupPosition].edIzmerenia, arrayList[14].toInt())
        editReakt.setListUpdateListiner(this)
        editReakt.show(childFragmentManager, "edit")
    }

    fun onDialogRashod(groupPosition: Int, childPosition: Int) {
        val arrayList = seash(groupPosition, childPosition)
        val rasxod = DialodReaktRasxod.getInstance(arrayList[14].toInt(), arrayList[15].toInt(), arrayList[8].toInt(), ChemLabFuel.userEdit, arrayList[9])
        rasxod.show(childFragmentManager, "rasxod")
    }

    fun onDialogJurnal(groupPosition: Int, childPosition: Int) {
        val arrayList = seash(groupPosition, childPosition)
        val jurnal = DialogJurnal.getInstance(arrayList[14].toInt(), arrayList[15].toInt(), arrayList[8].toInt(), arrayList[16], ChemLabFuel.userEdit, arrayList[9])
        jurnal.show(childFragmentManager, "jurnal")
    }

    fun onDialogEdit(groupPosition: Int, childPosition: Int) {
        val arrayList = seash(groupPosition, childPosition)
        val editReakt = DialodOpisanieEditReakt.getInstance(ChemLabFuel.userEdit, arrayList[14].toInt(), arrayList[15].toInt())
        editReakt.setListUpdateListiner(this)
        editReakt.show(childFragmentManager, "edit")
    }

    fun onDialogRemove(groupPosition: Int, childPosition: Int) {
        var spisokGroupSt = spisokGroup[groupPosition].arrayList[childPosition]
        val t1 = spisokGroupSt.indexOf(" <")
        if (t1 != -1) spisokGroupSt = spisokGroupSt.substring(0, t1)
        val confirm: DialogDeliteConfirm = DialogDeliteConfirm.getInstance(spisokGroup[groupPosition].string + " " + spisokGroupSt, groupPosition, childPosition)
        confirm.setDialogDeliteConfirmlistiner(this)
        confirm.show(childFragmentManager, "confirm")
    }

    fun setData(textview: Int, year: Int, month: Int, dayOfMonth: Int) {
        val editReakt = childFragmentManager.findFragmentByTag("edit") as? DialodOpisanieEditReakt
        editReakt?.setData(textview, year, month, dayOfMonth)
        val rasxod = childFragmentManager.findFragmentByTag("rasxod") as? DialodReaktRasxod
        rasxod?.setData(textview, year, month, dayOfMonth)
    }

    fun setDialogJurnal(groupposition: Int, childposition: Int, izmerenie: Int, s: String, jurnal: String, i3: Int, octatok: String) {
        val rasxod = DialodReaktRasxod.getInstance(groupposition, childposition, izmerenie, s, jurnal, i3, octatok)
        rasxod.show(childFragmentManager, "rasxod")
    }

    fun updateJurnalRasxoda(position: Int, t0: String, t1: String, t2: String, t3: String, t4: String, t5: String) {
        val jurnal = childFragmentManager.findFragmentByTag("jurnal") as? DialogJurnal
        jurnal?.updateJurnalRasxoda(position, t0, t1, t2, t3, t4, t5)
    }

    private fun seash(groupPosition: Int, childPosition: Int = 0): ArrayList<String> {
        val group = spisokGroup[groupPosition].string
        val t1 = spisokGroup[groupPosition].arrayList[childPosition].indexOf(".")
        val child = spisokGroup[groupPosition].arrayList[childPosition].substring(0, t1)
        val arrayList = ArrayList<String>()
        var end = false
        for ((key, value) in ChemLabFuel.ReaktiveSpisok) {
            for ((_, value2) in value) {
                arrayList.clear()
                for ((key1, value1) in value2) {
                    arrayList.add(key1, value1)
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
            for (i in ChemLabFuel.users.indices) {
                if (ChemLabFuel.users[i][0].contains(editedBy)) {
                    fn = ChemLabFuel.users[i][1]
                    ln = ChemLabFuel.users[i][2]
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
        for (i in ChemLabFuel.users.indices) {
            if (ChemLabFuel.users[i][0].contains(createBy)) {
                fnG = ChemLabFuel.users[i][1]
                lnG = ChemLabFuel.users[i][2]
                break
            }
        }
        val izmerenie = resources.getStringArray(R.array.izmerenie)
        val data02 = arrayList[13]
        val builder = "<strong>Партия</strong><br>" + arrayList[15] + "<br><br>" + "<strong>Дата получения</strong><br>" + arrayList[1] + "<br><br>" + "<strong>Поставщик</strong><br>" + arrayList[2] + "<br><br>" + "<strong>Претензии</strong><br>" + arrayList[3] + "<br><br>" + "<strong>Квалификация</strong><br>" + arrayList[4] + "<br><br>" + "<strong>Партия</strong><br>" + arrayList[17] + "<br><br>" + "<strong>Дата изготовления</strong><br>" + arrayList[5] + "<br><br>" + "<strong>Срок хранения</strong><br>" + arrayList[6] + "<br><br>" + "<strong>Условия хранения</strong><br>" + arrayList[7] + "<br><br>" + "<strong>Единица измерения</strong><br>" + izmerenie[arrayList[8].toInt()] + "<br><br>" + "<strong>Количество на остатке</strong><br>" + arrayList[9].replace(".", ",") + "<br><br>" + "<strong>Минимальное количество</strong><br>" + arrayList[10].replace(".", ",") + "<br><br>" + "<strong>Ответственный</strong><br>" + fnG + " " + lnG + "<br><br>" + "<strong>Изменено</strong><br>" + editedString
        val opisanie: DialodOpisanie = DialodOpisanie.getInstance(data02, builder)
        opisanie.show(childFragmentManager, "opisanie")
        return false
    }

    private fun updateUI() {
        activity?.let { activity ->
            activity.invalidateOptionsMenu()
            if (ChemLabFuel.isNetworkAvailable()) {
                progressBarTab2Listener?.onProgress(View.VISIBLE)
                val mDatabase = FirebaseDatabase.getInstance(ChemLabFuelApp.getApp()).reference
                mDatabase.child("reagents").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        ChemLabFuel.ReaktiveSpisok.clear()
                        spisokGroup.clear()
                        val gson = Gson()
                        for (data in dataSnapshot.children) {
                            val name = (data.child("name").value as String?) ?: ""
                            val id = data.key ?: ""
                            var ostatokSum = BigDecimal.ZERO
                            var minostatok = BigDecimal.ZERO
                            val spisokN = LinkedHashMap<Int, LinkedHashMap<Int, String>>()
                            val spisokChild = ArrayList<String>()
                            val g = Calendar.getInstance() as GregorianCalendar
                            val srokToDay = g.timeInMillis
                            var data05b: Long = 0
                            var data08 = 0
                            var data13 = ""
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
                                        val data12 = data2.child("data12").value as? String ?: ""
                                        data13 = data2.child("data13").value as? String ?: ""
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
                                        spisoks[i] = name //13
                                        i++
                                        spisoks[i] = id //14
                                        i++
                                        spisoks[i] = data2.key ?: "" //15
                                        i++
                                        spisoks[i] = gson.toJson(data11) //16
                                        i++
                                        spisoks[i] = data12 //17
                                        i++
                                        spisoks[i] = data13 //18
                                        data2.key?.let {
                                            spisokN[it.toInt()] = spisoks
                                        }
                                        val data05 = data2.child("data05").value as String
                                        val d = data05.split("-").toTypedArray()
                                        if (d.size == 3) g[d[0].toInt(), d[1].toInt() - 1] = d[2].toInt() else g[d[0].toInt(), d[1].toInt() - 1] = 1
                                        data05b = g.timeInMillis
                                        g.add(Calendar.MONTH, (data2.child("data06").value as Long).toInt())
                                        var ostatok = if (data2.child("data09").value is Double) BigDecimal.valueOf(data2.child("data09").value as Double)
                                        else BigDecimal.valueOf((data2.child("data09").value as Long).toDouble())
                                        ostatok = if (ostatok != BigDecimal.valueOf(0.toDouble())) ostatok.setScale(5, BigDecimal.ROUND_HALF_UP).stripTrailingZeros()
                                        else ostatok.setScale(0, BigDecimal.ROUND_HALF_UP)
                                        if (srokToDay < g.timeInMillis) {
                                            ostatokSum = ostatokSum.add(ostatok)
                                            g.add(Calendar.DATE, -45)
                                            if (srokToDay > g.timeInMillis) srok = " <font color=#9a2828>Истекает срок</font>"
                                        } else {
                                            srok = " <font color=#9a2828>Срок истёк</font>"
                                        }
                                        minostatok = if (data2.child("data10").value is Double) BigDecimal.valueOf(data2.child("data10").value as Double) else BigDecimal.valueOf((data2.child("data10").value as Long).toDouble())
                                        data08 = (data2.child("data08").value as Long).toInt()
                                        spisokChild.add(data2.key + "." + srok + " <!---->Остаток: " + ostatok.toPlainString().replace(".", ",") + " " + edIzmerenia[data08])
                                    }
                                }
                            }
                            spisokGroup.add(ReaktiveSpisok(activity, data05b, id.toInt(), name, ostatokSum, minostatok, data08, spisokChild, data13))
                            ChemLabFuel.ReaktiveSpisok[id.toInt()] = spisokN
                        }
                        spisokGroup.sort()
                        progressBarTab2Listener?.onProgress(View.GONE)
                        arrayAdapter2.notifyDataSetChanged()
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        progressBarTab2Listener?.onProgress(View.GONE)
                    }
                })
            } else {
                val internet = DialogNoInternet()
                internet.show(childFragmentManager, "internet")
            }
        }
    }

    fun setExpandGroup() {
        binding.listView2.post {
            for (i in 0 until arrayAdapter2.groupCount) {
                binding.listView2.expandGroup(i)
            }
        }
    }

    private inner class ListAdapterReakt(myContext: Context) : BaseExpandableListAdapter() {
        private val mContext = myContext
        private val fuel = mContext.getSharedPreferences("fuel", Context.MODE_PRIVATE)
        override fun getGroupCount(): Int {
            return spisokGroup.size
        }

        override fun getChildrenCount(groupPosition: Int): Int {
            return spisokGroup[groupPosition].arrayList.size
        }

        override fun getGroup(groupPosition: Int): Any {
            return spisokGroup[groupPosition]
        }

        override fun getChild(groupPosition: Int, childPosition: Int): Any {
            return spisokGroup[groupPosition].arrayList[childPosition]
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
            var groupOstatok = spisokGroup[groupPosition].ostatok
            if (groupOstatok != BigDecimal.ZERO) groupOstatok = groupOstatok.setScale(5, BigDecimal.ROUND_HALF_UP).stripTrailingZeros()
            var ostatok = " (Остаток: " + groupOstatok.toPlainString().replace(".", ",") + " " + edIzmerenia[spisokGroup[groupPosition].edIzmerenia] + ")"
            val compare = groupOstatok.compareTo(spisokGroup[groupPosition].minostatok)
            if (groupOstatok == BigDecimal.ZERO) ostatok = " <font color=#9a2828>Срок истёк</font>"
            else if (compare <= 0) ostatok = " (<font color=#9a2828>Остаток: " + groupOstatok.toPlainString().replace(".", ",") + " " + edIzmerenia[spisokGroup[groupPosition].edIzmerenia] + "</font>)"
            val id = if (spisokGroup[groupPosition].userID == "") spisokGroup[groupPosition].id.toString()
            else spisokGroup[groupPosition].userID
            group.text?.text = ChemLabFuel.fromHtml(id + ". " + spisokGroup[groupPosition].string + ostatok)
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
            viewHolder.text?.text = ChemLabFuel.fromHtml(spisokGroup[groupPosition].arrayList[childPosition])
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
                if (menuItem.itemId == R.id.menu_add) {
                    val arrayList = seash(groupPosition)
                    val editReakt = DialodOpisanieEditReakt.getInstance(ChemLabFuel.userEdit, spisokGroup[groupPosition].string, spisokGroup[groupPosition].minostatok.toString(), spisokGroup[groupPosition].edIzmerenia, arrayList[14].toInt())
                    editReakt.setListUpdateListiner(this@ChemLabFuelTab2)
                    editReakt.show(childFragmentManager, "edit")
                    return@setOnMenuItemClickListener true
                }
                if (menuItem.itemId == R.id.menu_minus) {
                    val arrayList = seash(groupPosition, childposition)
                    val rasxod = DialodReaktRasxod.getInstance(arrayList[14].toInt(), arrayList[15].toInt(), arrayList[8].toInt(), ChemLabFuel.userEdit, arrayList[9])
                    rasxod.show(childFragmentManager, "rasxod")
                }
                if (menuItem.itemId == R.id.menu_jurnal) {
                    val arrayList = seash(groupPosition, childposition)
                    if (!(arrayList[16] == "[]" || arrayList[16] == "")) {
                        val jurnal = DialogJurnal.getInstance(arrayList[14].toInt(), arrayList[15].toInt(), arrayList[8].toInt(), arrayList[16], ChemLabFuel.userEdit, arrayList[9])
                        jurnal.show(childFragmentManager, "jurnal")
                    } else {
                        ChemLabFuel.setToast("Нет журнала расхода")
                    }
                }
                if (menuItem.itemId == R.id.menu_redoktor) {
                    val arrayList = seash(groupPosition, childposition)
                    val editReakt = DialodOpisanieEditReakt.getInstance(ChemLabFuel.userEdit, arrayList[14].toInt(), arrayList[15].toInt())
                    editReakt.setListUpdateListiner(this@ChemLabFuelTab2)
                    editReakt.show(childFragmentManager, "edit")
                    return@setOnMenuItemClickListener true
                }
                if (menuItem.itemId == R.id.menu_remove) {
                    var spisokGroupSt = spisokGroup[groupPosition].arrayList[childposition]
                    val t1 = spisokGroupSt.indexOf(" <")
                    if (t1 != -1) spisokGroupSt = spisokGroupSt.substring(0, t1)
                    val confirm: DialogDeliteConfirm = DialogDeliteConfirm.getInstance(spisokGroup[groupPosition].string + " " + spisokGroupSt, groupPosition, childposition)
                    confirm.setDialogDeliteConfirmlistiner(this@ChemLabFuelTab2)
                    confirm.show(childFragmentManager, "confirm")
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
}
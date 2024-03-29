package serhij.korneluk.chemlabfuel

import android.app.Activity
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
import serhij.korneluk.chemlabfuel.databinding.CremlabfuelTab1Binding
import java.util.*
import kotlin.collections.ArrayList

class ChemLabFuelTab1 : Fragment(), AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, DialogDeliteConfirm.DialogDeliteConfirmlistiner {

    private var inventarnySpisok: ArrayList<InventorySpisok> = ArrayList()
    private lateinit var arrayAdapter: ListAdapter
    private var _binding: CremlabfuelTab1Binding? = null
    private val binding get() = _binding!!
    private var progressBarTab1Listener: ProgressBarTab1Listener? = null

    interface ProgressBarTab1Listener {
        fun onProgress(visibility: Int)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        progressBarTab1Listener?.onProgress(View.GONE)
        _binding = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Activity) {
            progressBarTab1Listener = try {
                context as ProgressBarTab1Listener
            } catch (e: ClassCastException) {
                throw ClassCastException("$activity must implement progressBarTab1Listener")
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = CremlabfuelTab1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            arrayAdapter = ListAdapter(it)
            binding.listView.adapter = arrayAdapter
            binding.listView.onItemClickListener = this
            binding.listView.onItemLongClickListener = this
            updateUI()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    fun setData(textview: Int, year: Int, month: Int, dayOfMonth: Int) {
        val edit = childFragmentManager.findFragmentByTag("edit") as? DialodOpisanieEdit
        edit?.setData(textview, year, month, dayOfMonth)
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
        inventarnySpisok.sort()
        arrayAdapter.notifyDataSetChanged()
        activity?.invalidateOptionsMenu()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        activity?.let {
            val id = item.itemId
            if (id == R.id.add) {
                if (ChemLabFuel.isNetworkAvailable()) {
                    val edit = DialodOpisanieEdit.getInstance(ChemLabFuel.userEdit, inventarnySpisok.size.toLong())
                    edit.show(childFragmentManager, "edit")
                } else {
                    val internet = DialogNoInternet()
                    internet.show(childFragmentManager, "internet")
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun onDialogDeliteClick(position: Int) {
        activity?.let {
            if (ChemLabFuel.isNetworkAvailable()) {
                val confirm: DialogDeliteConfirm = DialogDeliteConfirm.getInstance(inventarnySpisok[position].data02, -1, position)
                confirm.setDialogDeliteConfirmlistiner(this)
                confirm.show(childFragmentManager, "confirm")
            } else {
                val internet = DialogNoInternet()
                internet.show(childFragmentManager, "internet")
            }
        }
    }

    fun onDialogEditPosition(position: Int) {
        activity?.let {
            if (ChemLabFuel.isNetworkAvailable()) {
                val edit = DialodOpisanieEdit.getInstance(ChemLabFuel.userEdit, inventarnySpisok[position].uid, inventarnySpisok[position].data02, inventarnySpisok[position].data03, inventarnySpisok[position].data04, inventarnySpisok[position].data05, inventarnySpisok[position].data06, inventarnySpisok[position].data07, inventarnySpisok[position].data08, inventarnySpisok[position].data09, inventarnySpisok[position].data10, inventarnySpisok[position].data12)
                edit.show(childFragmentManager, "edit")
            } else {
                val internet = DialogNoInternet()
                internet.show(childFragmentManager, "internet")
            }
        }
    }

    override fun deliteData(position: Int, groupPosition: Int) {
        val mDatabase = FirebaseDatabase.getInstance(ChemLabFuelApp.getApp()).reference
        inventarnySpisok[position].uid?.let { mDatabase.child("equipments").child(it).removeValue() }
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
                for (i in ChemLabFuel.users.indices) {
                    if (ChemLabFuel.users[i][0].contains(editedBy ?: "")) {
                        fn = ChemLabFuel.users[i][1]
                        ln = ChemLabFuel.users[i][2]
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
            for (i in ChemLabFuel.users.indices) {
                if (ChemLabFuel.users[i][0].contains(createBy ?: "")) {
                    fnG = ChemLabFuel.users[i][1]
                    lnG = ChemLabFuel.users[i][2]
                    break
                }
            }
            val data02 = inventarnySpisok[position].data02
            val builder = getString(R.string.opisanie1, inventarnySpisok[position].data03, inventarnySpisok[position].data04, inventarnySpisok[position].data05, inventarnySpisok[position].data06, inventarnySpisok[position].data07, inventarnySpisok[position].data08, inventarnySpisok[position].data09, inventarnySpisok[position].data10, fnG, lnG, inventarnySpisok[position].data12, editedString)
            val opisanie = DialodOpisanie.getInstance(data02, builder)
            opisanie.show(childFragmentManager, "opisanie")
        }
    }

    override fun onItemLongClick(parent: AdapterView<*>?, view: View, position: Int, id: Long): Boolean {
        val menu: DialogContextMenu = DialogContextMenu.getInstance(position, inventarnySpisok[position].data02)
        menu.show(childFragmentManager, "menu")
        return true
    }

    private fun updateUI() {
        activity?.let { activity ->
            activity.invalidateOptionsMenu()
            if (ChemLabFuel.isNetworkAvailable()) {
                progressBarTab1Listener?.onProgress(View.VISIBLE)
                val mDatabase = FirebaseDatabase.getInstance(ChemLabFuelApp.getApp()).reference
                mDatabase.child("equipments").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        inventarnySpisok.clear()
                        for (data in dataSnapshot.children) {
                            if (data.value is HashMap<*, *>) {
                                val hashMap = data.value as HashMap<*, *>
                                if (hashMap.size > 12) {
                                    val editedAt = hashMap["editedAt"] ?: 0L
                                    val editedBy = hashMap["editedBy"] ?: ""
                                    inventarnySpisok.add(InventorySpisok(activity, hashMap["createdBy"] as String?, hashMap["data01"] as Long, hashMap["data02"] as String?, hashMap["data03"] as String?, hashMap["data04"] as String?, hashMap["data05"] as String?, hashMap["data06"] as String?, hashMap["data07"] as String?, hashMap["data08"] as String?, hashMap["data09"] as String?, hashMap["data10"] as String?, hashMap["data11"] as Long, hashMap["data12"] as String?, data.key, editedAt as Long, editedBy as String?))
                                }
                            }
                        }
                        inventarnySpisok.sort()
                        arrayAdapter.notifyDataSetChanged()
                        progressBarTab1Listener?.onProgress(View.GONE)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        progressBarTab1Listener?.onProgress(View.GONE)
                    }
                })
            } else {
                val internet = DialogNoInternet()
                internet.show(childFragmentManager, "internet")
            }
        }
    }

    private inner class ListAdapter(mycontext: Context) : ArrayAdapter<InventorySpisok>(mycontext, R.layout.simple_list_item, inventarnySpisok) {
        private val mcontext = mycontext
        private val fuel: SharedPreferences = mcontext.getSharedPreferences("fuel", Context.MODE_PRIVATE)
        override fun getView(position: Int, mView: View?, parent: ViewGroup): View {
            val root: View
            val viewHolder: ViewHolder
            if (mView == null) {
                val vi = mcontext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
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
            viewHolder.text?.text = ChemLabFuel.fromHtml(inventarnySpisok[position].data01.toString() + ". " + inventarnySpisok[position].data02 + dataLong)
            viewHolder.text?.textSize = fuel.getInt("fontsize", 18).toFloat()
            return root
        }

        private fun showPopupMenu(view: View?, position: Int) {
            val popup = PopupMenu(mcontext, view)
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

    private class ViewHolder {
        var text: TextView? = null
        var buttonPopup: ImageView? = null
    }
}
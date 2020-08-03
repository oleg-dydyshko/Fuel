package serhij.korneluk.chemlabfuel

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.PagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import kotlinx.android.synthetic.main.cremlabfuel.*
import serhij.korneluk.chemlabfuel.DialodOpisanieEditReakt.ListUpdateListiner
import serhij.korneluk.chemlabfuel.DialodReaktRasxod.UpdateJurnal
import serhij.korneluk.chemlabfuel.DialogContextMenu.DialogContextMenuListener
import serhij.korneluk.chemlabfuel.DialogContextMenuReakt.DialogContextMenuReaktListener
import serhij.korneluk.chemlabfuel.DialogData.DialogDataListiner
import serhij.korneluk.chemlabfuel.DialogDeliteConfirm.DialogDeliteConfirmlistiner
import java.util.*

class CremLabFuel : AppCompatActivity(), DialogContextMenuListener, DialogDeliteConfirmlistiner, DialogDataListiner, ListUpdateListiner, UpdateJurnal, DialogContextMenuReaktListener {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var myTabPagerAdapter: MyTabPagerAdapter

    override fun onDialogAddPartia(groupPosition: Int) {
        val page = (myTabPagerAdapter as MyFragmentStatePagerAdapter).getFragment(1) as CremLabFuelTab2
        page.onDialogAddPartia(groupPosition)
    }

    override fun onDialogRashod(groupPosition: Int, childPosition: Int) {
        val page = (myTabPagerAdapter as MyFragmentStatePagerAdapter).getFragment(1) as CremLabFuelTab2
        page.onDialogRashod(groupPosition, childPosition)
    }

    override fun onDialogJurnal(groupPosition: Int, childPosition: Int) {
        val page = (myTabPagerAdapter as MyFragmentStatePagerAdapter).getFragment(1) as CremLabFuelTab2
        page.onDialogJurnal(groupPosition, childPosition)
    }

    override fun onDialogEdit(groupPosition: Int, childPosition: Int) {
        val page = (myTabPagerAdapter as MyFragmentStatePagerAdapter).getFragment(1) as CremLabFuelTab2
        page.onDialogEdit(groupPosition, childPosition)
    }

    override fun onDialogRemove(groupPosition: Int, childPosition: Int) {
        val page = (myTabPagerAdapter as MyFragmentStatePagerAdapter).getFragment(1) as CremLabFuelTab2
        page.onDialogRemove(groupPosition, childPosition)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cremlabfuel)
        val fuel = getSharedPreferences("fuel", Context.MODE_PRIVATE)
        mAuth = FirebaseAuth.getInstance()
        myTabPagerAdapter = MyTabPagerAdapter(supportFragmentManager)
        tabPager.adapter = myTabPagerAdapter
        tabLayout.setupWithViewPager(tabPager)
        if (fuel.getBoolean("oborudovanie", true)) {
            tabPager.currentItem = 0
        } else {
            tabPager.currentItem = 1
        }
        tabLayout.setupWithViewPager(tabPager)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                val position = tab?.position ?: 0
                tabPager.currentItem = position
                val editor = fuel.edit()
                if (position == 0) editor.putBoolean("oborudovanie", true)
                else editor.putBoolean("oborudovanie", false)
                editor.apply()
                invalidateOptionsMenu()
            }
        })
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

    override fun updateList() {
        val page = (myTabPagerAdapter as MyFragmentStatePagerAdapter).getFragment(1) as CremLabFuelTab2
        page.updateList()
    }

    override fun updateJurnalRasxoda(position: Int, t0: String, t1: String, t2: String, t3: String, t4: String, t5: String) {
        val page = (myTabPagerAdapter as MyFragmentStatePagerAdapter).getFragment(1) as CremLabFuelTab2
        page.updateJurnalRasxoda(position, t0, t1, t2, t3, t4, t5)
    }

    override fun onDialogEditPosition(position: Int) {
        val page = (myTabPagerAdapter as MyFragmentStatePagerAdapter).getFragment(0) as CremLabFuelTab1
        page.onDialogEditPosition(position)
    }

    override fun onDialogDeliteClick(position: Int) {
        val page = (myTabPagerAdapter as MyFragmentStatePagerAdapter).getFragment(0) as CremLabFuelTab1
        page.onDialogDeliteClick(position)
    }

    override fun deliteData(groupPosition: Int, position: Int) {
        if (groupPosition == -1) {
            val page = (myTabPagerAdapter as MyFragmentStatePagerAdapter).getFragment(0) as CremLabFuelTab1
            page.deliteData(position)
        } else {
            val page = (myTabPagerAdapter as MyFragmentStatePagerAdapter).getFragment(1) as CremLabFuelTab2
            page.deliteData(groupPosition, position)
        }
    }

    override fun setData(textview: Int, year: Int, month: Int, dayOfMonth: Int) {
        val page = (myTabPagerAdapter as MyFragmentStatePagerAdapter).getFragment(1) as CremLabFuelTab2
        page.setData(textview, year, month, dayOfMonth)
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
        if (id == R.id.exit) {
            mAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        if (id == R.id.settings) {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        if (id == R.id.sortdate) {
            val fuel = getSharedPreferences("fuel", Context.MODE_PRIVATE)
            val editor = fuel.edit()
            if (item.isChecked) {
                editor.putInt("sort", 0)
                editor.apply()
            } else {
                editor.putInt("sort", 1)
                editor.apply()
            }
            val page = (myTabPagerAdapter as MyFragmentStatePagerAdapter).getFragment(0) as CremLabFuelTab1
            page.updateSort()
            val page2 = (myTabPagerAdapter as MyFragmentStatePagerAdapter).getFragment(1) as CremLabFuelTab2
            page2.updateSort()
        }
        if (id == R.id.sorttime) {
            val fuel = getSharedPreferences("fuel", Context.MODE_PRIVATE)
            val editor = fuel.edit()
            if (item.isChecked) {
                editor.putInt("sort", 0)
                editor.apply()
            } else {
                editor.putInt("sort", 2)
                editor.apply()
            }
            val page = (myTabPagerAdapter as MyFragmentStatePagerAdapter).getFragment(0) as CremLabFuelTab1
            page.updateSort()
            val page2 = (myTabPagerAdapter as MyFragmentStatePagerAdapter).getFragment(1) as CremLabFuelTab2
            page2.updateSort()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateUI() {
        invalidateOptionsMenu()
        if (isNetworkAvailable(this)) {
            val fuel = getSharedPreferences("fuel", Context.MODE_PRIVATE)
            val mDatabase = FirebaseDatabase.getInstance().reference
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
                    if (intent.extras?.getBoolean("reaktive", false) == true) {
                        tabPager.currentItem = 1
                        val page = (myTabPagerAdapter as MyFragmentStatePagerAdapter).getFragment(1) as CremLabFuelTab2
                        page.setExpandGroup()
                    }
                    sendBroadcast(Intent(this@CremLabFuel, ReceiverSetAlarm::class.java))
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        } else {
            val internet = DialogNoInternet()
            internet.show(supportFragmentManager, "internet")
        }
    }

    private inner class MyTabPagerAdapter(fragmentManager: FragmentManager) : MyFragmentStatePagerAdapter(fragmentManager) {
        override fun getCount(): Int {
            return 2
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return if (position == 0)
                getString(R.string.oborudovanie)
            else
                getString(R.string.reaktivy)
        }

        override fun getItem(position: Int): Fragment {
            return if (position == 0) {
                CremLabFuelTab1()
            } else {
                CremLabFuelTab2()
            }
        }

        override fun getItemPosition(ob: Any): Int {
            return PagerAdapter.POSITION_NONE
        }
    }

    companion object {
        val users = ArrayList<ArrayList<String>>()
        val ReaktiveSpisok = LinkedHashMap<Int, LinkedHashMap<Int, LinkedHashMap<Int, String>>>()
        var userEdit = ""

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

        @Suppress("DEPRECATION")
        fun setToast(context: Context, message: String) {
            val layout = LinearLayout(context)
            layout.setBackgroundResource(R.color.colorPrimary)
            val toast = TextView(context)
            toast.setTextColor(ContextCompat.getColor(context, R.color.colorIcons))
            toast.setPadding(10, 10, 10, 10)
            toast.text = message
            toast.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            layout.addView(toast)
            val mes = Toast(context)
            mes.duration = Toast.LENGTH_LONG
            mes.view = layout
            mes.show()
        }
    }
}
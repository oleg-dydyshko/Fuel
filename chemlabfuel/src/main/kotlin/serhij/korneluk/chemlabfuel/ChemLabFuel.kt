package serhij.korneluk.chemlabfuel

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import serhij.korneluk.chemlabfuel.databinding.CremlabfuelBinding

class ChemLabFuel : AppCompatActivity(), DialogData.DialogDataListiner, DialogContextMenu.DialogContextMenuListener, DialogContextMenuReakt.DialogContextMenuReaktListener, ChemLabFuelTab1.ProgressBarTab1Listener, ChemLabFuelTab2.ProgressBarTab2Listener, DialodReaktRasxod.UpdateJurnal, DialogJurnal.DialogJurnalListener {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var myTabPagerAdapter: MyTabPagerAdapter
    private lateinit var binding: CremlabfuelBinding

    override fun updateJurnalRasxoda(position: Int, t0: String, t1: String, t2: String, t3: String, t4: String, t5: String) {
        val page = supportFragmentManager.findFragmentByTag("f" + 1) as? ChemLabFuelTab2
        page?.updateJurnalRasxoda(position, t0, t1, t2, t3, t4, t5)
    }

    override fun onProgress(visibility: Int) {
        binding.progressBar.visibility = visibility
    }

    override fun onDialogEditPosition(position: Int) {
        val page = supportFragmentManager.findFragmentByTag("f" + 0) as? ChemLabFuelTab1
        page?.onDialogEditPosition(position)
    }

    override fun onDialogDeliteClick(position: Int) {
        val page = supportFragmentManager.findFragmentByTag("f" + 0) as? ChemLabFuelTab1
        page?.onDialogDeliteClick(position)
    }

    override fun onDialogAddPartia(groupPosition: Int) {
        val page = supportFragmentManager.findFragmentByTag("f" + 1) as? ChemLabFuelTab2
        page?.onDialogAddPartia(groupPosition)
    }

    override fun onDialogRashod(groupPosition: Int, childPosition: Int) {
        val page = supportFragmentManager.findFragmentByTag("f" + 1) as? ChemLabFuelTab2
        page?.onDialogRashod(groupPosition, childPosition)
    }

    override fun onDialogJurnal(groupPosition: Int, childPosition: Int) {
        val page = supportFragmentManager.findFragmentByTag("f" + 1) as? ChemLabFuelTab2
        page?.onDialogJurnal(groupPosition, childPosition)
    }

    override fun onDialogEdit(groupPosition: Int, childPosition: Int) {
        val page = supportFragmentManager.findFragmentByTag("f" + 1) as? ChemLabFuelTab2
        page?.onDialogEdit(groupPosition, childPosition)
    }

    override fun onDialogRemove(groupPosition: Int, childPosition: Int) {
        val page = supportFragmentManager.findFragmentByTag("f" + 1) as? ChemLabFuelTab2
        page?.onDialogRemove(groupPosition, childPosition)
    }

    override fun setDialogJurnal(groupposition: Int, childposition: Int, izmerenie: Int, s: String, jurnal: String, i3: Int, octatok: String) {
        val page = supportFragmentManager.findFragmentByTag("f" + 1) as? ChemLabFuelTab2
        page?.setDialogJurnal(groupposition, childposition, izmerenie, s, jurnal, i3, octatok)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CremlabfuelBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val fuel = getSharedPreferences("fuel", Context.MODE_PRIVATE)
        mAuth = FirebaseAuth.getInstance(ChemLabFuelApp.getApp())
        myTabPagerAdapter = MyTabPagerAdapter(this)
        binding.tabPager.adapter = myTabPagerAdapter
        if (fuel.getBoolean("oborudovanie", true)) {
            binding.tabPager.currentItem = 0
        } else {
            binding.tabPager.currentItem = 1
        }
        binding.tabPager.offscreenPageLimit = 1
        TabLayoutMediator(binding.tabLayout, binding.tabPager, false) { tab, position ->
            tab.text = if (position == 0) getString(R.string.oborudovanie)
            else getString(R.string.reaktivy)
        }.attach()
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                val position = tab?.position ?: 0
                binding.tabPager.currentItem = position
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
        binding.titleToolbar.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        setSupportActionBar(binding.toolbar)
        binding.titleToolbar.setText(R.string.app_main)
    }

    public override fun onResume() {
        super.onResume()
        overridePendingTransition(R.anim.alphain, R.anim.alphaout)
    }

    override fun setData(textview: Int, year: Int, month: Int, dayOfMonth: Int) {
        val page = supportFragmentManager.findFragmentByTag("f" + 0) as? ChemLabFuelTab1
        page?.setData(textview, year, month, dayOfMonth)
        val page2 = supportFragmentManager.findFragmentByTag("f" + 1) as? ChemLabFuelTab2
        page2?.setData(textview, year, month, dayOfMonth)
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
            val page = supportFragmentManager.findFragmentByTag("f" + 0) as? ChemLabFuelTab1
            page?.updateSort()
            val page2 = supportFragmentManager.findFragmentByTag("f" + 1) as? ChemLabFuelTab2
            page2?.updateSort()
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
            val page = supportFragmentManager.findFragmentByTag("f" + 0) as? ChemLabFuelTab1
            page?.updateSort()
            val page2 = supportFragmentManager.findFragmentByTag("f" + 1) as? ChemLabFuelTab2
            page2?.updateSort()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateUI() {
        invalidateOptionsMenu()
        if (isNetworkAvailable()) {
            val fuel = getSharedPreferences("fuel", Context.MODE_PRIVATE)
            val mDatabase = FirebaseDatabase.getInstance(ChemLabFuelApp.getApp()).reference
            mDatabase.child("users").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (data in dataSnapshot.children) {
                        val key = data.key ?: ""
                        if (mAuth.uid?.contains(key) == true) {
                            userEdit = key
                        }
                        for (data2 in data.children) {
                            if (data2.value is HashMap<*, *>) {
                                val hashMap = data2.value as HashMap<*, *>
                                val firstname = (hashMap["firstName"] as? String) ?: ""
                                val lastname = (hashMap["lastName"] as? String) ?: ""
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
                        binding.tabPager.currentItem = 1
                        binding.tabPager.post {
                            val page = supportFragmentManager.findFragmentByTag("f" + 1) as? ChemLabFuelTab2
                            page?.setExpandGroup()
                        }
                    }
                    SettingsActivity.runAlarm()
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        } else {
            val internet = DialogNoInternet()
            internet.show(supportFragmentManager, "internet")
        }
    }

    private inner class MyTabPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount() = 2

        override fun createFragment(position: Int): Fragment {
            return if (position == 0) {
                ChemLabFuelTab1()
            } else {
                ChemLabFuelTab2()
            }
        }
    }

    companion object {
        const val INFINITY = -1
        val users = ArrayList<ArrayList<String>>()
        val ReaktiveSpisok = LinkedHashMap<Int, LinkedHashMap<Int, LinkedHashMap<Int, String>>>()
        var userEdit = ""

        fun fromHtml(html: String) = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)

        @Suppress("DEPRECATION")
        fun isNetworkAvailable(): Boolean {
            val connectivityManager = ChemLabFuelApp.applicationContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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
        fun setToast(message: String) {
            val context = ChemLabFuelApp.applicationContext()
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
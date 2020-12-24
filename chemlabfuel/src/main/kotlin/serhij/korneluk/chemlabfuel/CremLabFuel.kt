package serhij.korneluk.chemlabfuel

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.util.SparseArray
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import serhij.korneluk.chemlabfuel.DialogData.DialogDataListiner
import serhij.korneluk.chemlabfuel.databinding.CremlabfuelBinding

class CremLabFuel : AppCompatActivity(), DialogDataListiner {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var myTabPagerAdapter: MyTabPagerAdapter
    private lateinit var binding: CremlabfuelBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CremlabfuelBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val fuel = getSharedPreferences("fuel", Context.MODE_PRIVATE)
        mAuth = FirebaseAuth.getInstance()
        myTabPagerAdapter = MyTabPagerAdapter(supportFragmentManager)
        binding.tabPager.adapter = myTabPagerAdapter
        binding.tabLayout.setupWithViewPager(binding.tabPager)
        if (fuel.getBoolean("oborudovanie", true)) {
            binding.tabPager.currentItem = 0
        } else {
            binding.tabPager.currentItem = 1
        }
        binding.tabLayout.setupWithViewPager(binding.tabPager)
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

    override fun setData(textview: Int, year: Int, month: Int, dayOfMonth: Int, fragment: Int) {
        if (fragment == 1) {
            val page = myTabPagerAdapter.getFragment(0) as CremLabFuelTab1
            page.setData(textview, year, month, dayOfMonth)
        } else {
            val page2 = myTabPagerAdapter.getFragment(1) as CremLabFuelTab2
            page2.setData(textview, year, month, dayOfMonth)
        }
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
            val page = myTabPagerAdapter.getFragment(0) as CremLabFuelTab1
            page.updateSort()
            val page2 = myTabPagerAdapter.getFragment(1) as CremLabFuelTab2
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
            val page = myTabPagerAdapter.getFragment(0) as CremLabFuelTab1
            page.updateSort()
            val page2 = myTabPagerAdapter.getFragment(1) as CremLabFuelTab2
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
                        binding.tabPager.currentItem = 1
                        val page = myTabPagerAdapter.getFragment(1) as CremLabFuelTab2
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

    private inner class MyTabPagerAdapter(fragmentManager: FragmentManager) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val registeredFragments = SparseArray<Fragment>()
        override fun getCount(): Int {
            return 2
        }

        override fun getPageTitle(position: Int): CharSequence {
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

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val fragment = super.instantiateItem(container, position) as Fragment
            registeredFragments.put(position, fragment)
            return fragment
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            registeredFragments.remove(position)
            super.destroyItem(container, position, `object`)
        }

        fun getFragment(key: Int) : Fragment {
            return registeredFragments.get(key)
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
package serhij.korneluk.chemlabfuel

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private var email: String = ""
    private var password1: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val fuel = getSharedPreferences("fuel", Context.MODE_PRIVATE)
        if (intent.extras?.getBoolean("notifications", false) == true) {
            val editor = fuel.edit()
            editor.putInt("sort", 2)
            editor.apply()
        }
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance()
        login.setOnClickListener {
            // Скрываем клавиатуру
            val imm1 = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm1.hideSoftInputFromWindow(username.windowToken, 0)
            email = username.text.toString()
            password1 = password.text.toString()
            if (email != "" && password1 != "") {
                mAuth.signInWithEmailAndPassword(email, password1).addOnCompleteListener(this@MainActivity) { task: Task<AuthResult?> ->
                    if (task.isSuccessful) {
                        val user1 = mAuth.currentUser
                        updateUI(user1)
                    } else {
                        val layout = LinearLayout(this)
                        layout.setBackgroundResource(R.color.colorPrimary)
                        val toast = TextView(this)
                        toast.setTextColor(ContextCompat.getColor(this, R.color.colorIcons))
                        toast.setPadding(10, 10, 10, 10)
                        toast.text = "Неверный логин или пароль"
                        toast.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                        layout.addView(toast)
                        val mes = Toast(this)
                        mes.duration = Toast.LENGTH_LONG
                        mes.view = layout
                        mes.show()
                        updateUI(null)
                    }
                }
            } else {
                val layout = LinearLayout(this)
                layout.setBackgroundResource(R.color.colorPrimary)
                val toast = TextView(this)
                toast.setTextColor(ContextCompat.getColor(this, R.color.colorIcons))
                toast.setPadding(10, 10, 10, 10)
                toast.text = "Неверный логин или пароль"
                toast.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                layout.addView(toast)
                val mes = Toast(this)
                mes.duration = Toast.LENGTH_LONG
                mes.view = layout
                mes.show()
                updateUI(null)
            }
        }
        link.isClickable = true
        link.movementMethod = LinkMovementMethod.getInstance()
        val text = "<a href='https://github.com/oleg-dydyshko/Fuel/blob/master/README.md'>Политика конфиденциальности</a>"
        link.text = CremLabFuel.fromHtml(text)
        setTollbarTheme()
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

    public override fun onStart() {
        super.onStart()
        // Check if useremail is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth.currentUser
        updateUI(currentUser)
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            val intent = Intent(this, CremLabFuel::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            if (getIntent().extras != null) intent.putExtra("reaktive", getIntent().extras?.getBoolean("reaktive", false))
            startActivity(intent)
            finish()
        }
    }
}
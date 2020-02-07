package serhij.korneluk.chemlabfuel

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        if (getIntent().extras != null) {
            intent.putExtra("notifications", getIntent().extras?.getBoolean("notifications", false))
            intent.putExtra("reaktive", getIntent().extras?.getBoolean("reaktive", false))
        }
        startActivity(intent)
        finish()
    }
}
package serhij.korneluk.chemlabfuel

import android.content.Context
import androidx.multidex.MultiDexApplication
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class CremLabFuelApp : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        initializeFirebaseApp()
    }

    init {
        instance = this
    }

    companion object {
        private var chemlabfuelApp: FirebaseApp? = null
        private var instance: CremLabFuelApp? = null

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }

        fun initializeFirebaseApp() { // Initialize Firebase Auth
            val options = FirebaseOptions.Builder().setApplicationId("lab-react-firebase")
                .setApiKey("AIzaSyAaZZ7BqCG0oqh_UhDy9C3USYyCU2C-HYk")
                .setDatabaseUrl("https://lab-react-firebase.firebaseio.com")
                .setStorageBucket("lab-react-firebase.appspot.com").build()
            chemlabfuelApp = FirebaseApp.initializeApp(applicationContext(), options, "chemlabfuel")
        }

        fun getApp(): FirebaseApp {
            if (chemlabfuelApp == null) initializeFirebaseApp()
            return chemlabfuelApp!!
        }
    }
}
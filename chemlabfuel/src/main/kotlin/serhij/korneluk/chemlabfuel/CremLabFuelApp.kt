package serhij.korneluk.chemlabfuel

import androidx.multidex.MultiDexApplication
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class CremLabFuelApp : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase Auth
        val options = FirebaseOptions.Builder().setApplicationId("lab-react-firebase")
            .setApiKey("AIzaSyAaZZ7BqCG0oqh_UhDy9C3USYyCU2C-HYk").setDatabaseUrl("https://lab-react-firebase.firebaseio.com")
            .setStorageBucket("lab-react-firebase.appspot.com").build()
        chemlabfuelApp = FirebaseApp.initializeApp(this, options, "chemlabfuel")
    }

    companion object {
        private var chemlabfuelApp: FirebaseApp? = null

        fun getApp() = chemlabfuelApp!!
    }
}
package dev.twobrain.purrsense

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.initialize

class PurrSenseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Firebase.initialize(context = this)
        // Firebase AI Logic requires App Check. Debug provider is accepted for the hackathon:
        // copy the debug secret from logcat into Firebase console > App Check > Manage debug tokens.
        // For production, switch to PlayIntegrityAppCheckProviderFactory.
        Firebase.appCheck.installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())
    }
}

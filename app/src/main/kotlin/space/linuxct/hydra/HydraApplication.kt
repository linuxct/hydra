package space.linuxct.hydra

import android.annotation.SuppressLint
import android.app.Application
import space.linuxct.hydra.views.MainActivity

class HydraApplication : Application() {
    companion object {
        @JvmStatic lateinit var INSTANCE: HydraApplication
        @SuppressLint("StaticFieldLeak") @JvmStatic lateinit var ACTIVITY: MainActivity
        @JvmStatic val TAG: String = "HydraApplication"
    }

    override fun onCreate() {
        INSTANCE = this
        super.onCreate()
    }
}
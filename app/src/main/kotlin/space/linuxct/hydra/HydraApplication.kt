package space.linuxct.hydra

import android.app.Application

class HydraApplication : Application() {
    companion object {
        @JvmStatic lateinit var INSTANCE: HydraApplication
        @JvmStatic val TAG: String = "HydraApplication"
    }

    override fun onCreate() {
        INSTANCE = this
        super.onCreate()
    }
}
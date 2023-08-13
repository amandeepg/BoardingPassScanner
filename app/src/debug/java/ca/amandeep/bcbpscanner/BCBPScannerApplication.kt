package ca.amandeep.bcbpscanner

import android.app.Application
import ca.amandeep.bcbpscanner.ui.boardingpass.AirportToNameMap
import timber.log.Timber
import timber.log.Timber.DebugTree

class BCBPScannerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AirportToNameMap.init(this)
        Timber.plant(DebugTree())
    }
}

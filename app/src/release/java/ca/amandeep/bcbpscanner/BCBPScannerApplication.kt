package ca.amandeep.bcbpscanner

import android.app.Application
import ca.amandeep.bcbpscanner.ui.boardingpass.AirportToNameMap

class BCBPScannerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AirportToNameMap.init(this)
    }
}

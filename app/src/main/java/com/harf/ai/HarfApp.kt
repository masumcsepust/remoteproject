package com.harf.ai

import android.app.Application
import com.harf.ai.data.AppSettings
import com.harf.ai.data.HarfDatabase
import com.harf.ai.data.HarfRepository
import com.harf.ai.data.VerifyApi
import java.io.File

class HarfApp : Application() {
    lateinit var repository: HarfRepository
        private set
    lateinit var settings: AppSettings
        private set

    override fun onCreate() {
        super.onCreate()
        settings = AppSettings(this)
        repository = HarfRepository(
            db = HarfDatabase(this),
            api = VerifyApi(BuildConfig.VERIFY_API_BASE_URL),
            // App-private internal storage: survives restarts, removed on uninstall.
            recordingsDir = File(filesDir, "recordings"),
        )
    }
}

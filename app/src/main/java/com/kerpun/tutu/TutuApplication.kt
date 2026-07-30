package com.kerpun.tutu

import android.app.Application
import com.kerpun.tutu.data.AppContainer
import kotlin.concurrent.thread

class TutuApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Warms up AppContainer (Supabase/Ktor client construction) on a background
        // thread as early as possible, so it's ready by the time the first ViewModel
        // needs it instead of blocking the main thread during first composition.
        thread(name = "AppContainer-warmup") { AppContainer.categoryRepository }
    }
}

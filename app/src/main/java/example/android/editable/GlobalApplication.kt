@file:Suppress("StaticFieldLeak")

package example.android.editable

import android.app.Application
import android.content.Context

class GlobalApplication : Application() {

    companion object {

        lateinit var context: Context
            private set

    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }

}
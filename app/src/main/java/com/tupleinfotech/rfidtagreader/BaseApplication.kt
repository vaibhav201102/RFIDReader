package com.tupleinfotech.rfidtagreader

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

/**
 * @Author: athulyatech
 * @Date: 5/10/24
 */

@SuppressLint("StaticFieldLeak")
class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        context = this
    }

    companion object {
        var context: Context? = null
            private set
    }
}
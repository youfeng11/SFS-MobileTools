package com.youfeng.sfs.mobiletools

import android.os.Build
import android.app.Application
import com.youfeng.sfs.mobiletools.common.logging.FileLoggingTree
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {
    @Inject lateinit var fileLoggingTree: FileLoggingTree

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Timber.plant(fileLoggingTree)

        Timber.i("应用版本：${BuildConfig.VERSION_NAME}（${BuildConfig.VERSION_CODE}）")
        Timber.i("设备信息：${Build.MANUFACTURER} ${Build.BRAND} ${Build.MODEL} ${Build.VERSION.SDK_INT}")
    }
}

package com.youfeng.sfs.mobiletools.data

import android.os.Environment
import okio.Path.Companion.toOkioPath

object SfsFileConfig {
    // SFS 游戏的包名
    const val PACKAGE_NAME = "com.StefMorojna.SpaceflightSimulator"

    val externalStoragePath = Environment.getExternalStorageDirectory().toOkioPath()

    // SFS 资源目录路径
    val assetsDirectoryPath = externalStoragePath / "Android" / "media" / PACKAGE_NAME

    // 自定义翻译
    const val CUSTOM_TRANSLATIONS = "Custom Translations"
}
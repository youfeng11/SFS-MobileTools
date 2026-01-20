package com.youfeng.sfs.mobiletools.data

import android.os.Environment
import okio.Path.Companion.toOkioPath

object SfsFileConfig {
    // SFS 游戏的包名
    const val PACKAGE_NAME = "com.StefMorojna.SpaceflightSimulator"

    val externalStoragePath = Environment.getExternalStorageDirectory().toOkioPath()

    // SFS 资源目录路径
    val assetsDirectoryPath = externalStoragePath / "Android" / "media" / PACKAGE_NAME

    // 蓝图
    val blueprintsPath = assetsDirectoryPath / "Saving" / "Blueprints"

    // Mods
    val partsModsPath = assetsDirectoryPath / "Mods" / "Custom_Assets" / "Parts"
    val texturePacksModsPath = assetsDirectoryPath / "Mods" / "Custom_Assets" / "Texture Packs"

    // 世界
    val worldsPath = assetsDirectoryPath / "Saving" / "Worlds"

    // 自定义星系
    val customSolarSystemsPath = assetsDirectoryPath / "Custom Solar Systems"

    // 自定义翻译
    val customTranslationsPath = assetsDirectoryPath / "Custom Translations"
}
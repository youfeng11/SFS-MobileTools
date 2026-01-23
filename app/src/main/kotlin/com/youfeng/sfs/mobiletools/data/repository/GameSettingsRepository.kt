package com.youfeng.sfs.mobiletools.data.repository

import android.content.Context
import com.youfeng.sfs.mobiletools.common.model.AssetInfo
import com.youfeng.sfs.mobiletools.common.model.AssetType
import com.youfeng.sfs.mobiletools.data.SfsFileConfig
import com.youfeng.sfs.mobiletools.util.sizeInKb
import javax.inject.Inject
import javax.inject.Singleton
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import okio.Path
import timber.log.Timber
import com.youfeng.sfs.mobiletools.common.model.ModType

interface GameSettingsRepository {
    fun getLanguageSettings(): List<AssetInfo>
    fun setLanguageSettings(asset: AssetInfo, custom: Boolean)
}


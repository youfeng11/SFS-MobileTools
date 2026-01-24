package com.youfeng.sfs.mobiletools.data.repository

import com.youfeng.sfs.mobiletools.common.model.AssetInfo

interface GameSettingsRepository {
    fun getLanguageSettings(): List<AssetInfo>
    fun setLanguageSettings(asset: AssetInfo, custom: Boolean)
}


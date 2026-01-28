package com.youfeng.sfs.mobiletools.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class GameLanguageSettingsDto(
    val codeName: String,
    val custom: Boolean
)
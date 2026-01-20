package com.youfeng.sfs.mobiletools.common.model

sealed class AssetType {
    data object Blueprint : AssetType()
    data class Mod(val type: ModType) : AssetType()
    data object World : AssetType()
    data object CustomSolarSystem : AssetType()
    data object CustomTranslation : AssetType()
}

enum class ModType {
    CODE_MOD,
    PART_ASSET_PQCK,
    TEXTURE_PACK
}

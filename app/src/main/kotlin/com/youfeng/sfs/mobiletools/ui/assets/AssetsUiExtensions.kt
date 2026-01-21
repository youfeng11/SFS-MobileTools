package com.youfeng.sfs.mobiletools.ui.assets

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.youfeng.sfs.mobiletools.R
import com.youfeng.sfs.mobiletools.common.model.AssetType
import com.youfeng.sfs.mobiletools.common.model.ModType

@Composable
fun AssetType.toDisplayName(): String {
    return when (this) {
        is AssetType.Blueprint -> stringResource(R.string.asset_type_blueprint)
        is AssetType.Mod -> {
            val subTypeRes = when (this.type) {
                ModType.CODE_MOD -> R.string.mod_type_code_mod
                ModType.PART_ASSET_PACK -> R.string.mod_type_part_asset_pack
                ModType.TEXTURE_PACK -> R.string.mod_type_texture_pack
            }
            stringResource(R.string.asset_type_mod, stringResource(subTypeRes))
        }
        is AssetType.World -> stringResource(R.string.asset_type_world)
        is AssetType.CustomSolarSystem -> stringResource(R.string.asset_type_custom_solar_system)
        is AssetType.CustomTranslation -> stringResource(R.string.asset_type_custom_translation)
    }
}
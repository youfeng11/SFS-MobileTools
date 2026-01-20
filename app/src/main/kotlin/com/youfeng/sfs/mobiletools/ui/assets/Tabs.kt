package com.youfeng.sfs.mobiletools.ui.assets

import androidx.annotation.StringRes
import com.youfeng.sfs.mobiletools.R

enum class Tabs(@param:StringRes val label: Int) {
    ALL(R.string.tab_all),
    BLUEPRINTS(R.string.tab_blueprints),
    MODS(R.string.tab_mods),
    WORLDS(R.string.tab_worlds),
    CUSTOM_SOLAR_SYSTEMS(R.string.tab_custom_solar_systems),
    CUSTOM_TRANSLATIONS(R.string.tab_custom_translations)
}

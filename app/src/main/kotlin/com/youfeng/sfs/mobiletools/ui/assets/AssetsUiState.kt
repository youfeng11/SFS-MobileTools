package com.youfeng.sfs.mobiletools.ui.assets

import com.youfeng.sfs.mobiletools.domain.model.AssetInfo

data class AssetsUiState(
    val isLoading: Boolean = false,
    val selectedTabIndex: Int = 0,
    val allAssets: List<AssetInfo> = emptyList(),
    val assetToDelete: AssetInfo? = null
)
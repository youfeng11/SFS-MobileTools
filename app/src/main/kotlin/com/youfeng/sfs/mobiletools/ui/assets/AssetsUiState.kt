package com.youfeng.sfs.mobiletools.ui.assets

import com.youfeng.sfs.mobiletools.common.model.AssetInfo

data class AssetsUiState(
    val isLoading: Boolean = false,
    val selectedTabIndex: Int = 0,
    val allAssets: List<AssetInfo> = emptyList(),
    // 为每个 Tab 预先计算好的过滤列表，UI 层直接使用
    val filteredAssetsByTab: List<List<AssetInfo>> = List(Tabs.entries.size) { emptyList() },
    val assetToDelete: AssetInfo? = null
)
package com.youfeng.sfs.mobiletools.ui.assets

import com.youfeng.sfs.mobiletools.common.model.AssetInfo

data class AssetsUiState(
    val isLoading: Boolean = false,
    val selectedTab: Tabs = Tabs.ALL,
    val assets: List<AssetInfo> = emptyList(),
    val assetToDelete: AssetInfo? = null // 将对话框状态也交给 VM 管理
)
package com.youfeng.sfs.mobiletools.ui.assets

import android.net.Uri
import com.youfeng.sfs.mobiletools.domain.model.AssetInfo
import com.youfeng.sfs.mobiletools.domain.model.AssetType

// State: 描述 UI 的所有可见状态
data class AssetsUiState(
    val isLoading: Boolean = false,
    val allAssets: List<AssetInfo> = emptyList(),
    // 对话框状态统一收拢到 State
    val assetToDelete: AssetInfo? = null,
    val showInstallDialog: Boolean = false
)

// Intent: 描述 UI 层触发的所有意图
sealed interface AssetsIntent {
    data object LoadAssets : AssetsIntent

    // 安装相关意图
    data object OpenInstallDialog : AssetsIntent
    data object CloseInstallDialog : AssetsIntent
    data class InstallAsset(val assetType: AssetType, val uri: Uri) : AssetsIntent

    // 删除相关意图
    data class ClickDelete(val asset: AssetInfo) : AssetsIntent
    data object ConfirmDelete : AssetsIntent
    data object CloseDeleteDialog : AssetsIntent
}

// Effect: 描述一次性消费的 UI 事件 (如 Toast, 导航)
sealed interface AssetsEffect {
    data class ShowToast(val message: String) : AssetsEffect
}
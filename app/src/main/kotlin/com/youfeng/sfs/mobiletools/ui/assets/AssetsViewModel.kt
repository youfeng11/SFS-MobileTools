package com.youfeng.sfs.mobiletools.ui.assets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youfeng.sfs.mobiletools.data.repository.AssetsRepository
import com.youfeng.sfs.mobiletools.domain.model.AssetInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AssetsViewModel @Inject constructor(
    private val assetsRepository: AssetsRepository
) : ViewModel() {

    // --- 内部碎片化状态 (Internal States) ---
    private val _rawAssets = MutableStateFlow<List<AssetInfo>>(emptyList())
    private val _assetToDelete = MutableStateFlow<AssetInfo?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val _showInstallDialog = MutableStateFlow(false) // 新增：接管安装弹窗状态

    // --- 暴露给 UI 的单一状态流 (Exposed State) ---
    // 你原来的 combine 逻辑，扩展了 dialog 状态
    val uiState: StateFlow<AssetsUiState> = combine(
        _rawAssets,
        _assetToDelete,
        _isLoading,
        _showInstallDialog
    ) { assets, toDelete, loading, showInstall ->
        AssetsUiState(
            isLoading = loading,
            allAssets = assets,
            assetToDelete = toDelete,
            showInstallDialog = showInstall
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AssetsUiState(isLoading = true)
    )

    // --- 副作用通道 (Effect) ---
    private val _effect = Channel<AssetsEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // --- 唯一的入口 ---
    fun processIntent(intent: AssetsIntent) {
        when (intent) {
            is AssetsIntent.LoadAssets -> loadAssets()

            is AssetsIntent.OpenInstallDialog -> _showInstallDialog.value = true
            is AssetsIntent.CloseInstallDialog -> _showInstallDialog.value = false
            is AssetsIntent.InstallAsset -> installAsset(intent.assetType, intent.uri)

            is AssetsIntent.ClickDelete -> _assetToDelete.value = intent.asset
            is AssetsIntent.CloseDeleteDialog -> _assetToDelete.value = null
            is AssetsIntent.ConfirmDelete -> confirmDelete()
        }
    }

    private fun loadAssets() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val list = assetsRepository.getAssetsList()
                _rawAssets.value = list
            } catch (e: Exception) {
                Timber.e(e, "加载资源出错！")
                sendEffect(AssetsEffect.ShowToast("加载资源失败: ${e.message}"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun confirmDelete() {
        val asset = _assetToDelete.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                assetsRepository.deleteAsset(asset)
                val updatedList = assetsRepository.getAssetsList()

                // 串行更新内部状态
                _rawAssets.value = updatedList
                _assetToDelete.value = null
                sendEffect(AssetsEffect.ShowToast("删除成功"))
            } catch (e: Exception) {
                Timber.e(e, "删除资源失败")
                sendEffect(AssetsEffect.ShowToast("删除失败: ${e.message}"))
                _assetToDelete.value = null
            }
        }
    }

    private fun installAsset(
        assetType: com.youfeng.sfs.mobiletools.domain.model.AssetType,
        uri: android.net.Uri
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            // 开始安装：关闭弹窗，显示加载
            _showInstallDialog.value = false
            _isLoading.value = true
            try {
                assetsRepository.installAsset(assetType, uri)
                val updatedList = assetsRepository.getAssetsList()
                _rawAssets.value = updatedList
                sendEffect(AssetsEffect.ShowToast("安装成功"))
            } catch (e: Exception) {
                Timber.e(e, "安装资源失败")
                sendEffect(AssetsEffect.ShowToast("安装失败: ${e.message}"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun sendEffect(effect: AssetsEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}
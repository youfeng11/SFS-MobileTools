package com.youfeng.sfs.mobiletools.ui.assets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youfeng.sfs.mobiletools.domain.model.AssetInfo
import com.youfeng.sfs.mobiletools.data.repository.AssetsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AssetsViewModel @Inject constructor(
    private val assetsRepository: AssetsRepository
) : ViewModel() {

    // Internal state streams
    private val _rawAssets = MutableStateFlow<List<AssetInfo>>(emptyList())
    private val _selectedTabIndex = MutableStateFlow(0) // 直接存储索引而不是 Tab 枚举
    private val _assetToDelete = MutableStateFlow<AssetInfo?>(null)
    private val _isLoading = MutableStateFlow(false)

    // Public UI state with filtered assets per tab
    val uiState: StateFlow<AssetsUiState> = combine(
        _rawAssets,
        _selectedTabIndex,
        _assetToDelete,
        _isLoading
    ) { assets, tabIndex, toDelete, loading ->
        AssetsUiState(
            isLoading = loading,
            selectedTabIndex = tabIndex,
            allAssets = assets,
            assetToDelete = toDelete
        ).also {
            Timber.i(it.toString())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AssetsUiState(isLoading = true)
    )

    fun loadAssets() {
        Timber.i("加载资源")
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.update { true }
            try {
                val list = assetsRepository.getAssetsList()
                _rawAssets.value = list
            } catch (e: Exception) {
                Timber.e(e, "加载资源出错！")
                _rawAssets.value = emptyList()
            } finally {
                _isLoading.update { false }
            }
        }
    }

    // 统一的 Tab 切换入口，只接受索引
    fun onTabIndexChanged(index: Int) {
        if (index in Tabs.entries.indices) {
            _selectedTabIndex.value = index
        }
    }

    fun showDeleteConfirmation(asset: AssetInfo?) {
        _assetToDelete.value = asset
    }

    fun confirmDelete() {
        val asset = _assetToDelete.value ?: return

        viewModelScope.launch(Dispatchers.IO) {
            assetsRepository.deleteAsset(asset)
            val updatedList = assetsRepository.getAssetsList()
            _rawAssets.value = updatedList
            _assetToDelete.value = null
        }
    }
}
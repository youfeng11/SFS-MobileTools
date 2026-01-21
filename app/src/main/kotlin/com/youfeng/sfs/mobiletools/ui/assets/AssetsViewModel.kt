package com.youfeng.sfs.mobiletools.ui.assets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youfeng.sfs.mobiletools.common.model.AssetInfo
import com.youfeng.sfs.mobiletools.common.model.AssetType
import com.youfeng.sfs.mobiletools.data.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssetsViewModel @Inject constructor(
    private val dataRepository: DataRepository
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
            // 为每个 Tab 预先计算好过滤后的列表
            filteredAssetsByTab = Tabs.entries.map { tab ->
                filterAssets(assets, tab)
            },
            assetToDelete = toDelete
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AssetsUiState(isLoading = true)
    )

    init {
        loadAssets()
    }

    fun loadAssets() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.update { true }
            try {
                val list = dataRepository.getAssetsList()
                _rawAssets.value = list
            } catch (e: Exception) {
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
            dataRepository.deleteAsset(asset)
            val updatedList = dataRepository.getAssetsList()
            _rawAssets.value = updatedList
            _assetToDelete.value = null
        }
    }

    private fun filterAssets(assets: List<AssetInfo>, tab: Tabs): List<AssetInfo> {
        return when (tab) {
            Tabs.ALL -> assets
            Tabs.BLUEPRINTS -> assets.filter { it.type is AssetType.Blueprint }
            Tabs.MODS -> assets.filter { it.type is AssetType.Mod }
            Tabs.WORLDS -> assets.filter { it.type is AssetType.World }
            Tabs.CUSTOM_SOLAR_SYSTEMS -> assets.filter { it.type is AssetType.CustomSolarSystem }
            Tabs.CUSTOM_TRANSLATIONS -> assets.filter { it.type is AssetType.CustomTranslation }
        }
    }
}
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

    // 1. Raw Data Streams (Internal)
    private val _rawAssets = MutableStateFlow<List<AssetInfo>>(emptyList())
    private val _selectedTab = MutableStateFlow(Tabs.ALL)
    private val _assetToDelete = MutableStateFlow<AssetInfo?>(null)
    private val _isLoading = MutableStateFlow(false)

    // 2. Combined UI State (Reactive Output)
    // 只要 Tab 变了，或者 原始列表变了，UiState 自动重新计算
    val uiState: StateFlow<AssetsUiState> = combine(
        _rawAssets,
        _selectedTab,
        _assetToDelete,
        _isLoading
    ) { assets, tab, toDelete, loading ->
        AssetsUiState(
            isLoading = loading,
            selectedTab = tab,
            assets = filterAssets(assets, tab),
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

    // 3. Operations (Actions)
    fun loadAssets() {
        viewModelScope.launch(Dispatchers.IO) { // Switch to IO thread for file operations
            _isLoading.update { true }
            try {
                // 模拟耗时或磁盘IO
                val list = dataRepository.getAssetsList() 
                _rawAssets.value = list
            } catch (e: Exception) {
                // Handle error state if needed
                _rawAssets.value = emptyList()
            } finally {
                _isLoading.update { false }
            }
        }
    }

    fun onTabSelected(tab: Tabs) {
        _selectedTab.value = tab
    }

    fun showDeleteConfirmation(asset: AssetInfo?) {
        _assetToDelete.value = asset
    }

    fun confirmDelete() {
        val asset = _assetToDelete.value ?: return
        
        viewModelScope.launch(Dispatchers.IO) {
            dataRepository.deleteAsset(asset)
            // Refresh list after delete
            val updatedList = dataRepository.getAssetsList()
            _rawAssets.value = updatedList
            _assetToDelete.value = null
        }
    }

    // Pure function for filtering
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
package com.youfeng.sfs.mobiletools.ui.assets

import androidx.lifecycle.ViewModel
import com.youfeng.sfs.mobiletools.common.model.AssetInfo
import com.youfeng.sfs.mobiletools.data.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AssetsViewModel @Inject constructor(
    private val dataRepository: DataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssetsUiState())
    val uiState: StateFlow<AssetsUiState> = _uiState

    init {
        updateAssetsList()
    }

    fun updateAssetsList() {
        _uiState.update { it.copy(assetsList = dataRepository.getAssetsList()) }
    }

    fun deleteAsset(asset: AssetInfo) {
        dataRepository.deleteAsset(asset)
        updateAssetsList() // 删除后立即刷新 UI
    }
}
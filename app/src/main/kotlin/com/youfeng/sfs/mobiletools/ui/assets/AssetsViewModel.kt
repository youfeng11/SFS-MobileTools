package com.youfeng.sfs.mobiletools.ui.assets

import androidx.lifecycle.ViewModel
import com.youfeng.sfs.mobiletools.data.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class AssetsViewModel @Inject constructor(
    private val dataRepository: DataRepository
) : ViewModel() {

    fun test(): String {
        return dataRepository.getCustomTranslationsList().toString()
    }
}
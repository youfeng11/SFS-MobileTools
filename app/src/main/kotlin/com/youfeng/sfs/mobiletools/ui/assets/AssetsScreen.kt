package com.youfeng.sfs.mobiletools.ui.assets

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.youfeng.sfs.mobiletools.R
import com.youfeng.sfs.mobiletools.common.model.AssetInfo
import com.youfeng.sfs.mobiletools.common.model.AssetType
import com.youfeng.sfs.mobiletools.common.model.ModType
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun AssetsScreen(viewModel: AssetsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Surface {
        AssetsLayout(
            uiState = uiState,
            onDeleteAsset = { viewModel.deleteAsset(it) } // 传递 ViewModel 方法
        )
    }
    
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        viewModel.updateAssetsList()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetsLayout(
    uiState: AssetsUiState,
    onDeleteAsset: (AssetInfo) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var selectedTab by remember { mutableStateOf(Tabs.ALL) }

    var assetToDelete by remember { mutableStateOf<AssetInfo?>(null) }

    // 确认删除对话框
    assetToDelete?.let { asset ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { assetToDelete = null },
            title = { Text("确认删除") },
            text = { Text("确定要删除 '${asset.name}' 吗？此操作不可撤销。") },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        onDeleteAsset(asset)
                        assetToDelete = null
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { assetToDelete = null }) {
                    Text("取消")
                }
            }
        )
    }

    val filteredAssets = remember(selectedTab, uiState.assetsList) {
        uiState.assetsList?.run {
            when (selectedTab) {
                Tabs.BLUEPRINTS ->
                    filter { it.type is AssetType.Blueprint }
                Tabs.MODS ->
                    filter { it.type is AssetType.Mod }
                Tabs.WORLDS ->
                    filter { it.type is AssetType.World }
                Tabs.CUSTOM_SOLAR_SYSTEMS ->
                    filter { it.type is AssetType.CustomSolarSystem }
                Tabs.CUSTOM_TRANSLATIONS ->
                    filter { it.type is AssetType.CustomTranslation }
                else -> this
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
                Column {
                    TopAppBar(
                        title = { Text(stringResource(R.string.navigation_assets)) },
                        scrollBehavior = scrollBehavior
                    )
                    SecondaryScrollableTabRow(
                        selectedTabIndex = Tabs.entries.indexOf(selectedTab),
                        edgePadding = 0.dp,
                        divider = {
                            HorizontalDivider()
                        } 
                    ) {
                        Tabs.entries.forEach { tab ->
                            Tab(
                                selected = selectedTab == tab,
                                onClick = { selectedTab = tab },
                                text = { Text(stringResource(tab.label)) }
                            )
                        }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            filteredAssets?.let {
                items(
                    items = it,
                    key = { it.name } // 如果 name 唯一
                ) { asset ->
                    AssetItem(asset, { assetToDelete = asset })
                }
            } ?: item { UnavailableText() }
        }
    }
}

@Composable
fun AssetItem(
    asset: AssetInfo, 
    onDeleteClick: () -> Unit // 新增回调
) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
        Row( // 使用 Row 布局
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = asset.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = when (asset.type) {
                        is AssetType.Blueprint -> stringResource(R.string.asset_type_blueprint)
                        is AssetType.Mod -> stringResource(R.string.asset_type_mod, stringResource(
                            when (asset.type.type) {
                                ModType.CODE_MOD -> R.string.mod_type_code_mod
                                ModType.PART_ASSET_PACK -> R.string.mod_type_part_asset_pack
                                ModType.TEXTURE_PACK -> R.string.mod_type_texture_pack
                            }
                        ))
                        is AssetType.World -> stringResource(R.string.asset_type_world)
                        is AssetType.CustomSolarSystem -> stringResource(R.string.asset_type_custom_solar_system)
                        is AssetType.CustomTranslation -> stringResource(R.string.asset_type_custom_translation)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // 添加删除按钮
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.delete_24px),
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun UnavailableText() {
    Card {
        Text("不可用", modifier = Modifier.padding(24.dp))
    }
}

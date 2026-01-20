package com.youfeng.sfs.mobiletools.ui.assets

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.youfeng.sfs.mobiletools.R
import com.youfeng.sfs.mobiletools.common.model.AssetInfo
import com.youfeng.sfs.mobiletools.common.model.AssetType
import androidx.compose.material3.HorizontalDivider

@Composable
fun AssetsScreen(viewModel: AssetsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Surface {
        AssetsLayout(uiState)
    }
    
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        viewModel.updateAssetsList()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetsLayout(
    uiState: AssetsUiState
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var selectedTab by remember { mutableStateOf(Tabs.ALL) }

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
                    AssetItem(asset)
                }
            } ?: item { UnavailableText() }
        }
    }
}

@Composable
fun AssetItem(asset: AssetInfo) {
    Card(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = asset.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(
                    when (asset.type) {
                        is AssetType.Blueprint -> R.string.asset_type_blueprint
                        is AssetType.Mod -> R.string.asset_type_mod
                        is AssetType.World -> R.string.asset_type_world
                        is AssetType.CustomSolarSystem -> R.string.asset_type_custom_solar_system
                        is AssetType.CustomTranslation -> R.string.asset_type_custom_translatio
                    }
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun UnavailableText() {
    Card {
        Text("不可用", modifier = Modifier.padding(24.dp))
    }
}

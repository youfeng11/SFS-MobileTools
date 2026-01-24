package com.youfeng.sfs.mobiletools.ui.assets

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.youfeng.sfs.mobiletools.R
import com.youfeng.sfs.mobiletools.common.model.AssetInfo
import com.youfeng.sfs.mobiletools.common.model.AssetType
import com.youfeng.sfs.mobiletools.ui.util.formatSizeFromKB

@Composable
fun AssetsScreen(viewModel: AssetsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        viewModel.loadAssets()
    }

    AssetsLayout(
        uiState = uiState,
        onTabIndexChanged = viewModel::onTabIndexChanged,
        onDeleteClick = viewModel::showDeleteConfirmation,
        onConfirmDelete = viewModel::confirmDelete,
        onDismissDelete = { viewModel.showDeleteConfirmation(null) }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AssetsLayout(
    uiState: AssetsUiState,
    onTabIndexChanged: (Int) -> Unit,
    onDeleteClick: (AssetInfo) -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDelete: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val tabs = Tabs.entries
    val pagerState = rememberPagerState(
        initialPage = uiState.selectedTabIndex,
        pageCount = { tabs.size }
    )
    rememberCoroutineScope()

    // ViewModel 状态变化时同步到 Pager（例如点击 Tab 时）
    LaunchedEffect(uiState.selectedTabIndex) {
        if (pagerState.currentPage != uiState.selectedTabIndex) {
            pagerState.animateScrollToPage(uiState.selectedTabIndex)
        }
    }

    // Delete confirmation dialog
    if (uiState.assetToDelete != null) {
        AlertDialog(
            onDismissRequest = onDismissDelete,
            title = { Text("确定删除吗？") },
            text = { Text("确定要删除 \"${uiState.assetToDelete.name}\" 吗？\n此操作不可撤销！") },
            confirmButton = {
                TextButton(onClick = onConfirmDelete) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDelete) { Text("取消") }
            }
        )
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
                    selectedTabIndex = pagerState.currentPage,
                    edgePadding = 4.dp
                ) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                onTabIndexChanged(index)
                            },
                            text = { Text(stringResource(tab.label)) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) { pageIndex ->
            // 直接从 UiState 获取该页面的过滤列表
            val currentTab = tabs[pageIndex]
            val filteredAssets by remember(uiState.allAssets, currentTab) {
                derivedStateOf {
                    when (currentTab) {
                        Tabs.ALL -> uiState.allAssets
                        Tabs.BLUEPRINTS -> uiState.allAssets.filter { it.type is AssetType.Blueprint }
                        Tabs.MODS -> uiState.allAssets.filter { it.type is AssetType.Mod }
                        Tabs.WORLDS -> uiState.allAssets.filter { it.type is AssetType.World }
                        Tabs.CUSTOM_SOLAR_SYSTEMS -> uiState.allAssets.filter { it.type is AssetType.CustomSolarSystem }
                        Tabs.CUSTOM_TRANSLATIONS -> uiState.allAssets.filter { it.type is AssetType.CustomTranslation }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    filteredAssets.isEmpty() -> {
                        UnavailableText(modifier = Modifier.align(Alignment.Center))
                    }

                    else -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(
                                items = filteredAssets,
                                key = { "${it.name}${it.type}" }
                            ) { asset ->
                                AssetItem(
                                    asset = asset,
                                    onDeleteClick = { onDeleteClick(asset) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AssetItem(
    asset: AssetInfo,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(
                    when (asset.type) {
                        is AssetType.Blueprint -> R.drawable.draft_24px
                        is AssetType.Mod -> R.drawable.extension_24px
                        is AssetType.World -> R.drawable.save_24px
                        is AssetType.CustomSolarSystem -> R.drawable.planet_24px
                        is AssetType.CustomTranslation -> R.drawable.translate_24px
                    }
                ),
                contentDescription = null,
                modifier = Modifier.padding(start = 4.dp, end = 16.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = asset.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${asset.type.toDisplayName()} | ${asset.size.formatSizeFromKB()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
fun UnavailableText(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "这里空空如也",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
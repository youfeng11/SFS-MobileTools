package com.youfeng.sfs.mobiletools.ui.assets

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.youfeng.sfs.mobiletools.R
import com.youfeng.sfs.mobiletools.domain.model.AssetInfo
import com.youfeng.sfs.mobiletools.domain.model.AssetType
import com.youfeng.sfs.mobiletools.domain.model.ModType
import com.youfeng.sfs.mobiletools.ui.util.formatSizeFromKB
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.foundation.layout.calculateStartPadding

@Composable
fun AssetsScreen(
    openInstallDialog: Boolean = false,
    viewModel: AssetsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        viewModel.loadAssets()
    }

    AssetsLayout(
        uiState = uiState,
        openInstallDialog = openInstallDialog,
        onTabIndexChanged = viewModel::onTabIndexChanged,
        onDeleteClick = viewModel::showDeleteConfirmation,
        onConfirmDelete = viewModel::confirmDelete,
        onDismissDelete = { viewModel.showDeleteConfirmation(null) },
        onInstallAsset = viewModel::installAsset
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AssetsLayout(
    uiState: AssetsUiState,
    openInstallDialog: Boolean = false,
    onTabIndexChanged: (Int) -> Unit,
    onDeleteClick: (AssetInfo) -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDelete: () -> Unit,
    onInstallAsset: (AssetType, Uri) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val tabs = Tabs.entries
    val pagerState = rememberPagerState(
        initialPage = uiState.selectedTabIndex,
        pageCount = { tabs.size }
    )
    rememberCoroutineScope()

    var showInstallDialog by remember { mutableStateOf(openInstallDialog) }

    // Open dialog when openInstallDialog parameter changes
    LaunchedEffect(openInstallDialog) {
        if (openInstallDialog) {
            showInstallDialog = true
        }
    }

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

    // Install Asset Dialog
    if (showInstallDialog) {
        InstallAssetDialog(
            onDismiss = { showInstallDialog = false },
            onInstall = { assetType, uri ->
                onInstallAsset(assetType, uri)
                showInstallDialog = false
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
                    edgePadding = WindowInsets.safeDrawing
    .only(WindowInsetsSides.Horizontal)
    .asPaddingValues()
    .calculateStartPadding(LayoutDirection.Ltr)
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showInstallDialog = true }
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.add_24px),
                    contentDescription = "安装资源"
                )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstallAssetDialog(
    onDismiss: () -> Unit,
    onInstall: (AssetType, Uri) -> Unit
) {
    var selectedAssetTypeIndex by remember { mutableIntStateOf(0) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val assetTypes = remember {
        listOf(
            AssetType.Blueprint,
            AssetType.Mod(ModType.PART_ASSET_PACK),
            AssetType.World,
            AssetType.CustomSolarSystem,
            AssetType.CustomTranslation
        )
    }

    val assetTypeLabels = listOf(
        "蓝图",
        "模组",
        "存档",
        "星系",
        "翻译"
    )

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        selectedUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("安装资源") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "选择资源类型",
                    style = MaterialTheme.typography.labelLarge
                )

                // Scrollable FilterChips with gradient edges as scroll indicators
                Box(modifier = Modifier.fillMaxWidth()) {
                    val scrollState = rememberScrollState()
                    val showLeftIndicator by remember {
                        derivedStateOf { scrollState.value > 0 }
                    }
                    val showRightIndicator by remember {
                        derivedStateOf { scrollState.value < scrollState.maxValue }
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        assetTypeLabels.forEachIndexed { index, label ->
                            FilterChip(
                                selected = selectedAssetTypeIndex == index,
                                onClick = { selectedAssetTypeIndex = index },
                                label = { Text(label) },
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                    }
                    
                    // Left gradient indicator
                    if (showLeftIndicator) {
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(48.dp)
                                .align(Alignment.CenterStart)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            AlertDialogDefaults.containerColor,
                                            AlertDialogDefaults.containerColor.copy(alpha = 0f)
                                        )
                                    )
                                )
                        )
                    }
                    
                    // Right gradient indicator
                    if (showRightIndicator) {
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(48.dp)
                                .align(Alignment.CenterEnd)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            AlertDialogDefaults.containerColor.copy(alpha = 0f),
                                            AlertDialogDefaults.containerColor
                                        )
                                    )
                                )
                        )
                    }
                }

                OutlinedButton(
                    onClick = {
                        filePickerLauncher.launch(
                            when (assetTypes[selectedAssetTypeIndex]) {
                                is AssetType.Blueprint -> arrayOf("*/*") // 文件夹
                                is AssetType.Mod -> arrayOf("*/*")
                                is AssetType.World -> arrayOf("*/*")
                                is AssetType.CustomSolarSystem -> arrayOf("*/*")
                                is AssetType.CustomTranslation -> arrayOf("text/plain", "application/octet-stream")
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.folder_open_24px),
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = selectedUri?.lastPathSegment ?: "选择文件"
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedUri?.let { uri ->
                        onInstall(assetTypes[selectedAssetTypeIndex], uri)
                    }
                },
                enabled = selectedUri != null
            ) {
                Text("安装")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
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
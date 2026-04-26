package com.youfeng.sfs.mobiletools.ui.assets

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.platform.LocalLayoutDirection
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

@Composable
fun AssetsScreen(
    openInstallDialog: Boolean = false,
    viewModel: AssetsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // 处理外部传入的打开弹窗指令
    LaunchedEffect(openInstallDialog) {
        if (openInstallDialog) {
            viewModel.processIntent(AssetsIntent.OpenInstallDialog)
        }
    }

    // 初始加载数据
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        viewModel.processIntent(AssetsIntent.LoadAssets)
    }

    // 处理一次性副作用 (Effect)
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AssetsEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 将 Intent 委托下去
    AssetsLayout(
        uiState = uiState,
        onIntent = viewModel::processIntent
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AssetsLayout(
    uiState: AssetsUiState,
    onIntent: (AssetsIntent) -> Unit // 单向数据流：唯一向上通讯的通道
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val tabs = Tabs.entries
    val pagerState = rememberPagerState(
        initialPage = uiState.selectedTabIndex,
        pageCount = { tabs.size }
    )
    val coroutineScope = rememberCoroutineScope()

    // 监听状态变化同步到 Pager
    LaunchedEffect(uiState.selectedTabIndex) {
        pagerState.animateScrollToPage(uiState.selectedTabIndex)
    }

    // 删除确认弹窗 (完全受控于 UiState)
    if (uiState.assetToDelete != null) {
        AlertDialog(
            onDismissRequest = { onIntent(AssetsIntent.CloseDeleteDialog) },
            title = { Text("确定删除吗？") },
            text = { Text("确定要删除 \"${uiState.assetToDelete.name}\" 吗？\n此操作不可撤销！") },
            confirmButton = {
                TextButton(
                    onClick = { onIntent(AssetsIntent.ConfirmDelete) },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(AssetsIntent.CloseDeleteDialog) }) { Text("取消") }
            }
        )
    }

    // 安装弹窗 (完全受控于 UiState)
    if (uiState.showInstallDialog) {
        InstallAssetDialog(
            onDismiss = { onIntent(AssetsIntent.CloseInstallDialog) },
            onInstall = { assetType, uri ->
                onIntent(AssetsIntent.InstallAsset(assetType, uri))
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
                    edgePadding = TopAppBarDefaults.windowInsets
                        .asPaddingValues()
                        .calculateStartPadding(LocalLayoutDirection.current)
                ) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                onIntent(AssetsIntent.SelectTab(index))
                            },
                            text = { Text(stringResource(tab.label)) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onIntent(AssetsIntent.OpenInstallDialog) }
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
                .padding(top = innerPadding.calculateTopPadding(), bottom = innerPadding.calculateBottomPadding())
                .fillMaxSize()
        ) { pageIndex ->
            // UI 的派生状态（Derived State）
            // 列表过滤纯属 UI 展示逻辑，放在这里使用 derivedStateOf 是完全合理的 MVI 实践
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

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                        end = innerPadding.calculateEndPadding(LocalLayoutDirection.current)
                    )
            ) {
                when {
                    uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                    filteredAssets.isEmpty() -> UnavailableText(Modifier.align(Alignment.Center))
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            items(items = filteredAssets, key = { "${it.name}${it.type}" }) { asset ->
                                AssetItem(
                                    asset = asset,
                                    onDeleteClick = { onIntent(AssetsIntent.ClickDelete(asset)) }
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
    LocalContext.current

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
                                leadingIcon = if (selectedAssetTypeIndex == index) {
                                    {
                                        Icon(
                                            imageVector = ImageVector.vectorResource(R.drawable.check_24px),
                                            contentDescription = "Localized Description",
                                            modifier = Modifier.size(FilterChipDefaults.IconSize),
                                        )
                                    }
                                } else {
                                    null
                                },
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
                        filePickerLauncher.launch(arrayOf("*/*"))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.folder_open_24px),
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
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
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.extraLarge
                    ),
                contentAlignment = Alignment.Center
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
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
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
            IconButton(
                onClick = onDeleteClick,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.delete_24px),
                    contentDescription = "Delete"
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
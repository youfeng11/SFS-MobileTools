package com.youfeng.sfs.mobiletools.ui.assets

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.LaunchedEffect

@Composable
fun AssetsScreen(viewModel: AssetsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Optional: Reload when screen becomes active if data might change externally
    LaunchedEffect(Unit) {
        viewModel.loadAssets() 
    }

    AssetsLayout(
        uiState = uiState,
        onTabSelected = viewModel::onTabSelected,
        onDeleteClick = viewModel::showDeleteConfirmation,
        onConfirmDelete = viewModel::confirmDelete,
        onDismissDelete = { viewModel.showDeleteConfirmation(null) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetsLayout(
    uiState: AssetsUiState,
    onTabSelected: (Tabs) -> Unit,
    onDeleteClick: (AssetInfo) -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDelete: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    // Dialog Logic
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
                    selectedTabIndex = Tabs.entries.indexOf(uiState.selectedTab),
                    edgePadding = 0.dp
                ) {
                    Tabs.entries.forEach { tab ->
                        Tab(
                            selected = uiState.selectedTab == tab,
                            onClick = { onTabSelected(tab) },
                            text = { Text(stringResource(tab.label)) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        // Content Area
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.assets.isEmpty()) {
                UnavailableText(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(
                        items = uiState.assets,
                        key = { it.name } // Ensure name is unique, otherwise use a fallback ID
                    ) { asset ->
                        AssetItem(asset = asset, onDeleteClick = { onDeleteClick(asset) })
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
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
        Row(
            modifier = Modifier.padding(16.dp), // Adjusted padding
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = asset.name, style = MaterialTheme.typography.titleMedium)
                // Use the helper extension here for cleaner code
                Text(
                    text = asset.type.toDisplayName(),
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
    // Improved Empty State
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // You could add an Icon here later
        Text(
            text = "这里空空如也", // Or string resource
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

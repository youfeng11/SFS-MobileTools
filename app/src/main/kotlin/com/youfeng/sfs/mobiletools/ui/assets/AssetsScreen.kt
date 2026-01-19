package com.youfeng.sfs.mobiletools.ui.assets

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.youfeng.sfs.mobiletools.R
import androidx.compose.material3.HorizontalDivider

@Composable
fun AssetsScreen(viewModel: AssetsViewModel = hiltViewModel()) {
    Surface {
        AssetsLayout(viewModel::test)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetsLayout(
    test: () -> String
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val tabs = listOf("全部", "蓝图", "世界", "星系", "翻译")
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
                Column {
                    TopAppBar(
                        title = { Text(stringResource(R.string.navigation_assets)) },
                        scrollBehavior = scrollBehavior
                    )
                    SecondaryTabRow(
                        selectedTabIndex = selectedTabIndex,
                        divider = {
                            HorizontalDivider()
                        } 
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = { Text(text = title) }
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
            items(20) { index ->
                Card(modifier = Modifier.padding(12.dp)) {
                    Text("分类 ${tabs[selectedTabIndex]} - 内容 $index", modifier = Modifier.padding(24.dp))
                }
            }
        }
    }
}
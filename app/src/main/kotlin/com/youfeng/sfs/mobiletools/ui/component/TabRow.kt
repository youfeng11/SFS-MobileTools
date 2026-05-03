package com.youfeng.sfs.mobiletools.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.TabIndicatorScope
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsetsSecondaryScrollableTabRow(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets.only(WindowInsetsSides.Horizontal),
    containerColor: Color = TabRowDefaults.secondaryContainerColor,
    contentColor: Color = TabRowDefaults.secondaryContentColor,
    edgePadding: Dp = TabRowDefaults.ScrollableTabRowEdgeStartPadding,
    indicator: @Composable TabIndicatorScope.() -> Unit = @Composable {
        TabRowDefaults.SecondaryIndicator(
            Modifier.tabIndicatorOffset(selectedTabIndex, matchContentSize = false)
        )
    },
    divider: @Composable () -> Unit = @Composable { HorizontalDivider() },
    tabs: @Composable () -> Unit
) {
    // 外层 Box 接管全屏宽度的背景和垂直方向的 Insets
    Box(
        modifier = Modifier
            .background(containerColor)
    ) {
        // 将分割线单独放置在底部，它会自然撑满屏幕宽度，不受边距影响
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
        ) {
            divider()
        }

        // 调用原生的 API，但传入改造后的参数
        SecondaryScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = modifier
                .windowInsetsPadding(windowInsets),
            containerColor = Color.Transparent, // 关键：背景设为透明，露出外层的颜色
            contentColor = contentColor,
            edgePadding = edgePadding,      // 关键：动态计算并传入包含了安全区的边距
            indicator = indicator,
            divider = {},                       // 关键：传入空内容，隐藏组件自带的分割线
            tabs = tabs
        )
    }
}
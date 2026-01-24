package com.youfeng.sfs.mobiletools.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.youfeng.sfs.mobiletools.R
import com.youfeng.sfs.mobiletools.ui.assets.AssetsScreen
import com.youfeng.sfs.mobiletools.ui.home.HomeScreen
import com.youfeng.sfs.mobiletools.ui.settings.SettingsScreen
import com.youfeng.sfs.mobiletools.ui.util.Navigator
import com.youfeng.sfs.mobiletools.ui.util.rememberNavigationState
import com.youfeng.sfs.mobiletools.ui.util.toEntries
import kotlinx.serialization.Serializable

// --- 1. 路由定义 ---
@Serializable
data object MainGraph : NavKey

@Serializable
data object Home : NavKey

@Serializable
data object Assets : NavKey

@Serializable
data object Settings : NavKey

@Serializable
data object Detail : NavKey

// --- 2. 顶级导航定义 ---
enum class TopLevelDestination(
    val route: NavKey,
    @param:StringRes val label: Int,
    @param:DrawableRes val unselectedIcon: Int,
    @param:DrawableRes val selectedIcon: Int
) {
    HOME(
        route = Home,
        label = R.string.navigation_home,
        unselectedIcon = R.drawable.home_24px,
        selectedIcon = R.drawable.home_fill_24px
    ),
    ASSETS(
        route = Assets,
        label = R.string.navigation_assets,
        unselectedIcon = R.drawable.widgets_24px,
        selectedIcon = R.drawable.widgets_fill_24px
    ),
    SETTINGS(
        route = Settings,
        label = R.string.navigation_settings,
        unselectedIcon = R.drawable.settings_24px,
        selectedIcon = R.drawable.settings_fill_24px
    )
}

// --- 3. 动画配置常量 ---
private const val ANIMATION_DURATION = 300
private const val FADE_DURATION = 200

// --- 4. 根导航 ---
@Composable
fun Navigation() {
    val navigationState = rememberNavigationState(
        startRoute = MainGraph,
        topLevelRoutes = setOf(MainGraph)
    )

    val navigator = remember { Navigator(navigationState) }

    val entryProvider = entryProvider {
        entry<MainGraph> {
            MainGraphScreen(navigator)
        }
        // 详情页使用从右侧滑入的动画
        entry<Detail>(
            metadata = NavDisplay.transitionSpec {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(ANIMATION_DURATION)
                ) + fadeIn(animationSpec = tween(ANIMATION_DURATION)) togetherWith
                        slideOutHorizontally(
                            targetOffsetX = { -it / 3 },
                            animationSpec = tween(ANIMATION_DURATION)
                        ) + fadeOut(animationSpec = tween(ANIMATION_DURATION))
            } + NavDisplay.popTransitionSpec {
                slideInHorizontally(
                    initialOffsetX = { -it / 3 },
                    animationSpec = tween(ANIMATION_DURATION)
                ) + fadeIn(animationSpec = tween(ANIMATION_DURATION)) togetherWith
                        slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec = tween(ANIMATION_DURATION)
                        ) + fadeOut(animationSpec = tween(ANIMATION_DURATION))
            } + NavDisplay.predictivePopTransitionSpec {
                // 预见式返回动画：用户滑动时的实时预览
                slideInHorizontally(
                    initialOffsetX = { -it / 3 },
                    animationSpec = tween(ANIMATION_DURATION)
                ) + fadeIn(animationSpec = tween(ANIMATION_DURATION)) togetherWith
                        slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec = tween(ANIMATION_DURATION)
                        ) + fadeOut(animationSpec = tween(ANIMATION_DURATION))
            }
        ) {
            SettingsScreen()
        }
    }

    NavDisplay(
        entries = navigationState.toEntries(entryProvider),
        onBack = { navigator.goBack() }
    )
}

// --- 5. 主图导航 (包含底部导航栏) ---
@Composable
private fun MainGraphScreen(rootNavigator: Navigator) {
    val mainNavigationState = rememberNavigationState(
        startRoute = Home,
        topLevelRoutes = setOf(Home, Assets, Settings)
    )

    val mainNavigator = remember { Navigator(mainNavigationState) }

    val mainEntryProvider = entryProvider {
        // 底部导航 Tab 使用淡入淡出动画
        entry<Home>(
            metadata = NavDisplay.predictivePopTransitionSpec {
                fadeIn(animationSpec = tween(FADE_DURATION)) togetherWith
                        fadeOut(animationSpec = tween(FADE_DURATION))
            }
        ) {
            HomeScreen(
                onNavigateToDetail = {
                    rootNavigator.navigate(Detail)
                }
            )
        }
        entry<Assets>(
            metadata = NavDisplay.predictivePopTransitionSpec {
                fadeIn(animationSpec = tween(FADE_DURATION)) togetherWith
                        fadeOut(animationSpec = tween(FADE_DURATION))
            }
        ) {
            AssetsScreen()
        }
        entry<Settings>(
            metadata = NavDisplay.predictivePopTransitionSpec {
                fadeIn(animationSpec = tween(FADE_DURATION)) togetherWith
                        fadeOut(animationSpec = tween(FADE_DURATION))
            }
        ) {
            SettingsScreen()
        }
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            TopLevelDestination.entries.forEach { destination ->
                val selected = mainNavigationState.topLevelRoute == destination.route

                item(
                    selected = selected,
                    onClick = {
                        mainNavigator.navigate(destination.route)
                    },
                    icon = {
                        Icon(
                            imageVector = ImageVector.vectorResource(
                                if (selected) destination.selectedIcon else destination.unselectedIcon
                            ),
                            contentDescription = stringResource(destination.label)
                        )
                    },
                    label = { Text(stringResource(destination.label)) }
                )
            }
        }
    ) {
        NavDisplay(
            entries = mainNavigationState.toEntries(mainEntryProvider),
            onBack = { mainNavigator.goBack() },
            modifier = Modifier.fillMaxSize()
        )
    }
}
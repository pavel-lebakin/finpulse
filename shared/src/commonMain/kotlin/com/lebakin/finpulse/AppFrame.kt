package com.lebakin.finpulse

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.lebakin.finpulse.core.navigation.ComposeRootNavigator
import com.lebakin.finpulse.core.navigation.Destination
import com.lebakin.finpulse.core.uikit.BackArrow
import com.lebakin.finpulse.core.uikit.BottomNavBar
import com.lebakin.finpulse.core.uikit.BottomNavItem
import com.lebakin.finpulse.core.uikit.FinPulseIcons
import com.lebakin.finpulse.core.uikit.MenuButton
import com.lebakin.finpulse.feature.assistant.presentation.navigation.AssistantDestination
import com.lebakin.finpulse.feature.insights.presentation.navigation.InsightsDestination
import com.lebakin.finpulse.feature.onboarding.presentation.navigation.OnboardingDestination
import com.lebakin.finpulse.feature.scanner.presentation.navigation.ScannerDestination
import com.lebakin.finpulse.feature.settings.presentation.navigation.SettingsDestination
import kotlinx.coroutines.launch

private val WideLayout = 600.dp

private val NoInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)

private const val SECTION_SLIDE_MILLIS = 250

private enum class AppSection(
    val label: String,
    val icon: ImageVector,
) {
    Home(label = "Home", icon = FinPulseIcons.Home),
    Scan(label = "Scan", icon = FinPulseIcons.Scan),
    Ask(label = "Assistant", icon = FinPulseIcons.Ask),
    Settings(label = "Settings", icon = FinPulseIcons.Settings),
}

@Composable
internal fun AppFrame(
    navigator: ComposeRootNavigator,
    backStack: List<NavKey>,
    section: @Composable (Int) -> Unit,
    detail: @Composable (NavKey) -> Unit,
    onboarding: @Composable () -> Unit,
) {
    val current = backStack.last()
    val onboardingVisible = current is OnboardingDestination
    val sectionRoot = current.isSectionRoot()
    val pages = rememberSectionPages(navigator = navigator, backStack = backStack, current = current)
    val pagerState = pages.state
    val selectPage = pages.select
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        val wide = !onboardingVisible && maxWidth >= WideLayout
        if (wide) {
            WideFrame(
                navigator = navigator,
                current = current,
                sectionRoot = sectionRoot,
                currentPage = pagerState.currentPage,
                onSelect = selectPage,
                pagerState = pagerState,
                section = section,
                detail = detail,
            )
        } else {
            FrameBody(
                navigator = navigator,
                current = current,
                onboarding = onboardingVisible,
                sectionRoot = sectionRoot,
                onMenu = null,
                pagerState = pagerState,
                onSelect = selectPage,
                section = section,
                detail = detail,
                onboardingContent = onboarding,
            )
        }
    }
}

private class SectionPages(
    val state: PagerState,
    val select: (Int) -> Unit,
)

@Composable
private fun rememberSectionPages(
    navigator: ComposeRootNavigator,
    backStack: List<NavKey>,
    current: NavKey,
): SectionPages {
    val pagerState = rememberPagerState(pageCount = { AppSection.entries.size })
    val scope = rememberCoroutineScope()
    var stackDrivesPager by remember { mutableStateOf(false) }
    LaunchedEffect(current) {
        val index = current.sectionIndex()
        if (index < 0 || index == pagerState.settledPage) {
            return@LaunchedEffect
        }
        stackDrivesPager = true
        pagerState.animateScrollToPage(index)
        stackDrivesPager = false
    }
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            if (stackDrivesPager) {
                return@collect
            }
            val top = backStack.last()
            val destination = AppSection.entries[page].destination()
            if (top.isSectionRoot() && top != destination) {
                navigator.replace(destination)
            }
        }
    }
    val select: (Int) -> Unit = { index ->
        scope.launch { pagerState.animateScrollToPage(index) }
    }
    return SectionPages(state = pagerState, select = select)
}

@Composable
private fun WideFrame(
    navigator: ComposeRootNavigator,
    current: NavKey,
    sectionRoot: Boolean,
    currentPage: Int,
    onSelect: (Int) -> Unit,
    pagerState: PagerState,
    section: @Composable (Int) -> Unit,
    detail: @Composable (NavKey) -> Unit,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = sectionRoot,
        drawerContent = {
            ModalDrawerSheet(windowInsets = NoInsets) {
                AppSection.entries.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(text = item.label) },
                        selected = currentPage == item.ordinal,
                        icon = { Icon(imageVector = item.icon, contentDescription = null) },
                        onClick = {
                            scope.launch { drawerState.close() }
                            onSelect(item.ordinal)
                        },
                    )
                }
            }
        },
    ) {
        FrameBody(
            navigator = navigator,
            current = current,
            onboarding = false,
            sectionRoot = sectionRoot,
            onMenu = if (sectionRoot) {
                { scope.launch { drawerState.open() } }
            } else {
                null
            },
            pagerState = pagerState,
            onSelect = onSelect,
            section = section,
            detail = detail,
            onboardingContent = {},
        )
    }
}

@Composable
private fun FrameBody(
    navigator: ComposeRootNavigator,
    current: NavKey,
    onboarding: Boolean,
    sectionRoot: Boolean,
    onMenu: (() -> Unit)?,
    pagerState: PagerState,
    onSelect: (Int) -> Unit,
    section: @Composable (Int) -> Unit,
    detail: @Composable (NavKey) -> Unit,
    onboardingContent: @Composable () -> Unit,
) {
    val showDetail = !onboarding && !sectionRoot
    SectionBackHandler(enabled = showDetail, onBack = navigator::back)
    Column(modifier = Modifier.fillMaxSize()) {
        if (onMenu != null) {
            MenuButton(onMenu = onMenu)
        } else if (showDetail) {
            BackArrow(onBack = navigator::back)
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clipToBounds(),
        ) {
            if (onboarding) {
                onboardingContent()
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = sectionRoot,
                    beyondViewportPageCount = 1,
                ) { page ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        section(page)
                    }
                }
                DetailOverlay(visible = showDetail, current = current, detail = detail)
            }
        }
        if (!onboarding && sectionRoot && onMenu == null) {
            BottomNavBar(
                items = AppSection.entries.map { item ->
                    BottomNavItem(
                        label = item.label,
                        icon = item.icon,
                        selected = pagerState.currentPage == item.ordinal,
                    )
                },
                onSelect = onSelect,
                windowInsets = NoInsets,
            )
        }
    }
}

@Composable
private fun DetailOverlay(
    visible: Boolean,
    current: NavKey,
    detail: @Composable (NavKey) -> Unit,
) {
    var shown by remember { mutableStateOf(current) }
    if (visible) {
        shown = current
    }
    AnimatedVisibility(
        visible = visible,
        modifier = Modifier.fillMaxSize(),
        enter = slideInHorizontally(tween(SECTION_SLIDE_MILLIS)) { width -> width },
        exit = slideOutHorizontally(tween(SECTION_SLIDE_MILLIS)) { width -> width },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            detail(shown)
        }
    }
}

private fun NavKey.isSectionRoot(): Boolean = when (this) {
    InsightsDestination.Dashboard,
    ScannerDestination.Capture,
    AssistantDestination.Ask,
    SettingsDestination.Home,
    -> true
    else -> false
}

private fun NavKey.sectionIndex(): Int = when (this) {
    InsightsDestination.Dashboard -> AppSection.Home.ordinal
    ScannerDestination.Capture -> AppSection.Scan.ordinal
    AssistantDestination.Ask -> AppSection.Ask.ordinal
    SettingsDestination.Home -> AppSection.Settings.ordinal
    else -> -1
}

private fun AppSection.destination(): Destination = when (this) {
    AppSection.Home -> InsightsDestination.Dashboard
    AppSection.Scan -> ScannerDestination.Capture
    AppSection.Ask -> AssistantDestination.Ask
    AppSection.Settings -> SettingsDestination.Home
}

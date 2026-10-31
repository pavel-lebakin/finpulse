package com.lebakin.finpulse.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lebakin.finpulse.core.uikit.FinPulsePalettes
import com.lebakin.finpulse.core.uikit.FinPulseTheme
import com.lebakin.finpulse.core.uikit.LocalFinPulseSpacing
import com.lebakin.finpulse.core.uikit.MenuButton
import kotlinx.coroutines.launch

private enum class CatalogPage(val label: String) {
    Tokens("Tokens"),
    Buttons("Buttons"),
    Fields("Fields"),
    Selection("Selection"),
    Navigation("Navigation"),
    Surfaces("Surfaces"),
    Feedback("Feedback"),
    Bars("Bars and menus"),
}

private val radiusRange = 4f..28f

private val spacingRange = 0.5f..2f

private val fontRange = 0.8f..1.6f

@Composable
fun CatalogApp() {
    var pageIndex by rememberSaveable { mutableIntStateOf(0) }
    val page = CatalogPage.entries[pageIndex.coerceIn(CatalogPage.entries.indices)]
    var paletteIndex by rememberSaveable { mutableIntStateOf(0) }
    val palette = FinPulsePalettes.all[paletteIndex.coerceIn(FinPulsePalettes.all.indices)]
    var dark by rememberSaveable { mutableStateOf(false) }
    var radius by rememberSaveable { mutableFloatStateOf(12f) }
    var spacing by rememberSaveable { mutableFloatStateOf(1f) }
    var fontScale by rememberSaveable { mutableFloatStateOf(1f) }
    FinPulseTheme(
        seed = palette.seed,
        dark = dark,
        baseRadius = radius.dp,
        spacingMultiplier = spacing,
        fontScale = fontScale,
        palette = true,
    ) {
        CatalogSystemBars(dark = dark)
        CatalogChrome(
            page = page,
            pageIndex = pageIndex,
            onPage = { pageIndex = it },
            content = {
                CatalogPageContent(
                    page = page,
                    paletteIndex = paletteIndex,
                    onPalette = { paletteIndex = it },
                    dark = dark,
                    onDark = { dark = it },
                    radius = radius,
                    onRadius = { radius = it },
                    spacing = spacing,
                    onSpacing = { spacing = it },
                    fontScale = fontScale,
                    onFontScale = { fontScale = it },
                )
            },
        )
    }
}

@Composable
private fun CatalogChrome(
    page: CatalogPage,
    pageIndex: Int,
    onPage: (Int) -> Unit,
    content: @Composable () -> Unit,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                CatalogDrawer(pageIndex = pageIndex, onPage = { index ->
                    onPage(index)
                    scope.launch { drawerState.close() }
                })
            },
        ) {
            CatalogBody(
                title = page.label,
                onMenu = { scope.launch { drawerState.open() } },
                content = content,
            )
        }
    }
}

@Composable
private fun CatalogDrawer(
    pageIndex: Int,
    onPage: (Int) -> Unit,
) {
    ModalDrawerSheet(windowInsets = WindowInsets.safeDrawing) {
        CatalogPage.entries.forEachIndexed { index, item ->
            NavigationDrawerItem(
                label = { Text(text = item.label) },
                selected = pageIndex == index,
                onClick = { onPage(index) },
            )
        }
    }
}

@Composable
private fun CatalogBody(
    title: String,
    onMenu: () -> Unit,
    content: @Composable () -> Unit,
) {
    val space = LocalFinPulseSpacing.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        MenuButton(onMenu = onMenu)
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = space.m),
            style = MaterialTheme.typography.headlineMedium,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(space.m),
            verticalArrangement = Arrangement.spacedBy(space.m),
        ) {
            content()
        }
    }
}

@Composable
private fun CatalogPageContent(
    page: CatalogPage,
    paletteIndex: Int,
    onPalette: (Int) -> Unit,
    dark: Boolean,
    onDark: (Boolean) -> Unit,
    radius: Float,
    onRadius: (Float) -> Unit,
    spacing: Float,
    onSpacing: (Float) -> Unit,
    fontScale: Float,
    onFontScale: (Float) -> Unit,
) {
    when (page) {
        CatalogPage.Tokens -> CatalogTokens(
            paletteIndex = paletteIndex,
            onPalette = onPalette,
            dark = dark,
            onDark = onDark,
            radius = radius,
            onRadius = onRadius,
            spacing = spacing,
            onSpacing = onSpacing,
            fontScale = fontScale,
            onFontScale = onFontScale,
            radiusRange = radiusRange,
            spacingRange = spacingRange,
            fontRange = fontRange,
        )
        CatalogPage.Buttons -> CatalogButtons()
        CatalogPage.Fields -> CatalogFields()
        CatalogPage.Selection -> CatalogSelection()
        CatalogPage.Navigation -> CatalogNavigation()
        CatalogPage.Surfaces -> CatalogSurfaces()
        CatalogPage.Feedback -> CatalogFeedback()
        CatalogPage.Bars -> CatalogBars()
    }
}

@Composable
internal fun CatalogStack(content: @Composable ColumnScope.() -> Unit) {
    val space = LocalFinPulseSpacing.current
    Column(
        verticalArrangement = Arrangement.spacedBy(space.m),
        content = content,
    )
}

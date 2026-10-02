package com.lebakin.finpulse.core.uikit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import io.github.takahirom.roborazzi.captureRoboImage
import java.io.File
import kotlin.test.Test

private val SHOT_WIDTH = 360.dp

private const val SHOT_TAG = "shot"

private const val AMOUNT_SAMPLE = "1,240.50 ₽"

private const val TRANSACTION_TITLE = "Groceries"

private const val TRANSACTION_SUBTITLE = "Oct 28"

private const val TRANSACTION_AMOUNT = "−860.00 ₽"

private const val BANNER_MESSAGE = "This is sample data. It is removed when you add your first transaction."

private const val BANNER_ACTION = "Clear now"

private const val EMPTY_TITLE = "Nothing here yet"

private const val EMPTY_BODY = "Add your first transaction, or look at a sample."

private val chartAmounts = listOf(12f, 40f, 18f, 32f)

class ComponentScreenshotTest {
    @Test
    fun moneyAmountLight() = capture(name = "moneyAmountLight") {
        MoneyAmount(amount = AMOUNT_SAMPLE)
    }

    @Test
    fun moneyAmountDark() = capture(name = "moneyAmountDark", dark = true) {
        MoneyAmount(amount = AMOUNT_SAMPLE)
    }

    @Test
    fun transactionRowLight() = capture(name = "transactionRowLight") {
        TransactionRow(
            title = TRANSACTION_TITLE,
            subtitle = TRANSACTION_SUBTITLE,
            amount = TRANSACTION_AMOUNT,
        )
    }

    @Test
    fun transactionRowDark() = capture(name = "transactionRowDark", dark = true) {
        TransactionRow(
            title = TRANSACTION_TITLE,
            subtitle = TRANSACTION_SUBTITLE,
            amount = TRANSACTION_AMOUNT,
        )
    }

    @Test
    fun noticeBannerLight() = capture(name = "noticeBannerLight") {
        NoticeBanner(
            message = BANNER_MESSAGE,
            actionLabel = BANNER_ACTION,
            onAction = {},
        )
    }

    @Test
    fun noticeBannerDark() = capture(name = "noticeBannerDark", dark = true) {
        NoticeBanner(
            message = BANNER_MESSAGE,
            actionLabel = BANNER_ACTION,
            onAction = {},
        )
    }

    @Test
    fun emptyStateLight() = capture(name = "emptyStateLight") {
        EmptyState(title = EMPTY_TITLE, body = EMPTY_BODY)
    }

    @Test
    fun emptyStateDark() = capture(name = "emptyStateDark", dark = true) {
        EmptyState(title = EMPTY_TITLE, body = EMPTY_BODY)
    }

    @Test
    fun amountBarsLight() = capture(name = "amountBarsLight") {
        AmountBars(amounts = chartAmounts)
    }

    @Test
    fun amountBarsDark() = capture(name = "amountBarsDark", dark = true) {
        AmountBars(amounts = chartAmounts)
    }

    @Test
    fun amountBarsEmptyLight() = capture(name = "amountBarsEmptyLight") {
        AmountBars(amounts = emptyList())
    }

    @Test
    fun amountBarsEmptyDark() = capture(name = "amountBarsEmptyDark", dark = true) {
        AmountBars(amounts = emptyList())
    }

    @OptIn(ExperimentalTestApi::class)
    private fun capture(
        name: String,
        dark: Boolean = false,
        content: @Composable () -> Unit,
    ) {
        val fontFamily = screenshotFontFamily()
        runComposeUiTest {
            setContent {
                FinPulseTheme(dark = dark) {
                    val current = MaterialTheme.typography
                    MaterialTheme(
                        colorScheme = MaterialTheme.colorScheme,
                        typography = current.copy(
                            headlineSmall = current.headlineSmall.copy(fontFamily = fontFamily),
                            bodyLarge = current.bodyLarge.copy(fontFamily = fontFamily),
                            bodyMedium = current.bodyMedium.copy(fontFamily = fontFamily),
                            labelLarge = current.labelLarge.copy(fontFamily = fontFamily),
                        ),
                    ) {
                        Box(
                            modifier = Modifier
                                .testTag(SHOT_TAG)
                                .width(SHOT_WIDTH)
                                .background(MaterialTheme.colorScheme.background)
                                .padding(LocalFinPulseSpacing.current.m),
                        ) {
                            content()
                        }
                    }
                }
            }
            onNodeWithTag(SHOT_TAG).captureRoboImage("src/jvmTest/snapshots/components/$name.png")
        }
    }
}

private fun screenshotFontFamily(): FontFamily {
    val resource = checkNotNull(
        ComponentScreenshotTest::class.java.classLoader.getResource("fonts/Roboto-Regular.ttf"),
    ) {
        "fonts/Roboto-Regular.ttf is missing from jvmTest resources"
    }
    val font = androidx.compose.ui.text.platform.Font(
        file = File(resource.toURI()),
        weight = FontWeight.Normal,
        style = FontStyle.Normal,
    )
    return FontFamily(font)
}

package com.lebakin.finpulse.feature.insights.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.lebakin.finpulse.core.common.Loadable
import com.lebakin.finpulse.core.uikit.AmountBars
import com.lebakin.finpulse.core.uikit.EmptyState
import com.lebakin.finpulse.core.uikit.LocalFinPulseSpacing
import com.lebakin.finpulse.core.uikit.MoneyAmount
import com.lebakin.finpulse.core.uikit.NoticeBanner
import com.lebakin.finpulse.core.uikit.TransactionRow
import com.lebakin.finpulse.feature.insights.presentation.mapper.SpendingUiModel
import com.lebakin.finpulse.feature.insights.presentation.model.mvi.DashboardAction
import com.lebakin.finpulse.feature.insights.presentation.model.mvi.DashboardContent
import com.lebakin.finpulse.feature.insights.presentation.model.mvi.DashboardState
import com.lebakin.finpulse.feature.insights.presentation.model.mvi.toUserMessage

@Composable
internal fun DashboardScreen(
    state: DashboardState,
    onAction: (DashboardAction) -> Unit,
) {
    val spacing = LocalFinPulseSpacing.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(spacing.l),
        verticalArrangement = Arrangement.spacedBy(spacing.m),
    ) {
        Text(text = "Dashboard", style = MaterialTheme.typography.headlineSmall)
        Button(onClick = { onAction(DashboardAction.ScanReceipt) }) {
            Text(text = "Scan a receipt")
        }
        when (val content = state.content) {
            Loadable.Loading -> Text(text = "Loading")
            is Loadable.Failed -> FailedContent(message = content.error.toUserMessage().text, onAction = onAction)
            is Loadable.Ready -> DashboardBody(content = content.value, onAction = onAction)
        }
        TextButton(onClick = { onAction(DashboardAction.ShowOnboardingAgain) }) {
            Text(text = "Show onboarding again")
        }
        TextButton(onClick = { onAction(DashboardAction.OpenConsent) }) {
            Text(text = "Allow sending to Gemini")
        }
        TextButton(onClick = { onAction(DashboardAction.OpenKey) }) {
            Text(text = "Gemini key")
        }
        TextButton(onClick = { onAction(DashboardAction.OpenLicenses) }) {
            Text(text = "Open source licenses")
        }
        MessageText(message = state.message, onDismiss = { onAction(DashboardAction.DismissMessage) })
        if (state.sheet != null) {
            TextButton(onClick = { onAction(DashboardAction.DismissSheet) }) {
                Text(text = "Close")
            }
        }
    }
}

@Composable
private fun FailedContent(
    message: String,
    onAction: (DashboardAction) -> Unit,
) {
    Text(text = message)
    Button(onClick = { onAction(DashboardAction.Retry) }) {
        Text(text = "Try again")
    }
}

@Composable
private fun DashboardBody(
    content: DashboardContent,
    onAction: (DashboardAction) -> Unit,
) {
    when (content) {
        DashboardContent.Empty -> EmptyState(
            title = "Nothing here yet",
            body = "Scan a receipt or write a phrase when you want to add a transaction.",
        )
        is DashboardContent.Ready -> ReadyContent(content = content, onAction = onAction)
    }
}

@Composable
private fun ReadyContent(
    content: DashboardContent.Ready,
    onAction: (DashboardAction) -> Unit,
) {
    if (content.showcase) {
        NoticeBanner(
            message = "This is sample data. It is removed when you add your first transaction.",
            actionLabel = "Clear now",
            onAction = { onAction(DashboardAction.ClearSample) },
        )
    }
    Text(text = "Spent this month", style = MaterialTheme.typography.bodyLarge)
    MoneyAmount(amount = content.spending.monthExpense)
    MonthChart(spending = content.spending)
    CategoryList(spending = content.spending)
    RecentList(spending = content.spending)
}

@Composable
private fun MonthChart(spending: SpendingUiModel) {
    Text(text = "Last three months", style = MaterialTheme.typography.titleMedium)
    AmountBars(amounts = spending.months.map { it.amount })
    Row(modifier = Modifier.fillMaxWidth()) {
        spending.months.forEach { month ->
            Text(
                text = month.label,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun CategoryList(spending: SpendingUiModel) {
    Text(text = "This month", style = MaterialTheme.typography.titleMedium)
    if (spending.categories.isEmpty()) {
        Text(text = "No expenses this month.")
    } else {
        spending.categories.forEach { category ->
            TransactionRow(title = category.name, subtitle = "Expense", amount = category.amount)
        }
    }
}

@Composable
private fun RecentList(spending: SpendingUiModel) {
    if (spending.recent.isNotEmpty()) {
        Text(text = "Recent", style = MaterialTheme.typography.titleMedium)
        spending.recent.forEach { movement ->
            TransactionRow(
                title = movement.title,
                subtitle = movement.subtitle,
                amount = movement.amount,
            )
        }
    }
}

package com.lebakin.finpulse.core.uikit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

@Composable
fun MoneyAmount(
    amount: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = amount,
        modifier = modifier,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
fun TransactionRow(
    title: String,
    subtitle: String,
    amount: String,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalFinPulseSpacing.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.m, vertical = spacing.s),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        MoneyAmount(amount = amount)
    }
}

@Composable
fun NoticeBanner(
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalFinPulseSpacing.current
    val radii = LocalFinPulseRadii.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(radii.medium))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(spacing.m),
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge)
        TextButton(onClick = onAction) {
            Text(text = actionLabel)
        }
    }
}

@Composable
fun EmptyState(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalFinPulseSpacing.current
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(spacing.s))
        Text(text = body, style = MaterialTheme.typography.bodyLarge)
    }
}

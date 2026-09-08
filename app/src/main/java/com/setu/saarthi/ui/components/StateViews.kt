package com.setu.saarthi.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.setu.saarthi.ui.theme.Dimens

@Composable
fun LoadingView(modifier: Modifier = Modifier, message: String? = null) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Dimens.CardGap)) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            if (message != null) {
                Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun ErrorView(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    icon: ImageVector = Icons.Filled.Warning
) {
    Box(modifier = modifier.fillMaxSize().padding(Dimens.Gutter), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Dimens.CardGap)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (onRetry != null) {
                OutlinedButton(onClick = onRetry) { Text("Retry") }
            }
        }
    }
}

@Composable
fun EmptyView(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector = Icons.Filled.Search,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Box(modifier = modifier.fillMaxSize().padding(Dimens.Gutter), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Dimens.CardGap)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(48.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
            if (actionLabel != null && onAction != null) {
                OutlinedButton(onClick = onAction) { Text(actionLabel) }
            }
        }
    }
}

/**
 * Single funnel every screen body goes through, so no screen silently skips
 * a loading/error/empty state.
 */
@Composable
fun StateSlot(
    isLoading: Boolean,
    errorMessage: String?,
    isEmpty: Boolean,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {},
    emptyTitle: String = "Nothing here yet",
    emptySubtitle: String? = null,
    content: @Composable () -> Unit
) {
    when {
        isLoading -> LoadingView(modifier)
        errorMessage != null -> ErrorView(errorMessage, modifier, onRetry)
        isEmpty -> EmptyView(emptyTitle, modifier, emptySubtitle)
        else -> content()
    }
}

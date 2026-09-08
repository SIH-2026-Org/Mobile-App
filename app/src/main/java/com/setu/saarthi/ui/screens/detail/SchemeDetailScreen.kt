package com.setu.saarthi.ui.screens.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.setu.saarthi.domain.model.RuleStatus
import com.setu.saarthi.ui.components.MatchBadge
import com.setu.saarthi.ui.components.SaarthiTopBar
import com.setu.saarthi.ui.components.StateSlot
import com.setu.saarthi.ui.theme.Dimens
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchemeDetailScreen(
    onBack: () -> Unit,
    viewModel: SchemeDetailViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val uriHandler = LocalUriHandler.current

    Scaffold(
        topBar = {
            SaarthiTopBar(
                title = uiState.scheme?.shortName ?: "Scheme",
                onBack = onBack
            )
        }
    ) { innerPadding ->
        StateSlot(
            isLoading = uiState.isLoading,
            errorMessage = uiState.errorMessage,
            isEmpty = uiState.scheme == null,
            onRetry = viewModel::onRetry,
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            val scheme = uiState.scheme!!
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(Dimens.Gutter),
                verticalArrangement = Arrangement.spacedBy(Dimens.CardGap)
            ) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(scheme.name, style = MaterialTheme.typography.headlineSmall)
                            Text(scheme.ministry, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = viewModel::onToggleSave) {
                            Icon(
                                if (uiState.isSaved) Icons.Filled.Star else Icons.Filled.StarBorder,
                                contentDescription = "Bookmark",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                uiState.matched?.let { matched ->
                    item { MatchBadge(status = matched.eligibility.status, score = matched.score.totalScore) }
                }

                item { Text(scheme.description, style = MaterialTheme.typography.bodyMedium) }

                uiState.matched?.let { matched ->
                    if (matched.eligibility.passed.isNotEmpty()) {
                        item { Text("Why you match", style = MaterialTheme.typography.titleMedium) }
                        items(matched.eligibility.passed) { rule -> RuleRow(rule.message, RuleStatus.PASS) }
                    }
                    if (matched.eligibility.failed.isNotEmpty()) {
                        item { Text("What doesn't match", style = MaterialTheme.typography.titleMedium) }
                        items(matched.eligibility.failed) { rule -> RuleRow(rule.message, RuleStatus.FAIL) }
                    }
                    if (matched.eligibility.missing.isNotEmpty()) {
                        item { Text("We need more info", style = MaterialTheme.typography.titleMedium) }
                        items(matched.eligibility.missing) { rule -> RuleRow(rule.message, RuleStatus.MISSING) }
                    }
                }

                item { Text("Financing", style = MaterialTheme.typography.titleMedium) }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        val financing = scheme.financing
                        Text("• Type: ${financing.type.name.lowercase().replaceFirstChar { it.uppercase() }}", style = MaterialTheme.typography.bodyMedium)
                        financing.maxAmount?.let { max ->
                            Text("• Up to ₹${"%,d".format(max)}", style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(
                            "• Interest rate: ${financing.interestRate.effectiveRate}% p.a.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (financing.subsidyAmount != null) {
                            Text("• Subsidy: ₹${"%,d".format(financing.subsidyAmount)}", style = MaterialTheme.typography.bodyMedium)
                        } else if (financing.subsidyPct != null) {
                            Text("• Subsidy: ${financing.subsidyPct}% of project cost", style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(
                            "• Collateral: ${if (financing.collateralRequired) "Required" else "Not required"}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        financing.subsidyNotes?.let { note ->
                            Text("• $note", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                item { Text("Documents required", style = MaterialTheme.typography.titleMedium) }
                items(scheme.documents) { doc ->
                    Text(
                        "• ${doc.name}${if (!doc.required) " (optional)" else ""}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                item {
                    Text(
                        "Figures are indicative - confirm details on the official portal before applying.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    Button(
                        onClick = { uriHandler.openUri(scheme.metadata.officialUrl) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Apply on official portal")
                    }
                }
            }
        }
    }
}

@Composable
private fun RuleRow(message: String, status: RuleStatus) {
    val color = when (status) {
        RuleStatus.PASS -> MaterialTheme.colorScheme.secondary
        RuleStatus.FAIL -> MaterialTheme.colorScheme.error
        RuleStatus.MISSING -> MaterialTheme.colorScheme.tertiary
    }
    val prefix = when (status) {
        RuleStatus.PASS -> "✓"
        RuleStatus.FAIL -> "✗"
        RuleStatus.MISSING -> "?"
    }
    Text("$prefix $message", style = MaterialTheme.typography.bodySmall, color = color)
}

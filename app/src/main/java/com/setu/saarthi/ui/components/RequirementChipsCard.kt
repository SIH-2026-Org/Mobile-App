package com.setu.saarthi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.setu.saarthi.domain.model.BeneficiaryProfile
import com.setu.saarthi.domain.model.SocialCategory
import com.setu.saarthi.ui.theme.CardShape
import com.setu.saarthi.ui.theme.Dimens

/**
 * The editable "we understood this" card, shown after the LLM layer parses
 * a query and before the rule engine runs - lets the user correct anything
 * before schemes are matched.
 */
@Composable
fun RequirementChipsCard(
    profile: BeneficiaryProfile,
    modifier: Modifier = Modifier,
    onFieldChange: (BeneficiaryProfile) -> Unit,
    onConfirm: () -> Unit,
    confirmLabel: String = "Find schemes"
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer, CardShape)
            .padding(Dimens.CardPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.FieldGap)
    ) {
        Text("We understood this", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = profile.activity.orEmpty(),
            onValueChange = { onFieldChange(profile.copy(activity = it.ifBlank { null })) },
            label = { Text("What is this for?") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = (profile.projectCost ?: profile.loanRequired)?.toString().orEmpty(),
            onValueChange = { text ->
                val amount = text.filter { it.isDigit() }.toLongOrNull()
                onFieldChange(profile.copy(projectCost = amount, loanRequired = amount))
            },
            label = { Text("Amount needed (₹)") },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.CardGap)) {
            OutlinedTextField(
                value = profile.state.orEmpty(),
                onValueChange = { onFieldChange(profile.copy(state = it.ifBlank { null })) },
                label = { Text("State") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = profile.age?.toString().orEmpty(),
                onValueChange = { text -> onFieldChange(profile.copy(age = text.filter { it.isDigit() }.toIntOrNull())) },
                label = { Text("Age") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Text("Social category", style = MaterialTheme.typography.labelLarge)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            SocialCategory.entries.forEach { category ->
                FilterChip(
                    selected = profile.socialCategory == category,
                    onClick = { onFieldChange(profile.copy(socialCategory = category)) },
                    label = { Text(category.name) }
                )
            }
        }

        Button(onClick = onConfirm, modifier = Modifier.fillMaxWidth()) {
            Text(confirmLabel)
        }
    }
}

package com.setu.saarthi.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.setu.saarthi.domain.model.SocialCategory
import com.setu.saarthi.ui.components.LoadingView
import com.setu.saarthi.ui.components.SaarthiTopBar
import com.setu.saarthi.ui.theme.Dimens
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: ProfileViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(topBar = { SaarthiTopBar("Your profile") }) { innerPadding ->
        if (uiState.isLoading) {
            LoadingView(Modifier.fillMaxSize())
            return@Scaffold
        }

        val profile = uiState.profile

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.Gutter),
            verticalArrangement = Arrangement.spacedBy(Dimens.FieldGap)
        ) {
            Text(
                "This helps us pre-fill your requirement and find better matches.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = profile.name,
                onValueChange = { viewModel.onFieldChange(profile.copy(name = it)) },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.CardGap)) {
                OutlinedTextField(
                    value = profile.age?.toString().orEmpty(),
                    onValueChange = { text -> viewModel.onFieldChange(profile.copy(age = text.filter { it.isDigit() }.toIntOrNull())) },
                    label = { Text("Age") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = profile.state.orEmpty(),
                    onValueChange = { viewModel.onFieldChange(profile.copy(state = it.ifBlank { null })) },
                    label = { Text("State") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = profile.activity.orEmpty(),
                onValueChange = { viewModel.onFieldChange(profile.copy(activity = it.ifBlank { null })) },
                label = { Text("Occupation / business activity") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = profile.annualIncome?.toString().orEmpty(),
                onValueChange = { text -> viewModel.onFieldChange(profile.copy(annualIncome = text.filter { it.isDigit() }.toLongOrNull())) },
                label = { Text("Annual family income (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text("Social category", style = MaterialTheme.typography.labelLarge)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SocialCategory.entries.forEach { category ->
                    FilterChip(
                        selected = profile.socialCategory == category,
                        onClick = { viewModel.onFieldChange(profile.copy(socialCategory = category)) },
                        label = { Text(category.name) }
                    )
                }
            }

            Button(
                onClick = viewModel::onSave,
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (uiState.isSaving) "Saving..." else "Save profile")
            }

            if (uiState.savedJustNow) {
                Text("Saved", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

package com.setu.saarthi.ui.screens.results

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.setu.saarthi.domain.model.MatchStatus
import com.setu.saarthi.ui.components.EmptyView
import com.setu.saarthi.ui.components.SaarthiTopBar
import com.setu.saarthi.ui.components.SchemeCard
import com.setu.saarthi.ui.theme.Dimens
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    onBack: () -> Unit,
    onSchemeClick: (String) -> Unit,
    viewModel: ResultsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val result = uiState.matchResult

    Scaffold(topBar = { SaarthiTopBar("Eligible schemes", onBack = onBack) }) { innerPadding ->
        when {
            result == null || result.status == MatchStatus.ERROR ->
                EmptyView(
                    "Couldn't load results",
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    subtitle = "Please go back and try your search again."
                )
            result.eligibleSchemes.isEmpty() && result.partialSchemes.isEmpty() ->
                EmptyView(
                    "No matching schemes found",
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    subtitle = "Try describing your requirement differently, or check back as new schemes are added."
                )
            else -> LazyColumn(
                contentPadding = PaddingValues(Dimens.Gutter),
                verticalArrangement = Arrangement.spacedBy(Dimens.CardGap),
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            ) {
                items(result.eligibleSchemes) { matched ->
                    SchemeCard(
                        scheme = matched.scheme,
                        matchStatus = matched.eligibility.status,
                        matchScore = matched.score.totalScore,
                        isSaved = matched.scheme.schemeId in uiState.savedIds,
                        onSaveClick = { viewModel.onToggleSave(matched.scheme.schemeId) },
                        onClick = { onSchemeClick(matched.scheme.schemeId) }
                    )
                }
                items(result.partialSchemes) { matched ->
                    SchemeCard(
                        scheme = matched.scheme,
                        matchStatus = matched.eligibility.status,
                        matchScore = matched.score.totalScore,
                        isSaved = matched.scheme.schemeId in uiState.savedIds,
                        onSaveClick = { viewModel.onToggleSave(matched.scheme.schemeId) },
                        onClick = { onSchemeClick(matched.scheme.schemeId) }
                    )
                }
            }
        }
    }
}

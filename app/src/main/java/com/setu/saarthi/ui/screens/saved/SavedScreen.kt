package com.setu.saarthi.ui.screens.saved

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
import com.setu.saarthi.ui.components.EmptyView
import com.setu.saarthi.ui.components.LoadingView
import com.setu.saarthi.ui.components.SaarthiTopBar
import com.setu.saarthi.ui.components.SchemeCard
import com.setu.saarthi.ui.theme.Dimens
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(
    onSchemeClick: (String) -> Unit,
    viewModel: SavedViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(topBar = { SaarthiTopBar("Saved schemes") }) { innerPadding ->
        when {
            uiState.isLoading -> LoadingView(Modifier.fillMaxSize().padding(innerPadding))
            uiState.schemes.isEmpty() -> EmptyView(
                "No saved schemes yet",
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                subtitle = "Bookmark schemes from your results to find them here later."
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(Dimens.Gutter),
                verticalArrangement = Arrangement.spacedBy(Dimens.CardGap),
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            ) {
                items(uiState.schemes) { scheme ->
                    SchemeCard(scheme = scheme, onClick = { onSchemeClick(scheme.schemeId) })
                }
            }
        }
    }
}

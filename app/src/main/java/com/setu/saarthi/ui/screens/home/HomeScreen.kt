package com.setu.saarthi.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.setu.saarthi.ui.components.EmptyView
import com.setu.saarthi.ui.components.ErrorView
import com.setu.saarthi.ui.components.LoadingView
import com.setu.saarthi.ui.components.NewsCard
import com.setu.saarthi.ui.components.RequirementChipsCard
import com.setu.saarthi.ui.components.SaarthiInputBar
import com.setu.saarthi.ui.components.SectionHeader
import com.setu.saarthi.ui.theme.Dimens
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToResults: () -> Unit,
    onNavigateToClarify: () -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.navigateTo) {
        when (uiState.navigateTo) {
            HomeNavTarget.RESULTS -> {
                onNavigateToResults()
                viewModel.onNavigated()
            }
            HomeNavTarget.CLARIFY -> {
                onNavigateToClarify()
                viewModel.onNavigated()
            }
            null -> Unit
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Saarthi-Setu") }) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(modifier = Modifier.fillMaxSize().imePadding()) {
                Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    when {
                        uiState.isNewsLoading -> LoadingView(Modifier.fillMaxWidth().padding(top = 32.dp))
                        uiState.newsError != null -> ErrorView(uiState.newsError!!, onRetry = viewModel::onRetryNews)
                        uiState.news.isEmpty() -> EmptyView("No news right now", subtitle = "Check back later for scheme updates")
                        else -> {
                            SectionHeader("Latest scheme news")
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = Dimens.Gutter),
                                horizontalArrangement = Arrangement.spacedBy(Dimens.CardGap)
                            ) {
                                items(uiState.news) { item -> NewsCard(item) }
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = Dimens.Gutter, vertical = 12.dp)
                ) {
                    SaarthiInputBar(
                        value = uiState.inputText,
                        onValueChange = viewModel::onInputChange,
                        onSubmit = viewModel::onSend,
                        isSubmitting = uiState.isParsing,
                        error = uiState.parseError
                    )
                }
            }
        }
    }

    val extracted = uiState.extracted
    if (extracted != null) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = viewModel::onDismissRequirementSheet,
            sheetState = sheetState
        ) {
            RequirementChipsCard(
                profile = extracted,
                onFieldChange = viewModel::onExtractedFieldChange,
                onConfirm = viewModel::onConfirmRequirement,
                confirmLabel = if (uiState.isMatching) "Finding schemes..." else "Find schemes",
                modifier = Modifier.fillMaxWidth().padding(Dimens.Gutter).navigationBarsPadding().imePadding()
            )
        }
    }
}

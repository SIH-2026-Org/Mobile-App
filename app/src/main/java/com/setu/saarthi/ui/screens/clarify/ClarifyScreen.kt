package com.setu.saarthi.ui.screens.clarify

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.setu.saarthi.ui.components.SaarthiTopBar
import com.setu.saarthi.ui.theme.CardShape
import com.setu.saarthi.ui.theme.Dimens
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClarifyScreen(
    onBack: () -> Unit,
    onDone: () -> Unit,
    viewModel: ClarifyViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isDone) {
        if (uiState.isDone) onDone()
    }

    Scaffold(topBar = { SaarthiTopBar("Just a couple more details", onBack = onBack) }) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).imePadding()) {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(Dimens.Gutter),
                verticalArrangement = Arrangement.spacedBy(Dimens.CardGap)
            ) {
                items(uiState.messages) { message ->
                    ChatBubble(fromUser = message.fromUser, text = message.text)
                }
                if (uiState.isThinking) {
                    item { ChatBubble(fromUser = false, text = "…") }
                }
                if (uiState.errorMessage != null) {
                    item {
                        Text(
                            uiState.errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = Dimens.Gutter, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = uiState.answerText,
                    onValueChange = viewModel::onAnswerChange,
                    singleLine = true,
                    enabled = !uiState.isThinking,
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surfaceContainerLow, CardShape)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                )
                IconButton(onClick = viewModel::onSubmitAnswer, enabled = !uiState.isThinking) {
                    if (uiState.isThinking) {
                        CircularProgressIndicator(modifier = Modifier.padding(4.dp).widthIn(max = 20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.ArrowForward, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(fromUser: Boolean, text: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (fromUser) Arrangement.End else Arrangement.Start) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    if (fromUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                    CardShape
                )
                .padding(12.dp)
        ) {
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (fromUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

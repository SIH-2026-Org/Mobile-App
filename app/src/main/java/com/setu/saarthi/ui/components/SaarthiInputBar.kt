package com.setu.saarthi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.setu.saarthi.ui.theme.Dimens
import com.setu.saarthi.ui.theme.GradientButtonEnd
import com.setu.saarthi.ui.theme.GradientButtonStart
import com.setu.saarthi.ui.theme.PillShape

/**
 * The bottom fixed input box - the entry point of the core flow
 * (User Text -> LLM Layer -> ...). Ported from PhiNance's `QuickEntryBar`.
 */
@Composable
fun SaarthiInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    isSubmitting: Boolean = false,
    error: String? = null,
    placeholder: String = "Mujhe dairy business ke liye ₹1.2 lakh loan chahiye..."
) {
    val hasText = value.isNotBlank()
    val borderColor = when {
        error != null -> MaterialTheme.colorScheme.error
        hasText -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.InputBarHeight)
                .background(MaterialTheme.colorScheme.surfaceContainerLow, PillShape)
                .border(1.dp, borderColor, PillShape)
                .padding(start = 24.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                enabled = !isSubmitting,
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(onSend = { if (hasText) onSubmit() }),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            maxLines = 1
                        )
                    }
                    innerTextField()
                }
            )
            androidx.compose.foundation.layout.Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = onSubmit,
                enabled = hasText && !isSubmitting,
                modifier = Modifier
                    .background(
                        brush = if (hasText) Brush.horizontalGradient(listOf(GradientButtonStart, GradientButtonEnd))
                        else Brush.horizontalGradient(listOf(androidx.compose.ui.graphics.Color.Transparent, androidx.compose.ui.graphics.Color.Transparent)),
                        shape = PillShape
                    )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.height(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Icon(
                        Icons.Filled.ArrowForward,
                        contentDescription = "Send",
                        tint = if (hasText) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }
            }
        }
        if (error != null) {
            Text(
                error,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 20.dp, top = 4.dp)
            )
        }
    }
}

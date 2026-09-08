package com.setu.saarthi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.setu.saarthi.domain.model.EligibilityStatus
import com.setu.saarthi.domain.model.Scheme
import com.setu.saarthi.ui.theme.CardShape
import com.setu.saarthi.ui.theme.Dimens

@Composable
fun SchemeCard(
    scheme: Scheme,
    modifier: Modifier = Modifier,
    matchStatus: EligibilityStatus? = null,
    matchScore: Int? = null,
    isSaved: Boolean = false,
    onSaveClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, CardShape)
            .clickable { onClick() }
            .padding(Dimens.CardPadding),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(scheme.shortName, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                scheme.ministry,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (matchStatus != null) {
                Spacer(Modifier.width(0.dp))
                MatchBadge(status = matchStatus, score = matchScore)
            }
        }
        if (onSaveClick != null) {
            IconButton(onClick = onSaveClick) {
                Icon(
                    imageVector = if (isSaved) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = if (isSaved) "Remove bookmark" else "Bookmark",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

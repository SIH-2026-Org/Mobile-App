package com.setu.saarthi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.setu.saarthi.domain.model.NewsItem
import com.setu.saarthi.ui.theme.CardShape
import com.setu.saarthi.ui.theme.Dimens

@Composable
fun NewsCard(item: NewsItem, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Column(
        modifier = modifier
            .width(260.dp)
            .background(MaterialTheme.colorScheme.surface, CardShape)
            .clickable { onClick() }
            .padding(Dimens.CardPadding),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(item.ministry, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(item.title, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text(
            item.summary,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        Text(item.publishedOn, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
    }
}

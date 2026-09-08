package com.setu.saarthi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.setu.saarthi.domain.model.EligibilityStatus
import com.setu.saarthi.ui.theme.PillShape
import com.setu.saarthi.ui.theme.TierEligible
import com.setu.saarthi.ui.theme.TierEligibleContainer
import com.setu.saarthi.ui.theme.TierNotEligible
import com.setu.saarthi.ui.theme.TierNotEligibleContainer
import com.setu.saarthi.ui.theme.TierPartial
import com.setu.saarthi.ui.theme.TierPartialContainer

@Composable
fun MatchBadge(status: EligibilityStatus, modifier: Modifier = Modifier, score: Int? = null) {
    val (label, fg, bg) = when (status) {
        EligibilityStatus.ELIGIBLE -> Triple("Eligible", TierEligible, TierEligibleContainer)
        EligibilityStatus.NEEDS_MORE_INFO -> Triple("Needs info", TierPartial, TierPartialContainer)
        EligibilityStatus.NOT_ELIGIBLE -> Triple("Not eligible", TierNotEligible, TierNotEligibleContainer)
    }
    val text = if (score != null) "$label · $score" else label
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = fg,
        modifier = modifier
            .background(bg, PillShape)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

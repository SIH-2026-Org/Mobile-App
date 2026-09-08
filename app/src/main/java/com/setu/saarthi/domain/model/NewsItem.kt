package com.setu.saarthi.domain.model

/** A scheme announcement/news card shown at the top of the Home feed. */
data class NewsItem(
    val id: String,
    val title: String,
    val summary: String,
    val ministry: String,
    val publishedOn: String,
    val relatedSchemeId: String? = null
)

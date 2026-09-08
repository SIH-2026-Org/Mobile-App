package com.setu.saarthi.data.mock

import com.setu.saarthi.domain.model.NewsItem

/**
 * Placeholder announcement cards for the Home feed. Dates and figures are
 * illustrative, not sourced from a live news API.
 */
object MockNewsData {

    val news: List<NewsItem> = listOf(
        NewsItem(
            id = "news_pmegp_budget",
            title = "PMEGP subsidy outlay increased for FY 2026-27",
            summary = "The Ministry of MSME has expanded margin money subsidy allocation for new micro-enterprises under PMEGP this financial year.",
            ministry = "Ministry of MSME",
            publishedOn = "2 Sep 2026",
            relatedSchemeId = "pmegp"
        ),
        NewsItem(
            id = "news_mudra_milestone",
            title = "MUDRA loans cross new disbursement milestone",
            summary = "Pradhan Mantri MUDRA Yojana continues to support small and micro enterprises with collateral-free credit across all three loan tiers.",
            ministry = "Ministry of Finance",
            publishedOn = "28 Aug 2026",
            relatedSchemeId = "mudra_kishore"
        ),
        NewsItem(
            id = "news_kusum_expansion",
            title = "PM-KUSUM solar pump enrolment window extended",
            summary = "Farmers can now apply for subsidised solar pump installations under PM-KUSUM through their state renewable energy department portal.",
            ministry = "Ministry of New and Renewable Energy",
            publishedOn = "20 Aug 2026",
            relatedSchemeId = "pm_kusum"
        ),
        NewsItem(
            id = "news_vishwakarma_toolkit",
            title = "PM Vishwakarma toolkit disbursement drive in rural districts",
            summary = "Artisans and craftspeople registered under PM Vishwakarma are being contacted for toolkit incentive disbursement in select districts.",
            ministry = "Ministry of MSME",
            publishedOn = "15 Aug 2026",
            relatedSchemeId = "pmvishwakarma"
        ),
        NewsItem(
            id = "news_standup_women",
            title = "Stand-Up India reports rising participation from women entrepreneurs",
            summary = "Bank branches report growing uptake of the women's track of Stand-Up India for greenfield enterprise loans.",
            ministry = "Ministry of Finance",
            publishedOn = "10 Aug 2026",
            relatedSchemeId = "standup_women"
        )
    )
}

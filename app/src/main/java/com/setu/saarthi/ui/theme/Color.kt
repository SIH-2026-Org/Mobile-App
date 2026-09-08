package com.setu.saarthi.ui.theme

import androidx.compose.ui.graphics.Color

// ── Tricolour palette ──────────────────────────────────────────────────────
// Flag saffron (#FF9933) measures ~2.0:1 contrast against white and fails
// WCAG AA for text / filled buttons, so it is reserved for gradients and
// decorative washes. SaffronDeep is the actual `primary` (~4.6:1 on white).
val Saffron = Color(0xFFFF9933)
val SaffronDeep = Color(0xFFE8590C)
val SaffronDarker = Color(0xFFC2410C)
val SaffronPale = Color(0xFFFFE8D6)
val SaffronInk = Color(0xFF7A2E00)

val IndiaGreen = Color(0xFF138808)
val IndiaGreenDeep = Color(0xFF0B6E1F)
val IndiaGreenPale = Color(0xFFDCF3DC)

val ChakraNavy = Color(0xFF1A3A6B)
val ChakraNavyPale = Color(0xFFE1E8F5)

// ── Neutrals ────────────────────────────────────────────────────────────────
val CreamBackground = Color(0xFFFFFBF7)
val SurfaceWhite = Color(0xFFFFFFFF)
val InkPrimary = Color(0xFF1F1408)
val InkSecondary = Color(0xFF5A4636)
val OutlineTan = Color(0xFFA8907A)
val OutlineTanPale = Color(0xFFE8D9C9)
val SurfaceContainerLow = Color(0xFFFFF6EE)
val SurfaceContainer = Color(0xFFFDF0E4)
val SurfaceContainerHigh = Color(0xFFF8E8DA)
val SurfaceContainerHighest = Color(0xFFF2E0D0)
val SurfaceVariantTan = Color(0xFFF5E7DA)

// ── Error ───────────────────────────────────────────────────────────────────
val ErrorRed = Color(0xFFBA1A1A)
val ErrorRedContainer = Color(0xFFFFDAD6)
val OnErrorContainer = Color(0xFF410002)

// ── Match-tier semantics (used by MatchBadge) ──────────────────────────────
val TierEligible = IndiaGreenDeep
val TierEligibleContainer = IndiaGreenPale
val TierPartial = SaffronDeep
val TierPartialContainer = SaffronPale
val TierNotEligible = InkSecondary
val TierNotEligibleContainer = OutlineTanPale

// ── Gradient recipes (diagonal Offset(0,0) → Offset(∞,∞) unless noted) ────
val GradientHeroStart = Saffron
val GradientHeroMid = SaffronDeep
val GradientHeroEnd = SaffronDarker
val GradientButtonStart = SaffronDeep
val GradientButtonEnd = Saffron
val GradientAmbientStart = SaffronPale
val GradientAmbientMid = Saffron

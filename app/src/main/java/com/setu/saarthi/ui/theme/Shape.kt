package com.setu.saarthi.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Named radii tokens so nothing downstream needs an inline .dp literal.
object SaarthiRadius {
    val Field = 14.dp
    val Card = 16.dp
    val Sheet = 20.dp
    val Hero = 28.dp
}

val PillShape = CircleShape
val FieldShape = RoundedCornerShape(SaarthiRadius.Field)
val CardShape = RoundedCornerShape(SaarthiRadius.Card)
val SheetShape = RoundedCornerShape(
    topStart = SaarthiRadius.Sheet,
    topEnd = SaarthiRadius.Sheet
)
val HeroShape = RoundedCornerShape(SaarthiRadius.Hero)

val SaarthiShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = FieldShape,
    large = CardShape,
    extraLarge = HeroShape
)

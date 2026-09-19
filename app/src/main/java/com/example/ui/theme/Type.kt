package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

// Static Font definitions with fallback to system fonts
val InterFontFamily: FontFamily = FontFamily(
    Font(R.font.inter, FontWeight.Medium),
    Font(R.font.inter, FontWeight.Bold),
    Font(R.font.inter, FontWeight.Normal)
)

val BowlbyOneFontFamily: FontFamily = FontFamily(
    Font(R.font.bowlby_one, FontWeight.Normal)
)

val Typography = Typography(
    // Display used specifically for prominent totals (40-48sp Bowlby One)
    displayLarge = TextStyle(
        fontFamily = BowlbyOneFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 44.sp,
        lineHeight = 50.sp,
        letterSpacing = 0.sp
    ),
    displayMedium = TextStyle(
        fontFamily = BowlbyOneFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 38.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = BowlbyOneFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    titleSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.sp
    ),
    labelLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp
    ),
    labelSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.3.sp
    )
)

fun getScaledTypography(scaleFactor: Float): Typography {
    if (scaleFactor == 1.0f) return Typography

    fun TextStyle.scale(factor: Float): TextStyle {
        return this.copy(
            fontSize = (this.fontSize.value * factor).sp,
            lineHeight = (this.lineHeight.value * factor).sp
        )
    }

    return Typography(
        displayLarge = Typography.displayLarge.scale(scaleFactor),
        displayMedium = Typography.displayMedium.scale(scaleFactor),
        displaySmall = Typography.displaySmall.scale(scaleFactor),
        headlineLarge = Typography.headlineLarge.scale(scaleFactor),
        headlineMedium = Typography.headlineMedium.scale(scaleFactor),
        headlineSmall = Typography.headlineSmall.scale(scaleFactor),
        titleLarge = Typography.titleLarge.scale(scaleFactor),
        titleMedium = Typography.titleMedium.scale(scaleFactor),
        titleSmall = Typography.titleSmall.scale(scaleFactor),
        bodyLarge = Typography.bodyLarge.scale(scaleFactor),
        bodyMedium = Typography.bodyMedium.scale(scaleFactor),
        bodySmall = Typography.bodySmall.scale(scaleFactor),
        labelLarge = Typography.labelLarge.scale(scaleFactor),
        labelMedium = Typography.labelMedium.scale(scaleFactor),
        labelSmall = Typography.labelSmall.scale(scaleFactor)
    )
}



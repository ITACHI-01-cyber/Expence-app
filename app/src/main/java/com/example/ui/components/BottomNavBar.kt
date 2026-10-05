package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppTheme
import com.example.ui.theme.DeepBlueCTA
import com.example.ui.theme.OnWarmPeachText
import com.example.ui.theme.PlumSurface
import com.example.ui.theme.PlumSurfaceBorder
import com.example.ui.theme.PlumSurfaceContainer
import com.example.ui.theme.TextCrispWhite
import com.example.ui.theme.TextMutedLavender
import com.example.ui.theme.TextSoftPurple
import com.example.ui.theme.WarmPeach
import com.example.viewmodel.ScreenTab

/**
 * Floating Segmented Navigation Bar matching the reference design:
 * "Financial App: Segmented navigation with strong active-state clarity"
 * Features:
 * - Floating white stadium/capsule with soft drop shadow
 * - Exact Iconly icon set: Home, Explore, Transfer (⇄), Analyze, Jobs
 * - Signature glowing purple-periwinkle gradient active pill
 */
@Composable
fun BottomNavBar(
    currentTab: ScreenTab,
    onTabSelected: (ScreenTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val navBgColor = colors.surface
    val inactiveIconColor = colors.textMutedLavender
    val inactiveTextColor = colors.textSoftPurple

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating Stadium/Pill Container
        Surface(
            shape = RoundedCornerShape(36.dp),
            color = navBgColor,
            border = BorderStroke(1.dp, colors.surfaceBorder),
            shadowElevation = 14.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(36.dp),
                    ambientColor = Color.Black.copy(alpha = 0.08f),
                    spotColor = Color.Black.copy(alpha = 0.12f)
                )
                .testTag("floating_bottom_nav_bar")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Home Tab
                NavSegmentItem(
                    title = "Home",
                    isSelected = currentTab == ScreenTab.DASHBOARD,
                    onClick = { onTabSelected(ScreenTab.DASHBOARD) },
                    inactiveIconColor = inactiveIconColor,
                    inactiveTextColor = inactiveTextColor,
                    testTag = "nav_tab_home"
                ) { tint ->
                    IconlyHome(tint = tint)
                }

                // 2. Wallet Tab (formerly Explore)
                NavSegmentItem(
                    title = "Wallet",
                    isSelected = currentTab == ScreenTab.WALLET,
                    onClick = { onTabSelected(ScreenTab.WALLET) },
                    inactiveIconColor = inactiveIconColor,
                    inactiveTextColor = inactiveTextColor,
                    testTag = "nav_tab_wallet"
                ) { tint ->
                    IconlyWallet(tint = tint)
                }

                // 3. Center Transfer (⇄) Hero Tab
                CenterTransferNavSegment(
                    isSelected = currentTab == ScreenTab.TRANSACTIONS,
                    onClick = { onTabSelected(ScreenTab.TRANSACTIONS) },
                    inactiveIconColor = inactiveIconColor,
                    testTag = "nav_tab_transactions"
                )

                // 4. Analytics Tab
                NavSegmentItem(
                    title = "Analytics",
                    isSelected = currentTab == ScreenTab.BUDGET,
                    onClick = { onTabSelected(ScreenTab.BUDGET) },
                    inactiveIconColor = inactiveIconColor,
                    inactiveTextColor = inactiveTextColor,
                    testTag = "nav_tab_analyze"
                ) { tint ->
                    IconlyAnalyze(tint = tint)
                }

                // 5. Settings Tab (formerly Jobs)
                NavSegmentItem(
                    title = "Settings",
                    isSelected = currentTab == ScreenTab.SETTINGS,
                    onClick = { onTabSelected(ScreenTab.SETTINGS) },
                    inactiveIconColor = inactiveIconColor,
                    inactiveTextColor = inactiveTextColor,
                    testTag = "nav_tab_settings"
                ) { tint ->
                    IconlySettings(tint = tint)
                }
            }
        }
    }
}

/**
 * Standard Segmented Item (Home, Explore, Analyze, Jobs)
 */
@Composable
private fun NavSegmentItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    inactiveIconColor: Color,
    inactiveTextColor: Color,
    testTag: String,
    modifier: Modifier = Modifier,
    icon: @Composable (Color) -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.04f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tab_scale"
    )

    val colors = AppTheme.colors

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .scale(scale)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            // Active Segment Pill: Dynamic Capsule with Tactile Glow
            Box(contentAlignment = Alignment.Center) {
                // Soft Ambient Drop Glow
                Box(
                    modifier = Modifier
                        .size(width = 52.dp, height = 26.dp)
                        .offset(y = 8.dp)
                        .background(colors.accentGlow.copy(alpha = 0.35f), RoundedCornerShape(100))
                        .blur(8.dp)
                )

                // Dynamic Capsule
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = colors.primaryAccent,
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(width = 62.dp, height = 48.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        icon(colors.onPrimaryAccent)
                    }
                }
            }
        } else {
            // Inactive State: Outlined icon with clean subtitle label below
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                icon(inactiveIconColor)
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = inactiveTextColor,
                    letterSpacing = (-0.1).sp
                )
            }
        }
    }
}

/**
 * Center Transfer (⇄) Hero Segment Button
 * Exactly as highlighted in the reference screenshot!
 */
@Composable
private fun CenterTransferNavSegment(
    isSelected: Boolean,
    onClick: () -> Unit,
    inactiveIconColor: Color,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.06f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "center_scale"
    )

    val colors = AppTheme.colors

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .scale(scale)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            // Active Center Segment: Dynamic Pill with Soft Halo
            Box(contentAlignment = Alignment.Center) {
                // Soft Ambient Drop Glow
                Box(
                    modifier = Modifier
                        .size(width = 54.dp, height = 28.dp)
                        .offset(y = 10.dp)
                        .background(colors.accentGlow.copy(alpha = 0.40f), RoundedCornerShape(100))
                        .blur(10.dp)
                )

                // Dynamic Capsule
                Box(
                    modifier = Modifier
                        .size(width = 66.dp, height = 50.dp)
                        .background(colors.primaryAccent, RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    IconlyTransfer(tint = colors.onPrimaryAccent)
                }
            }
        } else {
            // Inactive Center Segment: Soft subtle pill container with Transfer arrows
            Box(
                modifier = Modifier
                    .size(width = 58.dp, height = 46.dp)
                    .background(colors.surfaceContainer, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                IconlyTransfer(tint = colors.textMutedLavender)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Custom Iconly Vector Set Drawables (Home, Explore, Transfer ⇄, Analyze, Jobs)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * 1. Iconly Home: Arched roof modern outline with open doorway
 */
@Composable
fun IconlyHome(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(23.dp)) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 2.1.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

        val path = Path().apply {
            // Roof peak
            moveTo(w * 0.50f, h * 0.12f)
            // Left roof slope
            lineTo(w * 0.16f, h * 0.42f)
            // Left wall
            lineTo(w * 0.16f, h * 0.82f)
            // Bottom left corner
            quadraticTo(w * 0.16f, h * 0.90f, w * 0.26f, h * 0.90f)
            // Bottom line to doorway left
            lineTo(w * 0.38f, h * 0.90f)
            // Doorway left post up
            lineTo(w * 0.38f, h * 0.62f)
            // Doorway arch top
            quadraticTo(w * 0.50f, h * 0.54f, w * 0.62f, h * 0.62f)
            // Doorway right post down
            lineTo(w * 0.62f, h * 0.90f)
            // Bottom line to right wall
            lineTo(w * 0.74f, h * 0.90f)
            // Bottom right corner
            quadraticTo(w * 0.84f, h * 0.90f, w * 0.84f, h * 0.82f)
            // Right wall
            lineTo(w * 0.84f, h * 0.42f)
            // Close to roof peak
            close()
        }
        drawPath(path = path, color = tint, style = stroke)
    }
}

/**
 * 2. Iconly Explore: Compass circle with angled needle diamond
 */
@Composable
fun IconlyExplore(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(23.dp)) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 2.0.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val center = Offset(w / 2f, h / 2f)
        val radius = w * 0.40f

        // Outer circular bezel
        drawCircle(color = tint, radius = radius, center = center, style = stroke)

        // Inner compass diamond
        val needlePath = Path().apply {
            moveTo(w * 0.60f, h * 0.28f)
            lineTo(w * 0.45f, h * 0.45f)
            lineTo(w * 0.40f, h * 0.72f)
            lineTo(w * 0.55f, h * 0.55f)
            close()
        }
        drawPath(path = needlePath, color = tint, style = stroke)
    }
}

/**
 * 3. Iconly Transfer (⇄): Two opposing horizontal arrows exactly as in image
 */
@Composable
fun IconlyTransfer(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val strokeWidth = 2.2.dp.toPx()
        val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Top arrow pointing Right (→)
        val topY = h * 0.38f
        val leftX = w * 0.22f
        val rightX = w * 0.78f
        val headSize = w * 0.16f

        drawLine(
            color = tint,
            start = Offset(leftX, topY),
            end = Offset(rightX, topY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        val topHead = Path().apply {
            moveTo(rightX - headSize, topY - headSize)
            lineTo(rightX, topY)
            lineTo(rightX - headSize, topY + headSize)
        }
        drawPath(path = topHead, color = tint, style = stroke)

        // Bottom arrow pointing Left (←)
        val botY = h * 0.62f
        drawLine(
            color = tint,
            start = Offset(rightX, botY),
            end = Offset(leftX, botY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        val botHead = Path().apply {
            moveTo(leftX + headSize, botY - headSize)
            lineTo(leftX, botY)
            lineTo(leftX + headSize, botY + headSize)
        }
        drawPath(path = botHead, color = tint, style = stroke)
    }
}

/**
 * 4. Iconly Analyze: Rounded squircle card with rising line chart
 */
@Composable
fun IconlyAnalyze(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(23.dp)) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 2.0.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Rounded card outline
        val cardRect = RoundRect(
            rect = Rect(w * 0.12f, h * 0.12f, w * 0.88f, h * 0.88f),
            cornerRadius = CornerRadius(w * 0.22f, h * 0.22f)
        )
        val cardPath = Path().apply { addRoundRect(cardRect) }
        drawPath(path = cardPath, color = tint, style = stroke)

        // Rising zigzag chart line inside
        val chartPath = Path().apply {
            moveTo(w * 0.26f, h * 0.68f)
            lineTo(w * 0.44f, h * 0.42f)
            lineTo(w * 0.58f, h * 0.54f)
            lineTo(w * 0.74f, h * 0.32f)
        }
        drawPath(path = chartPath, color = tint, style = stroke)
    }
}

/**
 * 5. Iconly Settings: Clean modern precision gear / cog
 */
@Composable
fun IconlySettings(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(23.dp)) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 2.0.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val center = Offset(w / 2f, h / 2f)

        // Center hub circle
        drawCircle(color = tint, radius = w * 0.16f, center = center, style = stroke)

        // Outer 6-tooth gear perimeter
        val gearPath = Path()
        val numTeeth = 6
        val outerRadius = w * 0.42f
        val innerRadius = w * 0.31f
        for (i in 0 until numTeeth) {
            val a0 = Math.toRadians((i * 60.0)).toFloat()
            val a1 = Math.toRadians((i * 60.0 + 16.0)).toFloat()
            val a2 = Math.toRadians((i * 60.0 + 36.0)).toFloat()
            val a3 = Math.toRadians((i * 60.0 + 52.0)).toFloat()

            val p0 = Offset(center.x + innerRadius * kotlin.math.cos(a0), center.y + innerRadius * kotlin.math.sin(a0))
            val p1 = Offset(center.x + outerRadius * kotlin.math.cos(a1), center.y + outerRadius * kotlin.math.sin(a1))
            val p2 = Offset(center.x + outerRadius * kotlin.math.cos(a2), center.y + outerRadius * kotlin.math.sin(a2))
            val p3 = Offset(center.x + innerRadius * kotlin.math.cos(a3), center.y + innerRadius * kotlin.math.sin(a3))

            if (i == 0) gearPath.moveTo(p0.x, p0.y) else gearPath.lineTo(p0.x, p0.y)
            gearPath.lineTo(p1.x, p1.y)
            gearPath.lineTo(p2.x, p2.y)
            gearPath.lineTo(p3.x, p3.y)
        }
        gearPath.close()
        drawPath(path = gearPath, color = tint, style = stroke)
    }
}

/**
 * 6. Iconly Wallet: Modern card & wallet silhouette with clasp
 */
@Composable
fun IconlyWallet(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(23.dp)) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 2.0.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Main wallet body
        val walletRect = RoundRect(
            rect = Rect(w * 0.12f, h * 0.22f, w * 0.88f, h * 0.82f),
            cornerRadius = CornerRadius(w * 0.18f, h * 0.18f)
        )
        val walletPath = Path().apply { addRoundRect(walletRect) }
        drawPath(path = walletPath, color = tint, style = stroke)

        // Card slot line
        drawLine(
            color = tint,
            start = Offset(w * 0.14f, h * 0.40f),
            end = Offset(w * 0.60f, h * 0.40f),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round
        )

        // Right clasp flap
        val flapPath = Path().apply {
            moveTo(w * 0.60f, h * 0.40f)
            lineTo(w * 0.84f, h * 0.40f)
            quadraticTo(w * 0.90f, h * 0.52f, w * 0.84f, h * 0.64f)
            lineTo(w * 0.60f, h * 0.64f)
            close()
        }
        drawPath(path = flapPath, color = tint, style = stroke)

        // Small clasp snap circle
        drawCircle(
            color = tint,
            radius = w * 0.04f,
            center = Offset(w * 0.74f, h * 0.52f)
        )
    }
}

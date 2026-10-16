package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.analytics.calculateCreditCardSummary
import com.example.model.WalletEntity
import java.util.Locale

@Composable
fun LeatherCardPocket(
    wallet: WalletEntity,
    currencySymbol: String,
    modifier: Modifier = Modifier,
    isFrozen: Boolean = false,
    onCardClick: () -> Unit
) {
    val creditSum = calculateCreditCardSummary(wallet)
    val displayBalance = if (wallet.cardType.equals("credit", ignoreCase = true)) {
        creditSum?.availableCredit ?: 0.0
    } else {
        wallet.balance
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(265.dp)
            .padding(horizontal = 4.dp, vertical = 6.dp)
            .clickable { onCardClick() },
        contentAlignment = Alignment.TopCenter
    ) {
        // 1. Payment Card (Positioned in the upper portion, ~70% exposed, ~30% inserted into pocket behind lip)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(horizontal = 12.dp)
        ) {
            CustomizablePaymentCardView(
                wallet = wallet,
                currencySymbol = currencySymbol,
                isFrozen = isFrozen,
                showCardholderAndExpiry = false
            )
        }

        // 2. 3D Leather Pocket Base & Lip (Positioned at the bottom, overlapping the lower 30% of the card with natural occlusion & depth)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(125.dp)
                .align(Alignment.BottomCenter)
                .shadow(
                    elevation = 18.dp,
                    shape = RoundedCornerShape(26.dp),
                    spotColor = Color.Black.copy(alpha = 0.6f),
                    ambientColor = Color.Black.copy(alpha = 0.4f)
                )
                .clip(RoundedCornerShape(26.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E222B),
                            Color(0xFF14171F),
                            Color(0xFF0C0E12)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.25f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    ),
                    shape = RoundedCornerShape(26.dp)
                )
        ) {
            // Leather Grain, Pocket Opening Shadow & Stitching Details
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Inner Pocket Mouth Shadow (recessed interior depth where card enters)
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.65f), Color.Transparent),
                        startY = 0f,
                        endY = 28f
                    )
                )

                // Upper Curved Pocket Lip Curve
                val lipPath = Path().apply {
                    moveTo(0f, 0f)
                    cubicTo(w * 0.3f, 14f, w * 0.7f, 14f, w, 0f)
                    lineTo(w, 24f)
                    lineTo(0f, 24f)
                    close()
                }
                drawPath(
                    path = lipPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF2C323D), Color(0xFF181B22)),
                        startY = 0f,
                        endY = 24f
                    )
                )

                // Top Edge Specular Highlight along the lip
                drawLine(
                    color = Color.White.copy(alpha = 0.28f),
                    start = Offset(24f, 1f),
                    end = Offset(w - 24f, 1f),
                    strokeWidth = 2.2f
                )

                // Subtle Leather Grain Dots
                val dotColor = Color.White.copy(alpha = 0.04f)
                for (i in 0..30) {
                    val cx = (i * 37) % w.toInt()
                    val cy = ((i * 19) % (h * 0.75f)).toInt() + 15
                    drawCircle(dotColor, radius = 1.5f, center = Offset(cx.toFloat(), cy.toFloat()))
                }

                // Fine Perimeter Stitching Seam
                val stitchEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.28f),
                    topLeft = Offset(12f, 12f),
                    size = Size(w - 24f, h - 22f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f, 20f),
                    style = Stroke(width = 1.5f, pathEffect = stitchEffect)
                )
            }

            // Wallet Details Info Panel on Leather Front
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = wallet.cardName.ifBlank { wallet.bankName }.ifBlank { "EXPENSETRACK CARD" },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = "•••• ${wallet.cardNumber.takeLast(4).ifBlank { "4096" }} • Tap to inspect",
                            color = Color(0xFFB0BAC5),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Available Balance",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            letterSpacing = 0.4.sp
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = "$currencySymbol${String.format(Locale.US, "%,.2f", displayBalance)}",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp
                        )
                    }
                }
            }
        }
    }
}

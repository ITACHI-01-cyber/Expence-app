package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.UserSettingsEntity
import com.example.ui.theme.AppTheme
import com.example.ui.theme.TextCrispWhite

@Composable
fun TopBar(
    userSettings: UserSettingsEntity?,
    isSyncing: Boolean = false,
    onSyncClick: () -> Unit = {},
    onAddTransactionClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    Surface(
        color = colors.background,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Title with App Mascot Logo:
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = colors.surfaceContainer,
                    border = BorderStroke(1.dp, colors.surfaceBorder),
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("app_home_logo")
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_rabbit_logo),
                        contentDescription = "Rack App Mascot Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(3.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                }

                Column {
                    Text(
                        text = "Rack",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textMutedLavender,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = "Expense Tracker",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextCrispWhite,
                        letterSpacing = (-0.4).sp
                    )
                }
            }

            // Right Actions: Quick Add and Avatar Bubble with Name (Refresh icon removed as requested)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Add Transaction button
                Surface(
                    onClick = onAddTransactionClick,
                    shape = RoundedCornerShape(14.dp),
                    color = colors.ctaButton,
                    border = BorderStroke(1.dp, colors.surfaceBorder),
                    modifier = Modifier.testTag("top_add_transaction_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = colors.onCtaButton,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add",
                            color = colors.onCtaButton,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                // Profile Avatar with Name Pill
                Surface(
                    onClick = onProfileClick,
                    shape = RoundedCornerShape(18.dp),
                    color = colors.surfaceContainer,
                    border = BorderStroke(1.dp, colors.surfaceBorder),
                    modifier = Modifier.testTag("user_avatar_btn")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(colors.primaryAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🧔",
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = userSettings?.name?.split(" ")?.firstOrNull() ?: "Vivek",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textMutedLavender
                        )
                    }
                }
            }
        }
    }
}

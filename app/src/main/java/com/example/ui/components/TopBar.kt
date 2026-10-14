package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.UserSettingsEntity
import com.example.ui.theme.AppTheme
import java.io.File

@Composable
fun TopBar(
    userSettings: UserSettingsEntity?,
    isSyncing: Boolean = false,
    onSyncClick: () -> Unit = {},
    onAddTransactionClick: () -> Unit = {},
    onProfileClick: () -> Unit,
    onHelpClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val fullName = userSettings?.name?.trim()?.takeIf(String::isNotBlank) ?: "User"
    val profileUri = userSettings?.profilePictureUri.orEmpty()
    val photoFile = remember(profileUri) {
        if (profileUri.isNotBlank()) File(profileUri) else null
    }

    Surface(
        color = colors.background,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Circular User Avatar with Greeting
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onProfileClick)
                    .padding(vertical = 4.dp, horizontal = 2.dp)
                    .testTag("user_avatar_btn")
            ) {
                // Avatar 42dp with Camera Overlay
                Box(
                    modifier = Modifier.size(44.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceContainer)
                            .border(1.5.dp, Color(0xFFA855F7), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoFile != null && photoFile.exists()) {
                            AsyncImage(
                                model = photoFile,
                                contentDescription = "Profile Picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(42.dp).clip(CircleShape)
                            )
                        } else {
                            Text(
                                text = fullName.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }

                    // Small Camera Badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFA855F7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Edit Profile",
                            tint = Color.White,
                            modifier = Modifier.size(9.dp)
                        )
                    }
                }

                // Greeting & Full Display Name
                Column(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Hello,",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textMutedLavender,
                        letterSpacing = 0.2.sp
                    )
                    Text(
                        text = fullName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textCrispWhite,
                        letterSpacing = (-0.2).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

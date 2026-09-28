package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.DeepBlueCTA
import com.example.ui.theme.OnWarmPeachText
import com.example.ui.theme.PlumBackground
import com.example.ui.theme.PlumSurface
import com.example.ui.theme.PlumSurfaceBorder
import com.example.ui.theme.PlumSurfaceContainer
import com.example.ui.theme.SliderTrackBlue
import com.example.ui.theme.SliderTrackPeach
import com.example.ui.theme.SliderTrackPurple
import com.example.ui.theme.TextCrispWhite
import com.example.ui.theme.TextMutedLavender
import com.example.ui.theme.TextSoftPurple
import com.example.ui.theme.WarmPeach
import com.example.ui.theme.WarmPeachDark
import com.example.ui.theme.WarmPeachLight
import java.util.Locale
import kotlin.math.roundToInt

data class SplitParticipant(
    val id: String,
    val name: String,
    val avatarBg: Color,
    val emoji: String,
    val defaultShareRatio: Float
)

val defaultParticipants = listOf(
    SplitParticipant("1", "Me", Color(0xFF6366F1), "😎", 0.27f),
    SplitParticipant("2", "Cody", Color(0xFFA855F7), "👨‍💻", 0.60f),
    SplitParticipant("3", "Khalifa", Color(0xFFF97316), "👳‍♂️", 0.13f)
)

val nearbyFriends = listOf(
    SplitParticipant("cody", "Cody", Color(0xFFA855F7), "👨‍💻", 0.33f),
    SplitParticipant("khalifa", "Khalifa", Color(0xFFF97316), "👳‍♂️", 0.33f),
    SplitParticipant("lisa", "Lisa", Color(0xFFEC4899), "👩‍🦰", 0.34f)
)

val recentSplits = listOf(
    SplitParticipant("sing", "Sing", Color(0xFFEF4444), "🧔", 0.25f),
    SplitParticipant("alex", "Alex", Color(0xFFF59E0B), "👱‍♂️", 0.25f),
    SplitParticipant("brain", "Brain", Color(0xFF10B981), "👴", 0.25f),
    SplitParticipant("mike", "Mike", Color(0xFF3B82F6), "🧑‍🦱", 0.25f)
)

// ─────────────────────────────────────────────────────────────────────────────
// 1. Hero Total Bill Card (Warm Peach / Sand) matching Screen 1 in reference
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun BillSplitterHeroCard(
    totalAmount: Double,
    currencySymbol: String,
    onSplitNowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = WarmPeach,
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("hero_total_bill_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Content: Title + Big Bold Metric + "Split Now" Deep Blue CTA Button
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Total Bill",
                    color = OnWarmPeachText.copy(alpha = 0.80f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "$currencySymbol${String.format(Locale.US, "%,.2f", if (totalAmount > 0) totalAmount else 750.86)}",
                    color = OnWarmPeachText,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Primary CTA Deep Blue Button
                Button(
                    onClick = onSplitNowClick,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepBlueCTA,
                        contentColor = TextCrispWhite
                    ),
                    modifier = Modifier
                        .height(44.dp)
                        .testTag("split_now_hero_btn")
                ) {
                    Text(
                        text = "Split Now",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Right Content: "Split with" label and stacked avatar pill with "+"
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Split with",
                    color = OnWarmPeachText.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                // Vertical pill containing stacked avatars
                Surface(
                    shape = RoundedCornerShape(100),
                    color = Color.White.copy(alpha = 0.95f),
                    shadowElevation = 4.dp,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy((-8).dp)
                    ) {
                        nearbyFriends.forEach { friend ->
                            AvatarBubble(
                                emoji = friend.emoji,
                                bgColor = friend.avatarBg,
                                size = 30
                            )
                        }

                        // Plus add button
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF87171))
                                .clickable { onSplitNowClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add friend to split",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 2. "Your previous split" Card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun PreviousSplitCard(
    amount: Double,
    currencySymbol: String,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = PlumSurface,
        border = BorderStroke(1.dp, PlumSurfaceBorder),
        shadowElevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("previous_split_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Darker circular inset well with info icon
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(PlumSurfaceContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Previous split info",
                    tint = TextMutedLavender,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = "Your previous split",
                    color = TextMutedLavender,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$currencySymbol${String.format(Locale.US, "%,.2f", if (amount > 0) amount else 678.56)}",
                    color = TextCrispWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 3. "Nearby Friends" with Search button & Pill Pedestals
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun NearbyFriendsRow(
    onSearchClick: () -> Unit,
    onFriendClick: (SplitParticipant) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Warm Peach Squarcle Search Button
        Surface(
            onClick = onSearchClick,
            shape = RoundedCornerShape(22.dp),
            color = WarmPeach,
            shadowElevation = 4.dp,
            modifier = Modifier
                .size(62.dp)
                .testTag("nearby_search_btn")
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search friends",
                    tint = OnWarmPeachText,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Nearby Friends Container
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = PlumSurface,
            border = BorderStroke(1.dp, PlumSurfaceBorder),
            modifier = Modifier
                .weight(1f)
                .testTag("nearby_friends_container")
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nearby Friends",
                        color = TextCrispWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "See all",
                        color = TextMutedLavender,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Friends Pill Pedestals
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    nearbyFriends.forEach { friend ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onFriendClick(friend) }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            AvatarBubble(
                                emoji = friend.emoji,
                                bgColor = friend.avatarBg,
                                size = 38
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = friend.name,
                                color = TextMutedLavender,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            // Small peach pedestal dot
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(WarmPeach)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 4. "Recently Split" Avatars Row
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun RecentlySplitRow(
    onSelectPerson: (SplitParticipant) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Recently Split",
            color = TextCrispWhite,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            recentSplits.forEach { person ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelectPerson(person) }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    AvatarBubble(
                        emoji = person.emoji,
                        bgColor = person.avatarBg,
                        size = 46
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = person.name,
                        color = TextMutedLavender,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 5. Screen 2: Split Execution / Receipt Voucher & Interactive Sliders
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun SplitExecutionDialog(
    initialTotal: Double,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (List<Pair<String, Double>>) -> Unit
) {
    var title by remember { mutableStateOf("Team Dinner") }
    var totalBill by remember { mutableDoubleStateOf(if (initialTotal > 0) initialTotal else 750.86) }

    // Participant sliders ratios (Me, Cody, Khalifa)
    var ratioMe by remember { mutableStateOf(0.2675f) }
    var ratioCody by remember { mutableStateOf(0.5993f) }
    var ratioKhalifa by remember { mutableStateOf(0.1332f) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = PlumBackground,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Squarcle Back Button
                    Surface(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(16.dp),
                        color = PlumSurface,
                        border = BorderStroke(1.dp, PlumSurfaceBorder),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextCrispWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Text(
                        text = "Split Now",
                        color = TextCrispWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = TextMutedLavender
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Physical "Receipt" Voucher Card with Cutout Notches
                ReceiptVoucherCard(
                    title = title,
                    totalBill = totalBill,
                    currencySymbol = currencySymbol
                )

                Spacer(modifier = Modifier.height(26.dp))

                // Custom Interactive Sliders with Avatars
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(22.dp)
                ) {
                    val amountMe = totalBill * ratioMe
                    val amountCody = totalBill * ratioCody
                    val amountKhalifa = totalBill * ratioKhalifa

                    ParticipantSplitSlider(
                        name = "Me",
                        amount = amountMe,
                        currencySymbol = currencySymbol,
                        emoji = "😎",
                        avatarBg = Color(0xFF6366F1),
                        progress = ratioMe,
                        trackColor = SliderTrackBlue,
                        onProgressChange = { newRatio ->
                            ratioMe = newRatio
                            // Adjust remaining proportionally
                            val remaining = (1f - ratioMe).coerceAtLeast(0.01f)
                            val otherTotal = (ratioCody + ratioKhalifa).coerceAtLeast(0.01f)
                            ratioCody = (ratioCody / otherTotal) * remaining
                            ratioKhalifa = (ratioKhalifa / otherTotal) * remaining
                        }
                    )

                    ParticipantSplitSlider(
                        name = "Cody",
                        amount = amountCody,
                        currencySymbol = currencySymbol,
                        emoji = "👨‍💻",
                        avatarBg = Color(0xFFA855F7),
                        progress = ratioCody,
                        trackColor = SliderTrackPurple,
                        onProgressChange = { newRatio ->
                            ratioCody = newRatio
                            val remaining = (1f - ratioCody).coerceAtLeast(0.01f)
                            val otherTotal = (ratioMe + ratioKhalifa).coerceAtLeast(0.01f)
                            ratioMe = (ratioMe / otherTotal) * remaining
                            ratioKhalifa = (ratioKhalifa / otherTotal) * remaining
                        }
                    )

                    ParticipantSplitSlider(
                        name = "Khalifa",
                        amount = amountKhalifa,
                        currencySymbol = currencySymbol,
                        emoji = "👳‍♂️",
                        avatarBg = Color(0xFFF97316),
                        progress = ratioKhalifa,
                        trackColor = SliderTrackPeach,
                        onProgressChange = { newRatio ->
                            ratioKhalifa = newRatio
                            val remaining = (1f - ratioKhalifa).coerceAtLeast(0.01f)
                            val otherTotal = (ratioMe + ratioCody).coerceAtLeast(0.01f)
                            ratioMe = (ratioMe / otherTotal) * remaining
                            ratioCody = (ratioCody / otherTotal) * remaining
                        }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Bottom Action buttons in thumb zone
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val results = listOf(
                                "Me" to (totalBill * ratioMe),
                                "Cody" to (totalBill * ratioCody),
                                "Khalifa" to (totalBill * ratioKhalifa)
                            )
                            onConfirm(results)
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeepBlueCTA,
                            contentColor = TextCrispWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("confirm_split_button")
                    ) {
                        Text(
                            text = "Confirm Split",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("cancel_split_button")
                    ) {
                        Text(
                            text = "Cancel",
                            color = TextMutedLavender,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Receipt Voucher Card with side notch cutouts
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ReceiptVoucherCard(
    title: String,
    totalBill: Double,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(WarmPeach)
            .padding(vertical = 18.dp, horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Receipt Tag Pill
            Surface(
                shape = RoundedCornerShape(100),
                color = DeepBlueCTA
            ) {
                Text(
                    text = "Receipt",
                    color = TextCrispWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 5.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Two Columns: Title & Total Bill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Title",
                        color = OnWarmPeachText.copy(alpha = 0.70f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = title,
                        color = OnWarmPeachText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total Bill",
                        color = OnWarmPeachText.copy(alpha = 0.70f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$currencySymbol${String.format(Locale.US, "%,.2f", totalBill)}",
                        color = OnWarmPeachText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dark Purple Inset Well: Grouped Avatars "Spliting With"
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = PlumSurfaceContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy((-6).dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AvatarBubble("😎", Color(0xFF6366F1), 32)
                        AvatarBubble("👨‍💻", Color(0xFFA855F7), 32)
                        AvatarBubble("👳‍♂️", Color(0xFFF97316), 32)
                    }

                    Text(
                        text = "Splitting With",
                        color = TextMutedLavender,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Interactive Participant Split Slider
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ParticipantSplitSlider(
    name: String,
    amount: Double,
    currencySymbol: String,
    emoji: String,
    avatarBg: Color,
    progress: Float,
    trackColor: Color,
    onProgressChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Name & Amount Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AvatarBubble(emoji = emoji, bgColor = avatarBg, size = 34)
                Text(
                    text = name,
                    color = TextCrispWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = "$currencySymbol${String.format(Locale.US, "%,.2f", amount)}",
                color = TextCrispWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Custom Slider Track with Step Dots & Tactile Warm Peach Thumb
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            val totalWidthPx = constraints.maxWidth.toFloat()
            val thumbRadius = 14.dp
            val trackHeight = 12.dp

            // Background Track with Inactive Dots
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(trackHeight)
                    .clip(RoundedCornerShape(100))
                    .background(PlumSurfaceContainer)
            ) {
                // Step Dots
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(8) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.35f))
                        )
                    }
                }

                // Active Colored Fill
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = progress.coerceIn(0.05f, 1f))
                        .height(trackHeight)
                        .clip(RoundedCornerShape(100))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    trackColor.copy(alpha = 0.6f),
                                    trackColor,
                                    WarmPeach
                                )
                            )
                        )
                )
            }

            // Tactile Warm Peach Thumb with drag gesture
            val thumbOffsetXPx = (totalWidthPx * progress).coerceIn(0f, totalWidthPx - 28)

            Box(
                modifier = Modifier
                    .offset { IntOffset(thumbOffsetXPx.roundToInt(), 0) }
                    .size(28.dp)
                    .shadow(elevation = 6.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(WarmPeach)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { change, dragAmount ->
                            change.consume()
                            val newRatio = ((thumbOffsetXPx + dragAmount) / totalWidthPx).coerceIn(0.05f, 0.95f)
                            onProgressChange(newRatio)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(OnWarmPeachText.copy(alpha = 0.6f))
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Reusable Avatar Bubble
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AvatarBubble(
    emoji: String,
    bgColor: Color,
    size: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            fontSize = (size * 0.50f).sp,
            textAlign = TextAlign.Center
        )
    }
}

package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TransactionEntity
import com.example.model.WalletEntity
import com.example.ui.components.CardCustomizationScreen
import com.example.ui.components.CardDetailsBottomSheet
import com.example.ui.components.CustomizablePaymentCardView
import com.example.ui.components.TransactionItemRow
import com.example.ui.components.formatCurrency
import com.example.ui.theme.AppTheme
import com.example.ui.theme.DangerRed

@Composable
fun WalletScreen(
    wallets: List<WalletEntity>,
    transactions: List<TransactionEntity>,
    currencySymbol: String,
    onAddCard: () -> Unit,
    onTopUpCard: (WalletEntity) -> Unit,
    onDeleteCard: (String) -> Unit,
    onTransactionClick: (TransactionEntity) -> Unit,
    onSaveCardCustomization: (walletId: String, theme: String, primaryColor: String, secondaryColor: String, accentColor: String, artwork: String, cardStyle: String) -> Unit = { _, _, _, _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    var selectedWalletId by remember(wallets) { mutableStateOf(wallets.firstOrNull()?.id ?: "") }
    val selectedWallet = wallets.find { it.id == selectedWalletId } ?: wallets.firstOrNull()

    // Modals state: Card Details and Card Customization
    var cardForDetails by remember { mutableStateOf<WalletEntity?>(null) }
    var cardForCustomization by remember { mutableStateOf<WalletEntity?>(null) }

    val totalBalance = wallets.sumOf { it.balance }
    val cardTransactions = if (selectedWallet != null) {
        transactions.filter { it.walletId == selectedWallet.id }
    } else emptyList()

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 90.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Overall Balance Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = BorderStroke(1.dp, colors.surfaceBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TOTAL AVAILABLE BALANCE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textMutedLavender,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatCurrency(totalBalance, currencySymbol),
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.textCrispWhite,
                                letterSpacing = (-0.5).sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${wallets.size} Accounts & Cards Connected",
                                fontSize = 12.sp,
                                color = colors.brandColor,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Surface(
                            onClick = onAddCard,
                            shape = RoundedCornerShape(12.dp),
                            color = colors.primaryAccent,
                            modifier = Modifier.testTag("wallet_screen_add_card_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = colors.onPrimaryAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Add Card",
                                    color = colors.onPrimaryAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                )
                            }
                        }
                    }
                }
            }

            // Interactive Card Selection Carousel
            if (wallets.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Payment Source",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textCrispWhite
                        )
                        Text(
                            text = "Tap to inspect & customize",
                            fontSize = 11.5.sp,
                            color = colors.textMutedLavender
                        )
                    }
                }

                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(wallets, key = { it.id }) { wallet ->
                            val isSelected = wallet.id == selectedWalletId
                            Box(
                                modifier = Modifier
                                    .width(280.dp)
                                    .clickable {
                                        selectedWalletId = wallet.id
                                        cardForDetails = wallet
                                    }
                                    .clip(RoundedCornerShape(26.dp))
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) colors.primaryAccent else colors.surfaceBorder,
                                        shape = RoundedCornerShape(26.dp)
                                    )
                                    .testTag("wallet_carousel_card_${wallet.id}")
                            ) {
                                CustomizablePaymentCardView(
                                    wallet = wallet,
                                    currencySymbol = currencySymbol
                                )
                            }
                        }
                    }
                }
            } else {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = colors.brandColor,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No cards or accounts linked yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.textCrispWhite
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Add your first payment card to track balances and expenses by card.",
                                fontSize = 12.sp,
                                color = colors.textMutedLavender
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onAddCard,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primaryAccent)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add New Card", color = colors.onPrimaryAccent, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Prominent Selected Card Display & Quick Management Actions
            if (selectedWallet != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        border = BorderStroke(1.dp, colors.surfaceBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${selectedWallet.bankName} Card Details",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textCrispWhite,
                                    letterSpacing = (-0.3).sp
                                )

                                if (wallets.size > 1) {
                                    IconButton(
                                        onClick = { onDeleteCard(selectedWallet.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Card",
                                            tint = DangerRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            // Active Card Preview with real live customizations
                            CustomizablePaymentCardView(
                                wallet = selectedWallet,
                                currencySymbol = currencySymbol
                            )

                            // Quick Action Buttons: [ Customize Card ] & [ Top Up ]
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { cardForCustomization = selectedWallet },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.primaryAccent,
                                        contentColor = colors.onPrimaryAccent
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("wallet_customize_card_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Palette,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Customize Card",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }

                                OutlinedButton(
                                    onClick = { onTopUpCard(selectedWallet) },
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, colors.surfaceBorder),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("wallet_top_up_card_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = colors.brandColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Top Up",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = colors.brandColor
                                    )
                                }
                            }

                            // Card Meta Details
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Identifier",
                                        fontSize = 11.sp,
                                        color = colors.textMutedLavender
                                    )
                                    Text(
                                        text = selectedWallet.cardNumber,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = colors.textCrispWhite
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Card Theme Style",
                                        fontSize = 11.sp,
                                        color = colors.textMutedLavender
                                    )
                                    Text(
                                        text = selectedWallet.cardTheme.ifBlank { selectedWallet.designId }.uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = colors.brandColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Recent Card Transactions
            item {
                Text(
                    text = "Card Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.textCrispWhite
                )
            }

            if (cardTransactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        border = BorderStroke(1.dp, colors.surfaceBorder)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No transactions linked to this card yet.",
                                fontSize = 13.sp,
                                color = colors.textMutedLavender
                            )
                        }
                    }
                }
            } else {
                items(cardTransactions.take(8), key = { it.id }) { tx ->
                    TransactionItemRow(
                        transaction = tx,
                        walletName = selectedWallet?.bankName ?: "",
                        currencySymbol = currencySymbol,
                        onClick = { onTransactionClick(tx) }
                    )
                }
            }
        }

        // Card Details Bottom Sheet (when tapping a card)
        if (cardForDetails != null) {
            CardDetailsBottomSheet(
                wallet = cardForDetails!!,
                currencySymbol = currencySymbol,
                onCustomizeClick = {
                    cardForCustomization = cardForDetails
                    cardForDetails = null
                },
                onTopUpClick = {
                    val w = cardForDetails!!
                    cardForDetails = null
                    onTopUpCard(w)
                },
                onDeleteClick = if (wallets.size > 1) {
                    {
                        val id = cardForDetails!!.id
                        cardForDetails = null
                        onDeleteCard(id)
                    }
                } else null,
                onDismiss = { cardForDetails = null }
            )
        }

        // Full Card Customization Screen
        if (cardForCustomization != null) {
            CardCustomizationScreen(
                wallet = cardForCustomization!!,
                currencySymbol = currencySymbol,
                onSaveCustomization = { walletId, theme, primaryColor, secondaryColor, accentColor, artwork, cardStyle ->
                    onSaveCardCustomization(walletId, theme, primaryColor, secondaryColor, accentColor, artwork, cardStyle)
                    cardForCustomization = null
                },
                onDismiss = { cardForCustomization = null }
            )
        }
    }
}

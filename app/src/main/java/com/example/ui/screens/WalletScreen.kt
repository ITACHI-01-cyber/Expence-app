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
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.analytics.calculateCreditCardSummary
import com.example.model.TransactionEntity
import com.example.model.WalletEntity
import com.example.ui.components.CardCustomizationScreen
import com.example.ui.components.CardDetailsBottomSheet
import com.example.ui.components.CustomizablePaymentCardView
import com.example.ui.components.TransactionItemRow
import com.example.ui.components.formatCurrency
import com.example.ui.theme.AppTheme
import com.example.ui.theme.DangerRed
import java.util.Locale
import java.util.UUID
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@Composable
fun WalletScreen(
    wallets: List<WalletEntity>,
    cardDisplayBalances: Map<String, Double> = emptyMap(),
    transactions: List<TransactionEntity>,
    currencySymbol: String,
    onAddCard: () -> Unit,
    onTopUpCard: (WalletEntity) -> Unit,
    onDeleteCard: (String) -> Unit,
    onTransactionClick: (TransactionEntity) -> Unit,
    onSaveCardCustomization: (walletId: String, theme: String, primaryColor: String, secondaryColor: String, accentColor: String, artwork: String, cardStyle: String) -> Unit = { _, _, _, _, _, _, _ -> },
    onSaveDisplayBalance: (walletId: String, balance: Double, onComplete: (Boolean) -> Unit) -> Unit =
        { _, _, _ -> },
    onUpdateCreditSettings: (WalletEntity, (Boolean) -> Unit) -> Unit = { _, onComplete -> onComplete(false) },
    onRecordCreditPurchase: (WalletEntity) -> Unit = {},
    creditStatements: Map<String, List<com.example.network.CreditStatement>> = emptyMap(),
    creditActivity: Map<String, List<com.example.network.CreditActivityItem>> = emptyMap(),
    creditDataLoading: Boolean = false,
    isCreditRepaymentInProgress: Boolean = false,
    isCreditRefundInProgress: Boolean = false,
    onLoadCreditCardData: (String) -> Unit = {},
    onGenerateCreditCardStatement: (String) -> Unit = {},
    onRecordCreditCardRepayment: (
        cardId: String,
        sourceWalletId: String,
        amount: Double,
        statementId: String?,
        idempotencyKey: String,
        onComplete: (Boolean) -> Unit
    ) -> Unit = { _, _, _, _, _, onComplete -> onComplete(false) },
    onRecordCreditCardRefund: (
        purchaseId: String,
        amount: Double,
        idempotencyKey: String,
        onComplete: (Boolean) -> Unit
    ) -> Unit = { _, _, _, onComplete -> onComplete(false) },
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    var selectedWalletId by remember(wallets) { mutableStateOf(wallets.firstOrNull()?.id ?: "") }
    val selectedWallet = wallets.find { it.id == selectedWalletId } ?: wallets.firstOrNull()

    // Modals state: Card Details and Card Customization
    var cardForDetails by remember { mutableStateOf<WalletEntity?>(null) }
    var cardForCustomization by remember { mutableStateOf<WalletEntity?>(null) }
    var cardForCreditSetup by remember { mutableStateOf<WalletEntity?>(null) }
    var cardForPayment by remember { mutableStateOf<WalletEntity?>(null) }
    var purchaseForRefund by remember { mutableStateOf<com.example.network.CreditActivityItem?>(null) }

    androidx.compose.runtime.LaunchedEffect(selectedWallet?.id) {
        selectedWallet?.takeIf { it.cardType.equals("credit", ignoreCase = true) && it.creditTermsConfigured }
            ?.let { onLoadCreditCardData(it.id) }
    }

    val totalBalance = wallets
        .filterNot { it.cardType.equals("credit", ignoreCase = true) }
        .sumOf { it.balance }
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
                                text = "TOTAL AVAILABLE CASH",
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
                                text = "${wallets.count { !it.cardType.equals("credit", ignoreCase = true) }} cash accounts · ${wallets.count { it.cardType.equals("credit", ignoreCase = true) }} credit cards",
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
                                    currencySymbol = currencySymbol,
                                    displayBalance = cardDisplayBalances[wallet.id] ?: wallet.balance
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
                                    text = "${selectedWallet.cardName.ifBlank { selectedWallet.bankName }} Card Details",
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
                                currencySymbol = currencySymbol,
                                displayBalance = cardDisplayBalances[selectedWallet.id]
                                    ?: selectedWallet.balance
                            )

                            if (selectedWallet.cardType.equals("credit", ignoreCase = true)) {
                                CreditCardDetails(
                                    wallet = selectedWallet,
                                    currencySymbol = currencySymbol,
                                    statements = creditStatements[selectedWallet.id].orEmpty(),
                                    activity = creditActivity[selectedWallet.id].orEmpty(),
                                    isLoading = creditDataLoading,
                                    onSetup = { cardForCreditSetup = selectedWallet },
                                    onRefresh = { onLoadCreditCardData(selectedWallet.id) },
                                    onGenerateStatement = { onGenerateCreditCardStatement(selectedWallet.id) },
                                    onRefund = { purchaseForRefund = it }
                                )
                            }

                            // Credit card payments and purchases are separate from cash top-ups.
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

                                if (selectedWallet.cardType.equals("credit", ignoreCase = true)) {
                                    OutlinedButton(
                                        onClick = { onRecordCreditPurchase(selectedWallet) },
                                        enabled = selectedWallet.creditTermsConfigured,
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, colors.surfaceBorder),
                                        modifier = Modifier.weight(1f).height(48.dp)
                                    ) {
                                        Text("Record purchase", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = colors.brandColor)
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            cardForPayment = selectedWallet
                                            onLoadCreditCardData(selectedWallet.id)
                                        },
                                        enabled = selectedWallet.creditTermsConfigured &&
                                            (selectedWallet.outstandingBalance ?: 0.0) > 0.0,
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, colors.surfaceBorder),
                                        modifier = Modifier.weight(1f).height(48.dp)
                                    ) {
                                        Text("Pay card", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = colors.brandColor)
                                    }
                                } else {
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
                displayBalance = cardDisplayBalances[cardForDetails!!.id]
                    ?: cardForDetails!!.balance,
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
                displayBalance = cardDisplayBalances[cardForCustomization!!.id],
                onSaveDisplayBalance = onSaveDisplayBalance,
                onSaveCustomization = { walletId, theme, primaryColor, secondaryColor, accentColor, artwork, cardStyle ->
                    onSaveCardCustomization(walletId, theme, primaryColor, secondaryColor, accentColor, artwork, cardStyle)
                    cardForCustomization = null
                },
                onDismiss = { cardForCustomization = null }
            )
        }

        if (cardForCreditSetup != null) {
            CreditCardTermsDialog(
                wallet = cardForCreditSetup!!,
                onDismiss = { cardForCreditSetup = null },
                onSave = { updated, onComplete ->
                    onUpdateCreditSettings(updated) { saved ->
                        onComplete(saved)
                        if (saved) cardForCreditSetup = null
                    }
                }
            )
        }

        if (cardForPayment != null) {
            CreditCardRepaymentDialog(
                wallet = cardForPayment!!,
                sourceWallets = wallets.filterNot { it.cardType.equals("credit", ignoreCase = true) },
                statements = creditStatements[cardForPayment!!.id].orEmpty(),
                currencySymbol = currencySymbol,
                isSaving = isCreditRepaymentInProgress,
                onDismiss = { if (!isCreditRepaymentInProgress) cardForPayment = null },
                onSave = { sourceWalletId, amount, statementId, idempotencyKey ->
                    onRecordCreditCardRepayment(
                        cardForPayment!!.id,
                        sourceWalletId,
                        amount,
                        statementId,
                        idempotencyKey
                    ) { confirmed ->
                        if (confirmed) cardForPayment = null
                    }
                }
            )
        }

        if (purchaseForRefund != null && selectedWallet != null) {
            val purchase = purchaseForRefund!!
            val refundedAmount = creditActivity[selectedWallet.id].orEmpty()
                .filter {
                    it.transactionKind.equals("refund", ignoreCase = true) &&
                        it.relatedTransactionId == purchase.id
                }
                .sumOf { it.amount }
            CreditCardRefundDialog(
                purchase = purchase,
                alreadyRefunded = refundedAmount,
                currencySymbol = currencySymbol,
                isSaving = isCreditRefundInProgress,
                onDismiss = { if (!isCreditRefundInProgress) purchaseForRefund = null },
                onSave = { amount, idempotencyKey ->
                    onRecordCreditCardRefund(purchase.id, amount, idempotencyKey) { confirmed ->
                        if (confirmed) purchaseForRefund = null
                    }
                }
            )
        }
    }
}

@Composable
private fun CreditCardDetails(
    wallet: WalletEntity,
    currencySymbol: String,
    statements: List<com.example.network.CreditStatement>,
    activity: List<com.example.network.CreditActivityItem>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onGenerateStatement: () -> Unit,
    onRefund: (com.example.network.CreditActivityItem) -> Unit,
    onSetup: () -> Unit
) {
    val colors = AppTheme.colors
    val summary = calculateCreditCardSummary(wallet)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant.copy(alpha = 0.45f)),
        border = BorderStroke(1.dp, colors.surfaceBorder)
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Credit card", color = colors.textCrispWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            if (summary == null) {
                Text(
                    "Credit details are not configured. The old card balance was preserved and was not interpreted as a limit or debt.",
                    color = colors.textMutedLavender,
                    fontSize = 11.sp
                )
                OutlinedButton(onClick = onSetup) { Text("Set up credit terms") }
            } else {
                CreditValueRow("Credit limit", wallet.creditLimit ?: 0.0, currencySymbol)
                CreditValueRow("Outstanding", (wallet.outstandingBalance ?: 0.0).coerceAtLeast(0.0), currencySymbol)
                if (summary.creditBalance > 0.0) {
                    CreditValueRow("Card credit balance", summary.creditBalance, currencySymbol, colors.brandColor)
                }
                CreditValueRow("Available credit", summary.availableCredit, currencySymbol, AppTheme.colors.brandColor)
                androidx.compose.material3.LinearProgressIndicator(
                    progress = { (summary.utilizationPercent / 100.0).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape),
                    color = if (summary.utilizationPercent >= 80.0) DangerRed else colors.primaryAccent,
                    trackColor = colors.surfaceBorder
                )
                Text("Utilization ${String.format(Locale.getDefault(), "%.1f", summary.utilizationPercent)}%", color = colors.textMutedLavender, fontSize = 11.sp)
                Text("Statement closes day ${wallet.statementClosingDay} · payment due day ${wallet.paymentDueDay}", color = colors.textMutedLavender, fontSize = 11.sp)
                Text(
                    wallet.minimumPaymentAmount?.let { "Issuer minimum due: ${formatCurrency(it, currencySymbol)}" }
                        ?: "Issuer minimum due: unavailable",
                    color = colors.textMutedLavender,
                    fontSize = 11.sp
                )
                val latestStatement = statements.firstOrNull()
                val latestStatementRepayments = latestStatement?.let { statement ->
                    activity.filter {
                        it.transactionKind.equals("repayment", ignoreCase = true) &&
                            it.statementId == statement.id
                    }.sumOf { it.amount }
                } ?: 0.0
                val remaining = latestStatement?.let {
                    (it.statementBalance - latestStatementRepayments).coerceAtLeast(0.0)
                }
                val today = LocalDate.now(ZoneId.of(wallet.timezone))
                val closingDay = wallet.statementClosingDay ?: today.dayOfMonth
                val thisMonthClose = today.withDayOfMonth(minOf(closingDay, today.lengthOfMonth()))
                val lastClose = if (today.isAfter(thisMonthClose)) {
                    thisMonthClose
                } else {
                    val previousMonth = today.minusMonths(1)
                    previousMonth.withDayOfMonth(minOf(closingDay, previousMonth.lengthOfMonth()))
                }
                val currentCycleStart = lastClose.plusDays(1)
                val currentCyclePurchases = activity
                    .filter {
                        it.transactionKind.equals("purchase", ignoreCase = true) ||
                            it.transactionKind.equals("refund", ignoreCase = true)
                    }
                    .filter { item ->
                        runCatching { !LocalDate.parse(item.date.take(10)).isBefore(currentCycleStart) }
                            .getOrDefault(false)
                    }
                    .sumOf {
                        if (it.transactionKind.equals("refund", ignoreCase = true)) -it.amount else it.amount
                    }
                Text(
                    "Current-cycle net purchases: ${formatCurrency(currentCyclePurchases, currencySymbol)}",
                    color = colors.textCrispWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    latestStatement?.let { statement ->
                        "Last statement (${statement.periodStart}–${statement.periodEnd}): " +
                            "${formatCurrency(statement.statementBalance, currencySymbol)}"
                    } ?: "No statement generated yet",
                    color = colors.textMutedLavender,
                    fontSize = 11.sp
                )
                latestStatement?.let { statement ->
                    val dueDate = LocalDate.parse(statement.dueDate)
                    val daysUntilDue = ChronoUnit.DAYS.between(today, dueDate)
                    val status = when {
                        remaining != null && remaining <= 0.01 -> "Paid"
                        latestStatementRepayments > 0.0 -> "Partially paid"
                        today.isAfter(dueDate) -> "Overdue"
                        today.isBefore(dueDate) -> "Upcoming"
                        else -> "Unpaid"
                    }
                    Text(
                        "Statement due ${statement.dueDate} · $status" +
                            if (remaining != null && remaining > 0) " · Remaining ${formatCurrency(remaining, currencySymbol)}" else "",
                        color = if (status == "Overdue") DangerRed else colors.textMutedLavender,
                        fontSize = 11.sp
                    )
                    CreditValueRow(
                        "Total amount due",
                        remaining ?: 0.0,
                        currencySymbol,
                        if ((remaining ?: 0.0) > 0.0) DangerRed else colors.brandColor
                    )
                    (statement.minimumPaymentAmount ?: wallet.minimumPaymentAmount)?.let {
                        CreditValueRow("Minimum amount due", it, currencySymbol, colors.textMutedLavender)
                    } ?: Text(
                        "Minimum amount due unavailable; check the issuer's statement.",
                        color = colors.textMutedLavender,
                        fontSize = 11.sp
                    )
                    if (daysUntilDue >= 0 && remaining != null && remaining > 0) {
                        Text("$daysUntilDue day(s) until payment due", color = colors.textMutedLavender, fontSize = 11.sp)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onRefresh, enabled = !isLoading) {
                        Text(if (isLoading) "Loading..." else "Refresh statements")
                    }
                    OutlinedButton(onClick = onGenerateStatement, enabled = !isLoading) {
                        Text("Generate statement")
                    }
                }
                if (statements.isNotEmpty()) {
                    Text("Statement history", color = colors.textCrispWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    statements.take(5).forEach { statement ->
                        val paid = activity.filter {
                            it.transactionKind.equals("repayment", ignoreCase = true) &&
                                it.statementId == statement.id
                        }.sumOf { it.amount }
                        CreditValueRow(
                            "${statement.periodStart} – ${statement.periodEnd} · ${statement.dueDate}",
                            (statement.statementBalance - paid).coerceAtLeast(0.0),
                            currencySymbol
                        )
                    }
                }
                if (activity.isNotEmpty()) {
                    Text("Recent card activity", color = colors.textCrispWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    activity.take(5).forEach { item ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    item.description?.takeIf(String::isNotBlank)
                                        ?: item.category?.takeIf(String::isNotBlank)
                                        ?: if (item.transactionKind == "repayment") "Repayment" else "Purchase",
                                    color = colors.textCrispWhite,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                                Text(item.date.replace("T", " ").take(16), color = colors.textMutedLavender, fontSize = 10.sp)
                            }
                            Text(
                                (if (item.transactionKind == "repayment") "−" else "") +
                                (if (item.transactionKind == "refund") "+" else "") +
                                formatCurrency(item.amount, currencySymbol),
                                color = if (item.transactionKind == "repayment" || item.transactionKind == "refund") colors.brandColor else DangerRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        if (item.transactionKind.equals("purchase", ignoreCase = true)) {
                            TextButton(onClick = { onRefund(item) }) { Text("Refund") }
                        }
                    }
                }
                OutlinedButton(onClick = onSetup) { Text("Edit credit settings") }
            }
        }
    }
}

@Composable
private fun CreditValueRow(
    label: String,
    value: Double,
    currencySymbol: String,
    valueColor: Color = AppTheme.colors.textCrispWhite
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = AppTheme.colors.textMutedLavender, fontSize = 12.sp)
        Text(formatCurrency(value, currencySymbol), color = valueColor, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
    }
}

@Composable
private fun CreditCardRepaymentDialog(
    wallet: WalletEntity,
    sourceWallets: List<WalletEntity>,
    statements: List<com.example.network.CreditStatement>,
    currencySymbol: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (sourceWalletId: String, amount: Double, statementId: String?, idempotencyKey: String) -> Unit
) {
    var amountInput by remember(wallet.id) { mutableStateOf("") }
    var selectedSourceId by remember(wallet.id, sourceWallets) {
        mutableStateOf(sourceWallets.firstOrNull()?.id.orEmpty())
    }
    var selectedStatementId by remember(wallet.id) { mutableStateOf<String?>(null) }
    var sourceMenuExpanded by remember { mutableStateOf(false) }
    var statementMenuExpanded by remember { mutableStateOf(false) }
    var error by remember(wallet.id) { mutableStateOf<String?>(null) }
    var idempotencyKey by remember(wallet.id) { mutableStateOf(UUID.randomUUID().toString()) }
    val selectedSource = sourceWallets.firstOrNull { it.id == selectedSourceId }
    val parsedAmount = amountInput.trim().toDoubleOrNull()

    androidx.compose.ui.window.Dialog(onDismissRequest = { if (!isSaving) onDismiss() }) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface)
        ) {
            Column(
                Modifier.fillMaxWidth()
                    .verticalScroll(androidx.compose.foundation.rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Pay credit card", color = AppTheme.colors.textCrispWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    "This records an internal transfer against the card liability. It does not send a payment to the issuer.",
                    color = AppTheme.colors.textMutedLavender,
                    fontSize = 12.sp
                )
                CreditValueRow(
                    "Outstanding balance",
                    wallet.outstandingBalance ?: 0.0,
                    currencySymbol,
                    DangerRed
                )

                Text("Source account", color = AppTheme.colors.textMutedLavender, fontSize = 12.sp)
                Box {
                    OutlinedButton(
                        onClick = { sourceMenuExpanded = true },
                        enabled = sourceWallets.isNotEmpty() && !isSaving,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            selectedSource?.let { "${it.bankName} · ${formatCurrency(it.balance, currencySymbol)}" }
                                ?: "No cash account available",
                            color = AppTheme.colors.textCrispWhite
                        )
                    }
                    DropdownMenu(expanded = sourceMenuExpanded, onDismissRequest = { sourceMenuExpanded = false }) {
                        sourceWallets.forEach { source ->
                            DropdownMenuItem(
                                text = { Text("${source.bankName} · ${formatCurrency(source.balance, currencySymbol)}") },
                                onClick = {
                                    selectedSourceId = source.id
                                    idempotencyKey = UUID.randomUUID().toString()
                                    sourceMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = {
                        amountInput = it
                        idempotencyKey = UUID.randomUUID().toString()
                        error = null
                    },
                    label = { Text("Payment amount ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Apply to statement (optional)", color = AppTheme.colors.textMutedLavender, fontSize = 12.sp)
                Box {
                    OutlinedButton(
                        onClick = { statementMenuExpanded = true },
                        enabled = !isSaving,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val selected = statements.firstOrNull { it.id == selectedStatementId }
                        Text(
                            selected?.let { "${it.periodStart} – ${it.periodEnd}" } ?: "Current card balance",
                            color = AppTheme.colors.textCrispWhite
                        )
                    }
                    DropdownMenu(expanded = statementMenuExpanded, onDismissRequest = { statementMenuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Current card balance") },
                            onClick = {
                                selectedStatementId = null
                                idempotencyKey = UUID.randomUUID().toString()
                                statementMenuExpanded = false
                            }
                        )
                        statements.forEach { statement ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "${statement.periodStart} – ${statement.periodEnd} · due ${statement.dueDate}"
                                    )
                                },
                                onClick = {
                                    selectedStatementId = statement.id
                                    idempotencyKey = UUID.randomUUID().toString()
                                    statementMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                selectedStatementId?.let { id ->
                    statements.firstOrNull { it.id == id }?.let { statement ->
                        CreditValueRow("Statement balance", statement.statementBalance, currencySymbol)
                        Text(
                            statement.minimumPaymentAmount?.let {
                                "Issuer minimum: ${formatCurrency(it, currencySymbol)}"
                            } ?: "Issuer minimum unavailable; use the issuer's statement for guidance.",
                            color = AppTheme.colors.textMutedLavender,
                            fontSize = 11.sp
                        )
                    }
                }
                error?.let { Text(it, color = DangerRed, fontSize = 12.sp) }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Cancel") }
                    Button(
                        onClick = {
                            val amount = parsedAmount
                            if (selectedSource == null) {
                                error = "Select a cash account to pay from."
                            } else if (amount == null || !amount.isFinite() || amount <= 0.0) {
                                error = "Enter a valid amount greater than zero."
                            } else if (amount > selectedSource.balance) {
                                error = "The payment exceeds the selected account balance."
                            } else {
                                onSave(selectedSource.id, amount, selectedStatementId, idempotencyKey)
                            }
                        },
                        enabled = !isSaving,
                        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.primaryAccent)
                    ) {
                        Text(if (isSaving) "Recording..." else "Confirm payment", color = AppTheme.colors.onPrimaryAccent)
                    }
                }
            }
        }
    }
}

@Composable
private fun CreditCardRefundDialog(
    purchase: com.example.network.CreditActivityItem,
    alreadyRefunded: Double,
    currencySymbol: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (amount: Double, idempotencyKey: String) -> Unit
) {
    var amountInput by remember(purchase.id) { mutableStateOf("") }
    var error by remember(purchase.id) { mutableStateOf<String?>(null) }
    var idempotencyKey by remember(purchase.id) { mutableStateOf(UUID.randomUUID().toString()) }
    val refundableAmount = (purchase.amount - alreadyRefunded).coerceAtLeast(0.0)
    val parsedAmount = amountInput.trim().toDoubleOrNull()

    androidx.compose.ui.window.Dialog(onDismissRequest = { if (!isSaving) onDismiss() }) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface)
        ) {
            Column(
                Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Record purchase refund", color = AppTheme.colors.textCrispWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    purchase.description?.ifBlank { null } ?: purchase.category.orEmpty(),
                    color = AppTheme.colors.textMutedLavender,
                    fontSize = 12.sp
                )
                CreditValueRow("Original purchase", purchase.amount, currencySymbol)
                CreditValueRow("Already refunded", alreadyRefunded, currencySymbol)
                CreditValueRow("Remaining refundable", refundableAmount, currencySymbol)
                Text(
                    "A refund reduces recorded expenses and card liability. If the card has already been paid, it may create a card credit balance.",
                    color = AppTheme.colors.textMutedLavender,
                    fontSize = 11.sp
                )
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = {
                        amountInput = it
                        idempotencyKey = UUID.randomUUID().toString()
                        error = null
                    },
                    label = { Text("Refund amount ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let { Text(it, color = DangerRed, fontSize = 12.sp) }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Cancel") }
                    Button(
                        onClick = {
                            when {
                                parsedAmount == null || !parsedAmount.isFinite() || parsedAmount <= 0.0 ->
                                    error = "Enter a valid refund amount greater than zero."
                                parsedAmount > refundableAmount ->
                                    error = "Refund amount exceeds the unrefunded purchase amount."
                                else -> onSave(parsedAmount, idempotencyKey)
                            }
                        },
                        enabled = !isSaving && refundableAmount > 0.0,
                        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.primaryAccent)
                    ) {
                        Text(if (isSaving) "Recording..." else "Confirm refund", color = AppTheme.colors.onPrimaryAccent)
                    }
                }
            }
        }
    }
}

@Composable
private fun CreditCardTermsDialog(
    wallet: WalletEntity,
    onDismiss: () -> Unit,
    onSave: (WalletEntity, (Boolean) -> Unit) -> Unit
) {
    var cardNameInput by remember(wallet.id) { mutableStateOf(wallet.cardName) }
    var issuerInput by remember(wallet.id) { mutableStateOf(wallet.bankName) }
    var networkInput by remember(wallet.id) { mutableStateOf(wallet.cardBrand) }
    var limitInput by remember(wallet.id) { mutableStateOf(wallet.creditLimit?.toString().orEmpty()) }
    var outstandingInput by remember(wallet.id) { mutableStateOf(wallet.outstandingBalance?.toString().orEmpty()) }
    var closingDayInput by remember(wallet.id) { mutableStateOf(wallet.statementClosingDay?.toString().orEmpty()) }
    var dueDayInput by remember(wallet.id) { mutableStateOf(wallet.paymentDueDay?.toString().orEmpty()) }
    var minimumInput by remember(wallet.id) { mutableStateOf(wallet.minimumPaymentAmount?.toString().orEmpty()) }
    var isSaving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    androidx.compose.ui.window.Dialog(onDismissRequest = { if (!isSaving) onDismiss() }) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface)
        ) {
            Column(
                Modifier.fillMaxWidth().verticalScroll(androidx.compose.foundation.rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Credit card settings", color = AppTheme.colors.textCrispWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("${wallet.bankName} · ${wallet.cardName.ifBlank { wallet.cardBrand }}", color = AppTheme.colors.textMutedLavender, fontSize = 12.sp)
                Text("Enter the issuer-provided limit and current debt. These are account terms, not display-only card balances.", color = AppTheme.colors.textMutedLavender, fontSize = 11.sp)
                CreditTermInput("Card name", cardNameInput, { cardNameInput = it }, KeyboardType.Text)
                CreditTermInput("Issuer", issuerInput, { issuerInput = it }, KeyboardType.Text)
                CreditTermInput("Network (Visa, Mastercard, etc.)", networkInput, { networkInput = it }, KeyboardType.Text)
                CreditTermInput("Credit limit", limitInput, { limitInput = it }, KeyboardType.Decimal)
                CreditTermInput(
                    "Opening outstanding balance",
                    outstandingInput,
                    { outstandingInput = it },
                    KeyboardType.Decimal,
                    enabled = !wallet.creditTermsConfigured
                )
                if (wallet.creditTermsConfigured) {
                    Text(
                        "Outstanding balance changes only after confirmed purchases or repayments.",
                        color = AppTheme.colors.textMutedLavender,
                        fontSize = 11.sp
                    )
                }
                CreditTermInput("Statement closing day (1–31)", closingDayInput, { closingDayInput = it }, KeyboardType.Number)
                CreditTermInput("Payment due day (1–31)", dueDayInput, { dueDayInput = it }, KeyboardType.Number)
                CreditTermInput("Issuer minimum amount due (optional)", minimumInput, { minimumInput = it }, KeyboardType.Decimal)
                val cycleTimezone = if (wallet.creditTermsConfigured) {
                    wallet.timezone
                } else {
                    java.util.TimeZone.getDefault().id
                }
                Text("Cycle timezone: $cycleTimezone", color = AppTheme.colors.textMutedLavender, fontSize = 11.sp)
                if (error != null) Text(error!!, color = DangerRed, fontSize = 12.sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Cancel") }
                    Button(
                        onClick = {
                            val limit = limitInput.toDoubleOrNull()
                            val outstanding = outstandingInput.toDoubleOrNull()
                            val closingDay = closingDayInput.toIntOrNull()
                            val dueDay = dueDayInput.toIntOrNull()
                            val minimum = minimumInput.takeIf(String::isNotBlank)?.toDoubleOrNull()
                            error = when {
                                cardNameInput.isBlank() -> "Enter a name for this card."
                                issuerInput.isBlank() -> "Enter the card issuer."
                                networkInput.isBlank() -> "Enter the card network."
                                limit == null || !limit.isFinite() || limit <= 0 -> "Enter a positive credit limit."
                                outstanding == null || !outstanding.isFinite() || outstanding < 0 -> "Enter a valid non-negative balance."
                                outstanding > limit -> "Outstanding balance cannot exceed the configured limit."
                                closingDay !in 1..31 -> "Statement closing day must be between 1 and 31."
                                dueDay !in 1..31 -> "Payment due day must be between 1 and 31."
                                minimumInput.isNotBlank() && (minimum == null || !minimum.isFinite() || minimum <= 0 || minimum > outstanding) ->
                                    "Enter a valid issuer minimum amount no greater than the outstanding balance."
                                else -> null
                            }
                            if (error == null) {
                                isSaving = true
                                onSave(
                                    wallet.copy(
                                        bankName = issuerInput.trim().ifBlank { wallet.bankName },
                                        cardBrand = networkInput.trim().ifBlank { wallet.cardBrand },
                                        cardName = cardNameInput.trim().ifBlank { wallet.cardName },
                                        creditLimit = limit,
                                        outstandingBalance = outstanding,
                                        statementClosingDay = closingDay,
                                        paymentDueDay = dueDay,
                                        minimumPaymentAmount = minimum,
                                        creditTermsConfigured = true,
                                        timezone = cycleTimezone
                                    )
                                ) { saved ->
                                    isSaving = false
                                    if (!saved) error = "Could not save credit settings. Please retry."
                                }
                            }
                        },
                        enabled = !isSaving
                    ) {
                        Text(if (isSaving) "Saving…" else "Save settings")
                    }
                }
            }
        }
    }
}

@Composable
private fun CreditTermInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth()
    )
}

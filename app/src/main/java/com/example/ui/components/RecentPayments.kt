package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TransactionEntity
import com.example.model.WalletEntity
import com.example.network.formatDisplayDate
import com.example.ui.theme.AppTheme
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SuccessGreen

fun getCategoryIcon(category: String): ImageVector {
    return when (category.lowercase()) {
        "salary", "income" -> Icons.Default.Work
        "groceries" -> Icons.Default.ShoppingCart
        "food & dining", "food", "dining" -> Icons.Default.Fastfood
        "transport", "commute", "travel" -> Icons.Outlined.DirectionsCar
        "subscriptions", "entertainment", "netflix" -> Icons.Default.Subscriptions
        "shopping" -> Icons.Default.ShoppingCart
        "health", "medical" -> Icons.Outlined.MedicalServices
        "bills", "utilities" -> Icons.Default.Receipt
        else -> Icons.Default.LocalAtm
    }
}

fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "salary", "income" -> Color(0xFF10B981)
        "groceries" -> Color(0xFFF59E0B)
        "food & dining", "food" -> Color(0xFFEF4444)
        "transport" -> Color(0xFF3B82F6)
        "subscriptions" -> Color(0xFF8B5CF6)
        "shopping" -> Color(0xFFEC4899)
        "health" -> Color(0xFF06B6D4)
        "bills" -> Color(0xFF64748B)
        else -> Color(0xFF7C3AED)
    }
}

@Composable
fun RecentPayments(
    transactions: List<TransactionEntity>,
    wallets: List<WalletEntity>,
    currencySymbol: String,
    onViewAllClick: () -> Unit,
    onTransactionClick: (TransactionEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.surfaceBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Payment History",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textCrispWhite,
                    letterSpacing = (-0.3).sp
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onViewAllClick)
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .testTag("view_all_transactions_btn"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "View All",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.brandColor
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = colors.brandColor,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = colors.surfaceVariant,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.LocalAtm,
                                    contentDescription = null,
                                    tint = colors.textMutedLavender,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No transactions yet",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = colors.textCrispWhite
                        )
                        Text(
                            text = "Your recent spending will appear here",
                            fontSize = 12.sp,
                            color = colors.textMutedLavender
                        )
                    }
                }
            } else {
                val grouped = groupTransactionsByDate(transactions.take(6))

                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    listOf("Today", "Yesterday", "Earlier").forEach { groupKey ->
                        val listInGroup = grouped[groupKey]
                        if (!listInGroup.isNullOrEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = groupKey,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textMutedLavender,
                                    letterSpacing = 0.5.sp
                                )

                                listInGroup.forEachIndexed { idx, tx ->
                                    TransactionItemRow(
                                        transaction = tx,
                                        walletName = wallets.find { it.id == tx.walletId }?.bankName ?: "",
                                        currencySymbol = currencySymbol,
                                        onClick = { onTransactionClick(tx) }
                                    )
                                    if (idx < listInGroup.size - 1) {
                                        HorizontalDivider(
                                            color = colors.surfaceBorder.copy(alpha = 0.5f),
                                            thickness = 0.8.dp,
                                            modifier = Modifier.padding(start = 54.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun groupTransactionsByDate(transactions: List<TransactionEntity>): Map<String, List<TransactionEntity>> {
    val cal = java.util.Calendar.getInstance()
    val todayYear = cal.get(java.util.Calendar.YEAR)
    val todayDay = cal.get(java.util.Calendar.DAY_OF_YEAR)

    cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
    val yesterdayYear = cal.get(java.util.Calendar.YEAR)
    val yesterdayDay = cal.get(java.util.Calendar.DAY_OF_YEAR)

    return transactions.groupBy { tx ->
        val txCal = java.util.Calendar.getInstance().apply { timeInMillis = tx.timestamp }
        val txYear = txCal.get(java.util.Calendar.YEAR)
        val txDay = txCal.get(java.util.Calendar.DAY_OF_YEAR)

        when {
            txYear == todayYear && txDay == todayDay -> "Today"
            txYear == yesterdayYear && txDay == yesterdayDay -> "Yesterday"
            else -> "Earlier"
        }
    }
}

@Composable
fun TransactionItemRow(
    transaction: TransactionEntity,
    walletName: String,
    currencySymbol: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val isRefund = transaction.transactionKind.equals("refund", ignoreCase = true)
    val isExpense = transaction.type.equals("expense", ignoreCase = true)
    val catColor = getCategoryColor(transaction.category)
    val catIcon = getCategoryIcon(transaction.category)

    val title = when {
        transaction.description.isNotBlank() -> transaction.description
        else -> transaction.category.ifBlank { "Transaction" }
    }

    val subtitle = when {
        transaction.description.isNotBlank() && transaction.category.isNotBlank() -> "${transaction.category} • ${formatDisplayDate(transaction.date)}"
        else -> formatDisplayDate(transaction.date).ifBlank { "Payment" }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(catColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = catIcon,
                    contentDescription = transaction.category,
                    tint = catColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colors.textCrispWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (transaction.isRecurring) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = "Recurring",
                            tint = colors.brandColor,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = colors.textMutedLavender,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (walletName.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = colors.surfaceVariant,
                            border = BorderStroke(0.5.dp, colors.surfaceBorder)
                        ) {
                            Text(
                                text = walletName,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textMutedLavender,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        Text(
            text = "${if (isRefund) "+" else if (isExpense) "-" else "+"}${formatCurrency(kotlin.math.abs(transaction.amount), currencySymbol)}",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = if (isExpense && !isRefund) DangerRed else SuccessGreen,
            letterSpacing = (-0.2).sp
        )
    }
}

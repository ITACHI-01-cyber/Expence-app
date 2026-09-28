package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.SavingsGoalEntity
import com.example.model.TransactionEntity
import com.example.model.WalletEntity
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SuccessGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── 1. Add / Edit Transaction Dialog ──
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditTransactionDialog(
    initialTransaction: TransactionEntity? = null,
    wallets: List<WalletEntity>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (type: String, amount: Double, category: String, description: String, date: String, isRecurring: Boolean, walletId: String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var type by remember { mutableStateOf(initialTransaction?.type ?: "expense") }
    var amountStr by remember { mutableStateOf(if (initialTransaction != null) initialTransaction.amount.toString() else "") }
    var category by remember { mutableStateOf(initialTransaction?.category ?: "Groceries") }
    var customCategory by remember { mutableStateOf("") }
    var description by remember { mutableStateOf(initialTransaction?.description ?: "") }
    var isRecurring by remember { mutableStateOf(initialTransaction?.isRecurring ?: false) }
    var selectedWalletId by remember { mutableStateOf(initialTransaction?.walletId ?: wallets.firstOrNull()?.id ?: "") }
    var walletDropdownExpanded by remember { mutableStateOf(false) }

    val defaultCategories = if (type == "expense") {
        listOf("Groceries", "Food & Dining", "Transport", "Subscriptions", "Shopping", "Bills", "Health", "Entertainment", "Other")
    } else {
        listOf("Salary", "Investment", "Freelance", "Gift", "Refund", "Other")
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialTransaction != null) "Edit Transaction" else "Add Transaction",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Expense / Income Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp)
                ) {
                    Surface(
                        onClick = { type = "expense"; if (category == "Salary") category = "Groceries" },
                        shape = RoundedCornerShape(10.dp),
                        color = if (type == "expense") DangerRed else Color.Transparent,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Expense",
                            color = if (type == "expense") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .align(Alignment.CenterVertically),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    Surface(
                        onClick = { type = "income"; if (category == "Groceries") category = "Salary" },
                        shape = RoundedCornerShape(10.dp),
                        color = if (type == "income") SuccessGreen else Color.Transparent,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Income",
                            color = if (type == "income") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .align(Alignment.CenterVertically),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Amount Field
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tx_dialog_amount_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Wallet / Account Picker
                if (wallets.isNotEmpty()) {
                    val currentWallet = wallets.find { it.id == selectedWalletId } ?: wallets.first()
                    Text(
                        text = "Linked Wallet / Account",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            onClick = { walletDropdownExpanded = true },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${currentWallet.bankName} (${currentWallet.cardType.uppercase()}) - ${formatCurrency(currentWallet.balance, currencySymbol)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }

                        DropdownMenu(
                            expanded = walletDropdownExpanded,
                            onDismissRequest = { walletDropdownExpanded = false }
                        ) {
                            wallets.forEach { w ->
                                DropdownMenuItem(
                                    text = {
                                        Text("${w.bankName} (${w.cardType.uppercase()}) - ${formatCurrency(w.balance, currencySymbol)}")
                                    },
                                    onClick = {
                                        selectedWalletId = w.id
                                        walletDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Category Selection Chips
                Text(
                    text = "Category",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    defaultCategories.forEach { cat ->
                        val isSelected = category == cat
                        Surface(
                            onClick = { category = cat },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = cat,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Description Field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Merchant") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Recurring toggle (Monthly Bill / Subscription)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Recurring Monthly",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Mark as monthly bill or subscription",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = isRecurring,
                        onCheckedChange = { isRecurring = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (onDelete != null) {
                        OutlinedButton(
                            onClick = onDelete,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                            modifier = Modifier.testTag("tx_delete_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }

                    Button(
                        onClick = {
                            val amt = amountStr.toDoubleOrNull() ?: 0.0
                            if (amt > 0) {
                                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                val dStr = initialTransaction?.date ?: dateFormat.format(Date())
                                onSave(type, amt, category, description, dStr, isRecurring, selectedWalletId)
                                onDismiss()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tx_save_btn")
                    ) {
                        Text(
                            text = if (initialTransaction != null) "Update Transaction" else "Save Transaction",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ── 2. Add / Edit Wallet Dialog ──
@Composable
fun AddEditWalletDialog(
    initialWallet: WalletEntity? = null,
    onDismiss: () -> Unit,
    onSave: (bankName: String, cardType: String, cardBrand: String, cardNumber: String, cardHolderName: String, expiryDate: String, balance: Double, designId: String, primaryColor: String, secondaryColor: String) -> Unit
) {
    var bankName by remember { mutableStateOf(initialWallet?.bankName ?: "HDFC Bank") }
    var cardType by remember { mutableStateOf(initialWallet?.cardType ?: "debit") }
    var cardBrand by remember { mutableStateOf(initialWallet?.cardBrand ?: "Visa") }
    var cardNumber by remember { mutableStateOf(initialWallet?.cardNumber ?: "") }
    var cardHolderName by remember { mutableStateOf(initialWallet?.cardHolderName ?: "Guest User") }
    var expiryDate by remember { mutableStateOf(initialWallet?.expiryDate ?: "12/28") }
    var balanceStr by remember { mutableStateOf(initialWallet?.balance?.toString() ?: "5000") }
    var designId by remember { mutableStateOf(initialWallet?.designId ?: "midnight") }

    val themes = listOf(
        "midnight" to "Midnight",
        "sunset" to "Sunset",
        "aurora" to "Aurora",
        "berry" to "Berry",
        "ocean-gradient" to "Ocean",
        "mint" to "Mint"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialWallet != null) "Edit Wallet" else "Add New Wallet / Card",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Card Type selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(3.dp)
                ) {
                    listOf("debit" to "Debit", "credit" to "Credit", "upi" to "UPI", "cash" to "Cash").forEach { (type, label) ->
                        val isSelected = cardType == type
                        Surface(
                            onClick = { cardType = type },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .padding(vertical = 6.dp)
                                    .align(Alignment.CenterVertically),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("Bank Name or Wallet (e.g. HDFC, Paytm)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { cardNumber = it },
                    label = { Text(if (cardType == "upi") "UPI ID (e.g. user@paytm)" else "Card Number / Last 4 Digits") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = cardHolderName,
                        onValueChange = { cardHolderName = it },
                        label = { Text("Cardholder Name") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    if (cardType != "upi" && cardType != "cash") {
                        OutlinedTextField(
                            value = expiryDate,
                            onValueChange = { expiryDate = it },
                            label = { Text("Expiry (MM/YY)") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.width(100.dp),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = balanceStr,
                    onValueChange = { balanceStr = it },
                    label = { Text("Initial Balance") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Card Theme Color Picker
                Text(
                    text = "Card Visual Theme",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    themes.forEach { (tid, tname) ->
                        val isSel = designId == tid
                        Surface(
                            onClick = { designId = tid },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = tname,
                                fontSize = 10.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .padding(vertical = 6.dp)
                                    .align(Alignment.CenterVertically),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val bal = balanceStr.toDoubleOrNull() ?: 0.0
                        onSave(bankName, cardType, cardBrand, cardNumber, cardHolderName, expiryDate, bal, designId, "#1A1A2E", "#16213E")
                        onDismiss()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_wallet_btn")
                ) {
                    Text(text = "Save Wallet / Card", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ── 3. Top Up Balance Dialog ──
@Composable
fun TopUpBalanceDialog(
    wallet: WalletEntity,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double) -> Unit
) {
    var amountStr by remember { mutableStateOf("1000") }
    val quickAmounts = listOf(500.0, 1000.0, 2000.0, 5000.0)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Top Up Balance",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Add funds to ${wallet.bankName} (Current: ${formatCurrency(wallet.balance, currencySymbol)})",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Amount Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickAmounts.forEach { qa ->
                        Surface(
                            onClick = { amountStr = qa.toInt().toString() },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "+$currencySymbol${qa.toInt()}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .align(Alignment.CenterVertically),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Top Up Amount ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val amt = amountStr.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            onConfirm(amt)
                            onDismiss()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("confirm_top_up_btn")
                ) {
                    Text(text = "Add to Balance", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ── 4. Edit Budget Dialog ──
@Composable
fun EditBudgetDialog(
    currentIncome: Double,
    currentLimit: Double,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (income: Double, limit: Double) -> Unit
) {
    var incomeStr by remember { mutableStateOf(currentIncome.toString()) }
    var limitStr by remember { mutableStateOf(currentLimit.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Monthly Budget Limits",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = incomeStr,
                    onValueChange = { incomeStr = it },
                    label = { Text("Monthly Expected Income ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = limitStr,
                    onValueChange = { limitStr = it },
                    label = { Text("Monthly Budget Spending Limit ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val inc = incomeStr.toDoubleOrNull() ?: currentIncome
                        val lim = limitStr.toDoubleOrNull() ?: currentLimit
                        onSave(inc, lim)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_budget_limits_btn")
                ) {
                    Text(text = "Save Budget Limits", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ── 5. Add / Edit Savings Goal Dialog ──
@Composable
fun AddEditGoalDialog(
    initialGoal: SavingsGoalEntity? = null,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (title: String, amount: Double, medium: String) -> Unit
) {
    var title by remember { mutableStateOf(initialGoal?.title ?: "") }
    var amountStr by remember { mutableStateOf(initialGoal?.amount?.toString() ?: "") }
    var medium by remember { mutableStateOf(initialGoal?.medium ?: "Bank Account") }

    val defaultMediums = listOf("Bank Account", "Groww App", "Zerodha", "Fixed Deposit", "Cash")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialGoal != null) "Edit Savings Goal" else "New Savings Goal",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Title (e.g. Emergency Fund, Laptop)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Target Amount ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Saving Vehicle / Medium",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    defaultMediums.take(3).forEach { med ->
                        val isSel = medium == med
                        Surface(
                            onClick = { medium = med },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = med,
                                fontSize = 10.5.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .padding(vertical = 6.dp)
                                    .align(Alignment.CenterVertically),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val amt = amountStr.toDoubleOrNull() ?: 0.0
                        if (title.isNotBlank() && amt > 0) {
                            onSave(title, amt, medium)
                            onDismiss()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_goal_btn")
                ) {
                    Text(text = "Save Goal", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

package projeto_taes.projeto.transactions

import android.app.DatePickerDialog
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import projeto_taes.projeto.App
import java.text.SimpleDateFormat
import java.util.*



@Composable
fun TransationHistoryScreen(app: App) {
    // State to store transaction history
    var transactions by remember { mutableStateOf<List<TransactionEntry>>(emptyList()) }
    var filteredTransactions by remember { mutableStateOf<List<TransactionEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // State for date filter inputs
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    // Date formatter
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Fetch transaction history when the composable is displayed
    LaunchedEffect(Unit) {
        isLoading = true
        app.fetchTransactionHistory { exception ->
            if (exception != null) {
                isLoading = false
                errorMessage = exception.message
            } else {
                isLoading = false
                transactions = app.transactions
                filteredTransactions = app.transactions
                Log.d("TransactionHistory", "Fetched transactions: $transactions") // Debug log
            }
        }
    }

    // Function to filter transactions
    fun filterTransactions() {
        filteredTransactions = transactions.filter { transaction ->
            val transactionDate = dateFormat.parse(transaction.date)
            val start = if (startDate.isNotEmpty()) dateFormat.parse(startDate) else null
            val end = if (endDate.isNotEmpty()) dateFormat.parse(endDate) else null
            (start == null || transactionDate.after(start) || transactionDate == start) &&
                    (end == null || transactionDate.before(end) || transactionDate == end)
        }
    }

    // Dynamic title logic
    val title = if (startDate.isNotEmpty() || endDate.isNotEmpty()) {
        "Transactions from ${startDate.ifEmpty { "Start" }} to ${endDate.ifEmpty { "End" }}"
    } else {
        "Transaction History"
    }

    // Date picker dialogs
    val calendar = Calendar.getInstance()
    val startDatePickerDialog = DatePickerDialog(
        LocalContext.current,
        { _, year, month, dayOfMonth ->
            startDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val endDatePickerDialog = DatePickerDialog(
        LocalContext.current,
        { _, year, month, dayOfMonth ->
            endDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else if (errorMessage != null) {
            Text(
                text = "Error: $errorMessage",
                color = MaterialTheme.colorScheme.error,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        } else {
            // Dynamic Title
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Date filter inputs with buttons for DatePicker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Start Date: $startDate", fontSize = 16.sp)
                    Button(onClick = { startDatePickerDialog.show() }) {
                        Text("Select Start Date")
                    }
                }
                Column(
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "End Date: $endDate", fontSize = 16.sp)
                    Button(onClick = { endDatePickerDialog.show() }) {
                        Text("Select End Date")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filter button
            Button(onClick = { filterTransactions() }) {
                Text("Filter")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Transaction list
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(filteredTransactions) { transaction ->
                    TransactionCard(transaction)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}


@Composable
fun TransactionCard(transaction: TransactionEntry) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Date: ${transaction.date}")
            Text(text = "Description: ${transaction.description}")
            Text(text = "Value: ${transaction.value} coins")
        }
    }
}

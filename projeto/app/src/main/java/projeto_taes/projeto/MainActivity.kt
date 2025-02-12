package projeto_taes.projeto

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import projeto_taes.projeto.history.GameHistoryEntry
import projeto_taes.projeto.history.GameHistoryManager
import projeto_taes.projeto.history.GameHistoryScreen
import projeto_taes.projeto.notifications.NotificationHelper
import projeto_taes.projeto.notifications.NotificationIcon
import projeto_taes.projeto.notifications.NotificationList
import projeto_taes.projeto.scoreboards.GlobalScoreScreen
import projeto_taes.projeto.scoreboards.PersonalScoreScreen
import projeto_taes.projeto.scoreboards.Score
import projeto_taes.projeto.scoreboards.ScoreManager
import projeto_taes.projeto.scoreboards.ScoreboardScreen
import projeto_taes.projeto.ui.BoardSelectionScreen
import projeto_taes.projeto.ui.DashboardScreen
import projeto_taes.projeto.ui.GameScreen
import projeto_taes.projeto.ui.GameScreenTest
import projeto_taes.projeto.ui.LoginScreen
import projeto_taes.projeto.transactions.TransationHistoryScreen
import projeto_taes.projeto.ui.WalletScreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("Permissions", "Permission granted")
            } else {
                Log.d("Permissions", "Permission denied")
            }
        }

        if (!App.TESTING) {
            requestPostNotificationPermission()
        }

        setContent {
            MyApp(this)
        }
    }

    private fun requestPostNotificationPermission() {
        when {
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                Log.d("Permissions", "Permission already granted")
            }
            else -> {
                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApp(context: MainActivity) {
    val navController = rememberNavController()
    val app = App(context, navController)
    val showNotificationDialog = remember { mutableStateOf(false) }
    val notificationHelper = NotificationHelper(context)
    val showErrorDialog = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { AppBarBackArrow(navController) },
                title = { AppBarTitle(navController) },
                actions = { NotificationIcon { showNotificationDialog.value = true } }
            )
        },
        content = { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "login",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("login") { LoginScreen(app) }
                composable("dashboard") { DashboardScreen(app) }
                composable("boardSelection") { BoardSelectionScreen(app) }
                composable("newGame/{boardSize}") { backStackEntry ->
                    val boardSize = backStackEntry.arguments?.getString("boardSize") ?: "3x4"

                    // Select the game based on if is test or production
                    val gameScreen: @Composable (NavController, String, (Int, Int) -> Unit) -> Unit =
                        if (App.TESTING)
                            { navController, boardSize, onGameFinish ->
                                GameScreenTest(navController, boardSize, onGameFinish)
                            }
                        else
                            { navController, boardSize, onGameFinish ->
                                GameScreen(navController, boardSize, onGameFinish,app)
                            }

                    gameScreen(navController, boardSize) { moves, time ->
                        if (app.isUserLoggedIn()) {
                            var newScore = Score(moves, time)
                            val oldScore = app.scoreManager.getPersonalScore(boardSize)
                            Log.e("ScoreDebug", "New Score: time=${newScore.time}, moves=${newScore.moves}")
                            Log.e("ScoreDebug", "Old Score: time=${oldScore.time}, moves=${oldScore.moves}")
                            if (newScore.isBetterThan(oldScore)) {
                                notificationHelper.sendNotification(
                                    "New High Score!",
                                    "Congratulations! New high score with $moves moves in $time seconds on the $boardSize board.",
                                    system = true
                                )

                                // send reward notification when user beats personal score
                                notificationHelper.sendNotification(
                                    "You Received A Reward!",
                                    "Congratulations! You received 1 coin for beating your personal record on the $boardSize board.",
                                    system = true
                                )

                                app.giveReward(1, "Reward for beating your personal record on the $boardSize board.") { exception ->
                                    if (exception != null) {
                                        Log.e("RewardError", "Failed to reward coin: ${exception.message}")
                                    } else {
                                        Log.d("Reward", "Reward granted and transaction logged successfully.")
                                    }
                                }


                                newScore = Score.bestOfBoth(newScore, oldScore)
                                app.scoreManager.updatePersonalScore(boardSize, newScore) { exception ->
                                    if (exception != null) {
                                        errorMessage.value = "Failed to update personal score: ${exception.message}"
                                        showErrorDialog.value = true
                                    }
                                }
                                app.scoreManager.updateGlobalScore(app.username, boardSize, newScore) { exception ->
                                    if (exception != null) {
                                        errorMessage.value = "Failed to update global scores: ${exception.message}"
                                        showErrorDialog.value = true
                                    }
                                }
                            }
                            if (app.scoreManager.isTopThreeGlobalScore(boardSize, newScore)) {
                                notificationHelper.sendNotification(
                                    "You Received A Reward!",
                                    "Congratulations! You received 1 coin for beating top 3 records on the $boardSize board.",
                                    system = true
                                )
                                // add code for coin reward when we have backend
                                app.giveReward(1, "Reward for top 3 records on the $boardSize board.") { exception ->
                                    if (exception != null) {
                                        Log.e("RewardError", "Failed to reward coin: ${exception.message}")
                                    } else {
                                        Log.d("Reward", "Reward granted and transaction logged successfully.")
                                    }
                                }

                            }
                            app.historyManager.addGameToHistory(GameHistoryEntry(getCurrentDateFormatted(), boardSize, moves, time)) // Add game to local history

                            // Now use lifecycleScope directly from the MainActivity
                            context.lifecycleScope.launch {
                                try {
                                    app.historyManager.pushUpdatesToServer() // Sync local history with Firestore
                                } catch (e: Exception) {
                                    Log.e("GameHistory", "Failed to sync history: ${e.message}")
                                }
                            }
                        }
                    }
                }
                composable("wallet") { WalletScreen(app) }
                composable("transationHistory") { TransationHistoryScreen(app) }
                composable("scoreboards") { ScoreboardScreen(app) }

                composable("personalScore") { PersonalScoreScreen(app.scoreManager) }
                composable("globalScore") { GlobalScoreScreen(app.scoreManager) }
                composable("gameHistory") { GameHistoryScreen(app.historyManager) }
            }

            if (showNotificationDialog.value) {
                NotificationList(
                    notifications = notificationHelper.getNotifications(),
                    isVisible = showNotificationDialog.value,
                    onClose = { showNotificationDialog.value = false }
                )
            }

            if (showErrorDialog.value) {
                AlertDialog(
                    onDismissRequest = { showErrorDialog.value = false },
                    title = { Text("Error") },
                    text = { Text(errorMessage.value) },
                    confirmButton = {
                        TextButton(onClick = { showErrorDialog.value = false }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    )
}

@Composable
fun AppBarTitle(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "Home"
    Text(getTitleForRoute(currentRoute, navController.currentBackStackEntry))
}

@Composable
fun AppBarBackArrow(navController: NavHostController) {
    IconButton(onClick = { navController.navigateUp() }) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
    }
}

fun getTitleForRoute(route: String, backStackEntry: NavBackStackEntry?): String {
    return when {
        route.startsWith("newGame/") && backStackEntry != null -> {
            val boardSize = backStackEntry.arguments?.getString("boardSize") ?: "3x4"
            "Game Screen - $boardSize"
        }
        route == "login" -> "Login"
        route == "dashboard" -> "Dashboard"
        route == "boardSelection" -> "Board Selection"
        route == "wallet" -> "Wallet"
        route == "personalScore" -> "Personal Scoreboard"
        route == "globalScore" -> "Global Scoreboard"
        route == "scoreboards" -> "Scoreboard"
        route == "gameHistory" -> "Game History"
        else -> "Memory Game App"
    }
}

fun getCurrentDateFormatted(): String {
    val currentDate = Date()
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return formatter.format(currentDate)
}

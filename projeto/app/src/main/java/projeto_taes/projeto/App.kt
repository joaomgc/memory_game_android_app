package projeto_taes.projeto

import android.util.Log
import androidx.navigation.NavHostController
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Transaction
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import projeto_taes.projeto.history.GameHistoryEntry
import projeto_taes.projeto.history.GameHistoryManager
import projeto_taes.projeto.scoreboards.Score
import projeto_taes.projeto.scoreboards.ScoreEntry
import projeto_taes.projeto.scoreboards.ScoreManager
import projeto_taes.projeto.transactions.TransactionEntry
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class App(
    val mainActivity: MainActivity,
    val navController: NavHostController
) {
    private var auth: FirebaseAuth = Firebase.auth
    val firestore = Firebase.firestore // Initialize Firestore

    companion object {
        const val TESTING = BuildConfig.IS_TESTING
    }

    val scoreManager = ScoreManager(this)
    val historyManager = GameHistoryManager(this)

    val api = API()

    var user: FirebaseUser? = null
        private set
    var username: String = ""
        private set
    var coins: Int = 0
    var email: String? = null
    var transactions: List<TransactionEntry> = emptyList()

    fun tryLogin(username: String, password: String, onComplete: (Exception?) -> Unit) {
        if (isUserLoggedIn()) {
            onComplete(RuntimeException("Already logged in."))
            return
        }

        if (username.isEmpty() || password.isEmpty()) {
            onComplete(RuntimeException("Username and password must not be empty."))
            return
        }

        api.login(username, password) { exception ->

            // Keep firebase login for now to not break everything
            if (exception != null) {
                onComplete(exception)
                return@login
            }

            auth.signInWithEmailAndPassword("million@mail.pt", "123456")
                .addOnCompleteListener(mainActivity) { task ->
                    if (task.isSuccessful) {
                        // auth.currentUser is not null here
                        user = auth.currentUser
                        // Call the function to check and create the Firestore user document
                        ensureUserDocumentExists(user!!.uid) { exception ->
                            onComplete(exception)
                        }
                        this.username = username
                        //this.username = user!!.email.toString()
                    } else {
                        onComplete(task.exception)
                    }
                }


            // When all fixed, remove above and uncomment below
            //onComplete(exception)
        }
    }

    private fun ensureUserDocumentExists(uid: String, onComplete: (Exception?) -> Unit) {
        val userRef = firestore.collection("users").document(uid)

        userRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                // Create a new user document with default fields
                val newUser = mapOf(
                    "coins" to 0, // Initialize coins to 0
                    "email" to user?.email, // Store the Firebase Auth email
                )
                userRef.set(newUser)
                    .addOnSuccessListener {
                        onComplete(null) // Document created successfully
                    }
                    .addOnFailureListener { exception ->
                        onComplete(exception) // Handle Firestore errors
                    }
            } else {
                onComplete(null) // Document already exists
            }
        }.addOnFailureListener { exception ->
            onComplete(exception) // Handle Firestore errors
        }
    }

    // Function to fetch user data (coins, username, etc.) and store it in the App class
    fun fetchUserData(onComplete: (Exception?) -> Unit) {
        api.get(
            "users/${username}",
            onSuccess = { _, responseJson ->
                val user = responseJson?.optJSONObject("user")
                if(user!=null){
                    coins = user.optInt("coins")
                    Log.d("debug", "Coins loaded from Server: $coins")
                    onComplete(null)
                    return@get
                }
                onComplete(RuntimeException("Failed to fetch user data."))
            },
            onException = { exception ->
                onComplete(exception)
            }
        )
    }

    fun logout() {
        if (user != null) {
            // Clear user-related data
            api.logout()
            user = null
            username = ""  // Clear the cached username
            coins = 0      // Reset coins to initial state
            email = ""     // Clear the email (if applicable)
            scoreManager.resetPersonalScore()
            transactions = emptyList()

            // Sign out from Firebase Auth
            auth.signOut()
        }
    }

    fun isUserLoggedIn(): Boolean {
        //return user != null
        return api.isLoggedIn()
    }

    suspend fun fetchPersonalScores(): Map<String, Map<String, Int>> {
        if (!isUserLoggedIn()) {
            throw RuntimeException("User not logged in.")
        }

        // Fetch the API response asynchronously
        val data = withContext(Dispatchers.IO) {
            fetchApiResponseAsync("users/$username/scores")
        }

        // Parse the scores from the response
        val scores = data?.optJSONObject("scores")
            ?: throw RuntimeException("Invalid or missing scores data.")

        val result = mutableMapOf<String, Map<String, Int>>()

        scores.keys().forEach { board ->
            val entry = scores.optJSONObject(board)

            if (entry != null) {
                val moves = entry.optInt("moves", Score.INITIAL_SCORE)
                val time = entry.optInt("time", Score.INITIAL_SCORE)

                // No need to find minimum values since only one score per board is expected
                if (moves != Score.INITIAL_SCORE && time != Score.INITIAL_SCORE) {
                    result[board] = mapOf(
                        "moves" to moves,
                        "time" to time
                    )
                }
            }
        }

        return result
    }

    suspend fun fetchGlobalScores(): Map<String, MutableList<ScoreEntry>> {
        // Fetch the API response asynchronously
        val data = withContext(Dispatchers.IO) {
            fetchApiResponseAsyncNoAuth("scores/global")
        }

        // Parse the scores from the response
        val scores = data?.optJSONObject("scores")
            ?: throw RuntimeException("Invalid or missing scores data.")

        val result = mutableMapOf<String, MutableList<ScoreEntry>>()

        scores.keys().forEach { board ->
            val entries = scores.optJSONArray(board)

            if (entries != null) {
                val scoreList = mutableListOf<ScoreEntry>()

                for (i in 0 until entries.length()) {
                    val entry = entries.optJSONObject(i)
                    if (entry != null) {
                        val username = entry.optString("username", "unknown")
                        val moves = entry.optInt("moves", Int.MAX_VALUE)
                        val time = entry.optInt("time", Int.MAX_VALUE)

                        scoreList.add(ScoreEntry(username, Score(moves, time)))
                    }
                }

                result[board] = scoreList
            }
        }

        return result
    }

    fun payCoin(onComplete: (Exception?) -> Unit) {
        if (!isUserLoggedIn()) {
            onComplete(RuntimeException("User not logged in."))
            return
        }

        val emptyJson = JSONObject()

        api.post(
            "transactions/payCoin",
            emptyJson,
            onSuccess = { response, responseBody ->
                try {
                    if (response.code == 200 && responseBody != null) {
                        CoroutineScope(Dispatchers.Main).launch {
                            coins -= 1
                            onComplete(null)
                        }
                    } else {
                        onComplete(RuntimeException("Unexpected response: ${response.code}"))
                    }
                } catch (e: JSONException) {
                    CoroutineScope(Dispatchers.Main).launch {
                        Log.e("debug", "Invalid JSON response: ${responseBody.toString()}")
                        onComplete(e)
                    }
                }
            },
            onException = { exception ->
                CoroutineScope(Dispatchers.Main).launch {
                    Log.e("debug", "API call failed: $exception", exception)
                    onComplete(exception)
                }
            }
        )
    }

    private suspend fun fetchApiResponseAsync(endpoint: String): JSONObject? {
        return suspendCancellableCoroutine { continuation ->
            api.get(
                endpoint,
                onSuccess = { _, responseJson ->
                    continuation.resume(responseJson)
                },
                onException = { exception ->
                    continuation.resumeWithException(exception)
                }
            )
        }
    }

    private suspend fun fetchApiResponseAsyncNoAuth(endpoint: String): JSONObject? {
        return suspendCancellableCoroutine { continuation ->
            api.getNoAuth(
                endpoint,
                onSuccess = { _, responseJson ->
                    continuation.resume(responseJson)
                },
                onException = { exception ->
                    continuation.resumeWithException(exception)
                }
            )
        }
    }

    fun incCoinsForCurrentUser(onComplete: (Exception?) -> Unit) {
        doOperationOnCurrentUser(
            operation = { userRef, transaction, snapshot ->
                val currentCoins = snapshot.getLong("coins") ?: 0
                transaction.update(userRef, "coins", currentCoins + 1)
            },
            onComplete = { exception ->
                if (exception == null) {
                    coins++
                }
                onComplete(exception)
            }
        )
    }

    fun decCoinsForCurrentUser(onComplete: (Exception?) -> Unit) {
        doOperationOnCurrentUser(
            operation = { userRef, transaction, snapshot ->
                val currentCoins = snapshot.getLong("coins") ?: 0
                transaction.update(userRef, "coins", currentCoins - 1)
            },
            onComplete = { exception ->
                if (exception == null) {
                    coins--
                }
                onComplete(exception)
            }
        )
    }

    private fun doOperationOnCurrentUser(
        operation: (DocumentReference, Transaction, DocumentSnapshot) -> Unit,
        onComplete: (Exception?) -> Unit
    ) {
        if (user == null) {
            onComplete(RuntimeException("User not logged in."))
            return
        }

        val userRef = firestore.collection("users").document(user!!.uid)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)

            if (!snapshot.exists()) {
                throw RuntimeException("User document does not exist.")
            }

            operation(userRef, transaction, snapshot);
        }.addOnSuccessListener {
            onComplete(null) // Operation completed successfully
        }.addOnFailureListener { exception ->
            onComplete(exception) // Handle Firestore errors
        }
    }

    suspend fun fetchGameHistory(): List<GameHistoryEntry> {
        if (user == null) {
            throw RuntimeException("User not logged in.")
        }

        val userId = user!!.uid
        val userRef = firestore.collection("users").document(userId)

        return try {
            // Fetch the user's document
            val snapshot = userRef.get().await()

            if (snapshot.exists()) {
                // Extract the gameHistory array from the document
                val gameHistoryArray = snapshot.get("gameHistory") as? List<Map<String, Any>>

                // Map the array to a list of GameHistoryEntry objects
                gameHistoryArray?.map { gameEntry ->
                    GameHistoryEntry(
                        date = gameEntry["date"] as String,
                        boardSize = gameEntry["boardSize"] as String,
                        moves = (gameEntry["moves"] as Long).toInt(),
                        timeInSeconds = (gameEntry["timeInSeconds"] as Long).toInt() // Ensure this field name matches
                    )
                } ?: emptyList() // If the array is null, return an empty list
            } else {
                emptyList() // Return an empty list if the document does not exist
            }
        } catch (e: Exception) {
            throw RuntimeException("Error fetching game history: ${e.message}")
        }
    }

    /*
    suspend fun fetchTransactionHistory(): List<TransactionEntry> {
        if (user == null) {
            throw RuntimeException("User not logged in.")
        }

        val userId = user!!.uid
        val userRef = firestore.collection("users").document(userId)

        return try {
            // Fetch the user's document
            val snapshot = userRef.get().await()

            if (snapshot.exists()) {
                // Extract the transactions array from the document
                val transactionsArray = snapshot.get("transactionHistory") as? List<Map<String, Any>>

                // Map the array to a list of TransactionEntry objects
                transactionsArray?.map { transactionEntry ->
                    TransactionEntry(
                        date = transactionEntry["date"] as String,
                        description = transactionEntry["description"] as String,
                        value = (transactionEntry["value"] as Long).toInt() // Ensure "value" is cast correctly
                    )
                } ?: emptyList() // Return an empty list if transactionsArray is null
            } else {
                emptyList() // Return an empty list if the document does not exist
            }
        } catch (e: Exception) {
            throw RuntimeException("Error fetching transaction history: ${e.message}")
        }
    }*/

    fun fetchTransactionHistory(onComplete: (Exception?) -> Unit) {
        api.get(
            "users/${username}",
            onSuccess = { _, responseJson ->
                val user = responseJson?.optJSONObject("user")
                if (user != null) {
                    // Extract transaction history data
                    val transactionHistory = user.optJSONArray("transactionHistory")
                    if (transactionHistory != null) {
                        val transactionsList = mutableListOf<TransactionEntry>()
                        for (i in 0 until transactionHistory.length()) {
                            val transactionJson = transactionHistory.getJSONObject(i)
                            val transaction = TransactionEntry(
                                date = transactionJson.optString("date", ""),
                                description = transactionJson.optString("description", "No description"),
                                value = transactionJson.optInt("value", 0)
                            )
                            transactionsList.add(transaction)
                        }
                        Log.d("debug", "Transaction History loaded: $transactionsList")
                        transactions = transactionsList // Save the fetched transactions to the global variable
                        onComplete(null) // Success
                        return@get
                    } else {
                        Log.d("debug", "No transaction history found.")
                    }
                }
                onComplete(RuntimeException("Failed to fetch user data.")) // Error if user not found
            },
            onException = { exception ->
                onComplete(exception) // Handle exception
            }
        )
    }

    fun savePersonalScore(boardSize: String, newScore: Score, onComplete: (Exception?) -> Unit) {
        if (user == null) {
            onComplete(RuntimeException("User not logged in."))
            return
        }

        // Construct the JSON object for the API request
        val scoreData = JSONObject().apply {
            put("moves", newScore.moves)
            put("time", newScore.time)
            put("board", boardSize)
        }

        // Send the request to the API
        CoroutineScope(Dispatchers.IO).launch {
            try {
                api.post(
                    "users/${username}/scores",
                    scoreData,
                    onSuccess = { _, _ -> onComplete(null) },
                    onException = { exception -> onComplete(exception) }
                )
            } catch (e: Exception) {
                onComplete(e)
            }
        }
    }

    fun saveIndividualGlobalScore(boardSize: String, scoreEntry: ScoreEntry, onComplete: (Exception?) -> Unit) {
        // Convert the score entry to JSON
        val scoreData = JSONObject().apply {
            put("username", scoreEntry.username)
            put("moves", scoreEntry.score.moves)
            put("time", scoreEntry.score.time)
            put("board", boardSize)  // Ensure the score knows its board size
        }

        // Send the score data to the API endpoint
        CoroutineScope(Dispatchers.IO).launch {
            try {
                api.post(
                    "scores/global/",
                    scoreData,
                    onSuccess = { _, _ -> onComplete(null) },
                    onException = { exception -> onComplete(exception) }
                )
            } catch (e: Exception) {
                onComplete(e)
            }
        }
    }

    fun saveGlobalScores(boardSize: String, newScores: List<ScoreEntry>, onComplete: (Exception?) -> Unit) {
        // Create a JSON Array to hold all the scores to be sent to the server
        val scoresData = JSONArray().apply {
            newScores.forEach { scoreEntry ->
                put(JSONObject().apply {
                    put("username", scoreEntry.username)
                    put("moves", scoreEntry.score.moves)
                    put("time", scoreEntry.score.time)
                    put("board", boardSize)  // Ensure each score knows its board size
                })
            }
        }

        // Send the JSON Array to the API endpoint
        CoroutineScope(Dispatchers.IO).launch {
            try {
                api.post(
                    "scores/global",
                    JSONObject().put("scores", scoresData),
                    onSuccess = { _, _ -> onComplete(null) },
                    onException = { exception -> onComplete(exception) }
                )
            } catch (e: Exception) {
                onComplete(e)
            }
        }
    }

    // Function only for testing score system in test mode
    fun saveTestScores(app: App, saveCompleted: CompletableDeferred<Boolean>) {
        if (!app.isUserLoggedIn()) {
            saveCompleted.completeExceptionally(RuntimeException("User not logged in."))
            return
        }

        val boardSizes = listOf("3x4", "4x4", "6x6")
        val testScore = Score(1000, 1000)  // 1000 for testing in katalon the change to other score

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val updateTasks = boardSizes.flatMap { boardSize ->
                    listOf(
                        async {
                            app.api.post(
                                "users/${app.username}/scores",
                                JSONObject().apply {
                                    put("moves", testScore.moves)
                                    put("time", testScore.time)
                                    put("board", boardSize)
                                },
                                onSuccess = { _, _ -> true },
                                onException = { exception -> throw exception }
                            )
                        },
                        async {
                            app.api.post(
                                "scores/global",
                                JSONObject().apply {
                                    put("username", app.username)
                                    put("moves", testScore.moves)
                                    put("time", testScore.time)
                                    put("board", boardSize)
                                },
                                onSuccess = { _, _ -> true },
                                onException = { exception -> throw exception }
                            )
                        }
                    )
                }
                updateTasks.awaitAll()
                saveCompleted.complete(true)
            } catch (e: Exception) {
                saveCompleted.completeExceptionally(e)
            }
        }
    }

    fun giveReward(amount: Int, description: String, onComplete: (Exception?) -> Unit) {
        // Check if user is logged in
        if (!isUserLoggedIn()) {
            onComplete(RuntimeException("User not logged in."))
            return
        }

        // Create the JSON object for the request body
        val rewardRequest = JSONObject().apply {
            put("amount", amount)
            put("description", description)
        }

        // Execute the request asynchronously
        api.post(
            "transactions/giveReward",
            rewardRequest,
            onSuccess = { response, responseJson ->
                coins += amount
                onComplete(null)
            },
            onException = { exception ->
                onComplete(exception)
            }
        )
    }

    fun pushGameHistoryToServer(gameHistory: List<GameHistoryEntry>, onComplete: (Exception?) -> Unit) {
        if (!api.isLoggedIn()) {
            Log.d("debug", "User not logged in.")
            onComplete(RuntimeException("User not logged in"))
            return
        }

        val jsonArray = JSONArray(gameHistory.map { entry ->
            JSONObject().apply {
                put("date", entry.date)
                put("boardSize", entry.boardSize)
                put("moves", entry.moves)
                put("timeInSeconds", entry.timeInSeconds)
            }
        })

        val requestBody = JSONObject().apply {
            put("history", jsonArray)
        }

        api.post(
            "histories/",
            requestBody,
            onSuccess = { _, _ ->
                Log.d("debug", "Game history successfully pushed to the server.")
                onComplete(null)
            },
            onException = { exception ->
                Log.d("debug", exception.toString())
                onComplete(RuntimeException("Error pushing game history: ${exception.message}"))
            }
        )
    }

    fun fetchGameHistoryFromServer(onComplete: (List<GameHistoryEntry>?, Exception?) -> Unit) {
        if (!api.isLoggedIn()) {
            Log.d("debug", "User not logged in.")
            onComplete(null, RuntimeException("User not logged in"))
            return
        }

        api.get(
            "histories/",
            onSuccess = { _, responseJson ->
                Log.d("debug", "Success fetching history. $responseJson")
                try {
                    val json = responseJson ?: throw IOException("Empty response body")
                    val jsonArray = json.getJSONArray("history")
                    val gameHistory = (0 until jsonArray.length()).map { index ->
                        val entry = jsonArray.getJSONObject(index)
                        GameHistoryEntry(
                            date = entry.getString("date"),
                            boardSize = entry.getString("boardSize"),
                            moves = entry.getInt("moves"),
                            timeInSeconds = entry.getInt("timeInSeconds")
                        )
                    }
                    onComplete(gameHistory, null)
                } catch (e: Exception) {
                    onComplete(null, e)
                }
            },
            onException = { exception ->
                Log.d("debug", exception.toString())
                onComplete(null, RuntimeException("Network error: ${exception.message}"))
            }
        )
    }
}

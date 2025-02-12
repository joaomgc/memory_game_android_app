package projeto_taes.projeto

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class API {
    companion object {
        private val JSON: MediaType = "application/json".toMediaType()
        private const val API_URL = "http://10.0.2.2:5000/api/"
    }

    private val httpClient = OkHttpClient()

    var token: String = ""
    private var tokenExpirationTime: Long = 1 * 60 * 60 * 1000 // 1 hour
    private var tokenRefreshJob: Job? = null

    fun login(username: String, password: String, onComplete: (Exception?) -> Unit) {
        if (isLoggedIn()) {
            onComplete(Exception("Already logged in"))
            return
        }

        val loginJson = JSONObject()
            .put("username", username)
            .put("password", password)

        val request = Request.Builder()
            .url(API_URL + "auth/login")
            .post(loginJson.toString().toRequestBody(JSON))
            .build()

        executeRequest(
            request,
            onSuccess = { response, responseData ->
                val token = responseData?.optString("token")
                if (token == null) {
                    onComplete(Exception("The token was not received"))
                    return@executeRequest
                }
                this.token = token
                scheduleTokenRefresh()
                onComplete(null)
            },
            onException = { exception ->
                onComplete(exception)
            }
        )
    }

    fun logout() {
        token = ""
        tokenRefreshJob?.cancel()
    }

    private fun scheduleTokenRefresh() {
        tokenRefreshJob = CoroutineScope(Dispatchers.IO).launch {
            val refreshTime = tokenExpirationTime - (5 * 60 * 1000) // Refresh 5 minutes before expiration
            delay(refreshTime)
            refreshToken()
        }
    }

    private fun refreshToken() {
        post(
            "auth/refresh",
            JSONObject(),
            onSuccess = { response, responseData ->
                val newToken = responseData?.optString("token")
                if (newToken == null) {
                    println("The token was not received")
                    return@post
                }
                this.token = newToken
                println("Token successfully refreshed")

                scheduleTokenRefresh()
            },
            onException = { exception ->
                println("Token refresh failed: ${exception.message}")
            }
        )
    }

    fun get(
        url: String,
        onSuccess: (Response, JSONObject?) -> Unit,
        onException: (Exception) -> Unit
    ) {
        val request = Request.Builder()
            .url(API_URL + url)
            .addHeader("Authorization", "Bearer $token")
            .build()

        executeRequest(request, onSuccess, onException)
    }

    fun getNoAuth(
        url: String,
        onSuccess: (Response, JSONObject?) -> Unit,
        onException: (Exception) -> Unit
    ) {
        val request = Request.Builder()
            .url(API_URL + url)
            .build()

        executeRequest(request, onSuccess, onException)
    }

    fun post(
        url: String,
        json: JSONObject,
        onSuccess: (Response, JSONObject?) -> Unit,
        onException: (Exception) -> Unit
    ) {
        val request = Request.Builder()
            .url(API_URL + url)
            .addHeader("Authorization", "Bearer $token")
            .post(json.toString().toRequestBody(JSON))
            .build()

        executeRequest(request, onSuccess, onException)
    }

    private fun executeRequest(
        request: Request,
        onSuccess: (Response, JSONObject?) -> Unit,
        onException: (Exception) -> Unit
    ) {
        val call = httpClient.newCall(request)
        call.enqueue(object : Callback {
            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val responseJson = try {
                    if (response.body == null) null else JSONObject(response.body!!.string())
                } catch (e: Exception) {
                    onException(e)
                    return
                }

                if (responseJson != null) {
                    val status = responseJson.optString("status")
                    if (status.isNotEmpty()) {
                        when (status) {
                            "OK" -> {
                                val responseData = responseJson.optJSONObject("data")
                                onSuccess(response, responseData)
                                return
                            }
                            "FAILED" -> {
                                val errorMessage = responseJson.optJSONObject("data")?.optString("error")
                                if (!errorMessage.isNullOrEmpty()) {
                                    onException(Exception("$errorMessage, $request"))
                                }
                                return
                            }
                        }
                    }
                }

                onException(Exception("Unknown API error: ${response.body}"))
            }

            override fun onFailure(call: Call, e: IOException) {
                onException(e)
            }
        })
    }

    fun isLoggedIn(): Boolean {
        return token.isNotEmpty()
    }
}

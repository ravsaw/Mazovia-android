package pl.edu.mazovia.mazovia

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.runBlocking
import pl.edu.mazovia.mazovia.RetrofitMazoviaApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

@Composable
fun LoginScreen(navController: NavController) {
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo lub obrazek
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = null,
            modifier = Modifier
                .size(150.dp)
                .padding(bottom = 16.dp)
        )

        // Pole loginu
        TextField(
            value = login,
            onValueChange = { login = it },
            label = { Text("Login") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            singleLine = true
        )

        // Pole hasła
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        // Kontener dla przycisków
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Główny przycisk logowania
            Button(
                onClick = {
                    // Logika logowania
                    runBlocking {
                        performLogin(login, password, navController, context)
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Login")
            }

            // Pierwszy przycisk testowy
            Button(
                onClick = { debugV() },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text("Verify")
            }

            // Drugi przycisk testowy
            Button(
                onClick = { debugUnV() },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text("Unverify")
            }
        }
    }
}

private suspend fun performLogin(
    login: String,
    password: String,
    navController: NavController,
    context: Context
) {
    if (login.isNotEmpty() && password.isNotEmpty()) {

        val retrofitMazoviaApi = RetrofitMazoviaApi.shared()
        var scode = ""
        var s = TokenService.getServerCode(context)
        if (s != null) {
            scode = s
        }
        retrofitMazoviaApi.sendLogin(username = login,password = password, serverCode = scode)
            .enqueue(object : Callback<LoginResponse> {
                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        Log.d("Success", "body is $body")

//                        if let aToken = result.accessToken, let rToken = result.refreshToken, let _ = result.expiresIn {
//                            TokenService.saveToken(aToken, rToken)
//                            self?.performSegue(withIdentifier: "LoginToHomeS", sender: self)
//                        } else if let status = result.status, let message = result.message, let serverCode = result.serverCode {
//                            TokenService.saveServerCode(serverCode)
//                            self?.showToast(message: "\(status) | \(message)")
//                        } else {
//                            self?.showToast(message: "\(result)")
//                        }

                        val result = response.body()
                        if (result?.access_token != null && result.refresh_token != null && result.expires_in != null) {
                        } else if (result?.status != null && result.message != null && result.serverCode != null) {
                            runBlocking {
                            }
                        } else {
                        }

                        when {
                            result?.access_token != null && result?.refresh_token != null && result?.expires_in != null -> {
                                //TokenService.saveToken(context, result.accessToken, result.refreshToken)
//                                TokenService.saveToken(result.access_token, result.refresh_token)
                                // performSegue(withIdentifier: "LoginToHomeS", sender: this)

                                runBlocking {
                                    TokenService.saveToken(
                                        context,
                                        result.access_token,
                                        result.refresh_token
                                    )
                                    Log.w("Success", "token saved")
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }
                                }
                            }
                            result?.status != null && result?.message != null && result?.serverCode != null -> {
                                // TokenService.saveServerCode(result.serverCode)
                                // showToast(message = "${result.status} | ${result.message}")
                                runBlocking {
                                TokenService.saveServerCode(context, result.serverCode)
                                }
                                Log.w("Success", "server code saved")
                            }
                            else -> {
                                // showToast(message = "$result")
                                Log.e("login failure", "login failed $result")
                            }
                        }

                    } else {
                        val errorBody = response.errorBody()
                        val errorMessage = errorBody?.string() ?: "Unknown error"
                        Log.w("unSuccess", "body is empty, error: $errorMessage")
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Log.e("failure", "failed", t)
                }
            })

    } else {

        navController.navigate(Screen.Home.route) {
            popUpTo(Screen.Login.route) { inclusive = true }
        }
    }
}

private fun debugV() {
    RetrofitMazoviaApi.shared().debugVerifyDevices()
        .enqueue(object : Callback<DebugVerifyResponse> {
            override fun onResponse(
                call: Call<DebugVerifyResponse>,
                response: Response<DebugVerifyResponse>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("Success", "body is $body")
                } else {
                    val errorBody = response.errorBody()
                    val errorMessage = errorBody?.string() ?: "Unknown error"
                    Log.w("unSuccess", "body is empty, error: $errorMessage")
                }
            }

            override fun onFailure(call: Call<DebugVerifyResponse>, t: Throwable) {
                Log.e("failure", "failed", t)
            }
        })
}

private fun debugUnV() {
    RetrofitMazoviaApi.shared().debugUnverifyDevices()
        .enqueue(object : Callback<DebugUnverifyResponse> {
            override fun onResponse(
                call: Call<DebugUnverifyResponse>,
                response: Response<DebugUnverifyResponse>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("Success", "body is $body")
                } else {
                    val errorBody = response.errorBody()
                    val errorMessage = errorBody?.string() ?: "Unknown error"
                    Log.w("unSuccess", "body is empty, error: $errorMessage")
                }
            }

            override fun onFailure(call: Call<DebugUnverifyResponse>, t: Throwable) {
                Log.e("failure", "failed", t)
            }
        })
}
package pl.edu.mazovia.mazovia.module.login

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import android.util.Log
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.edu.mazovia.mazovia.R
import pl.edu.mazovia.mazovia.Screen

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = viewModel(
        factory = LoginViewModel.Factory(LocalContext.current)
    )
) {
    var login by remember { mutableStateOf("apitester") }
    var password by remember { mutableStateOf("Apitester123") }
    var debugMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = null,
            modifier = Modifier
                .size(150.dp)
                .padding(bottom = 16.dp)
        )

        TextField(
            value = login,
            onValueChange = { login = it },
            label = { Text("Login") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            singleLine = true
        )

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

        // Debug message display
        debugMessage?.let { message ->
            Text(
                text = message,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                color = MaterialTheme.colorScheme.error
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    if (login.isNotEmpty() && password.isNotEmpty()) {
                        viewModel.login(
                            username = login,
                            password = password,
                            context = context,
                            onSuccess = {
                                navController.navigate(Screen.Verification.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            },
                            onServerCode = { message ->
                                debugMessage = "Server code saved: $message"
                            },
                            onError = { error ->
                                debugMessage = "Error: $error"
                            }
                        )
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Login")
            }

            Button(
                onClick = {
                    viewModel.debugVerify(
                        onSuccess = { message ->
                            debugMessage = message
                        },
                        onError = { error ->
                            debugMessage = error
                        }
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text("Verify")
            }

            Button(
                onClick = {
                    viewModel.debugUnverify(
                        onSuccess = { message ->
                            debugMessage = message
                        },
                        onError = { error ->
                            debugMessage = error
                        }
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text("Unverify")
            }
        }
    }
}
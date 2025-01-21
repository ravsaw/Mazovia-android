package pl.edu.mazovia.mazovia.module.landing

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import pl.edu.mazovia.mazovia.R
import pl.edu.mazovia.mazovia.Screen

@Composable
fun LandingScreen(
    navController: NavController,
    viewModel: LandingViewModel = LandingViewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.checkAuthStatus(
            context = context,
            onExpired = {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Landing.route) { inclusive = true }
                }
            },
            onValid = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Landing.route) { inclusive = true }
                }
            },
            onError = { error ->
                Log.e("Landing", "Error: $error")
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Landing.route) { inclusive = true }
                }
            }
        )
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = null,
            modifier = Modifier.size(200.dp)
        )
    }
}
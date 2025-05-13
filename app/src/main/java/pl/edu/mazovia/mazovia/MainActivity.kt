package pl.edu.mazovia.mazovia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pl.edu.mazovia.mazovia.module.home.HomeScreen
import pl.edu.mazovia.mazovia.module.landing.LandingScreen
import pl.edu.mazovia.mazovia.module.login.LoginScreen
import pl.edu.mazovia.mazovia.module.verification.VerificationScreen
import pl.edu.mazovia.mazovia.ui.theme.MazoviaTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MazoviaTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Landing.route) {
        composable(Screen.Landing.route) {
            LandingScreen(navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }


        composable(Screen.Verification.route) {
            VerificationScreen(navController)
        }
    }
}
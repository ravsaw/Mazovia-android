package pl.edu.mazovia.mazovia

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay


@Composable
fun LandingScreen(navController: NavController) {
    // Efekt będzie wykonany tylko raz przy inicjalizacji
    LaunchedEffect(Unit) {
        // Opóźnienie 5 sekund
        delay(5000)

        // Nawigacja do kolejnego ekranu
        navController.navigate(Screen.Login.route) {
            popUpTo(Screen.Landing.route) { inclusive = true }
        }
    }

    // Zawartość ekranu startowego
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
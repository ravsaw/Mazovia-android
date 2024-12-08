package pl.edu.mazovia.mazovia

sealed class Screen(val route: String) {
    object Landing : Screen("landing")
    object Login : Screen("login")
    object Home : Screen("home")
}
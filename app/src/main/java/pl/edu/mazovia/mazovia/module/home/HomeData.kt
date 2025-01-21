package pl.edu.mazovia.mazovia.module.home

data class HomeData(
    val type: String,
    val status: String,
    val created: String,
    val contextData: String,
    val verificationId: String
)
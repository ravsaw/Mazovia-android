package pl.edu.mazovia.mazovia.module.home

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data class Success(val items: List<HomeData>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
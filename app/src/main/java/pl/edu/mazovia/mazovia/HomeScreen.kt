package pl.edu.mazovia.mazovia

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.runBlocking

// Model danych
data class YourDataModel(
    val name: String,
    val description: String,
    val acceptClick: () -> Unit,
    val rejectClick: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableItemRow(
    item: YourDataModel,
    onEditClick: (YourDataModel) -> Unit,
    onDeleteClick: (YourDataModel) -> Unit
) {
    var isVisible by remember { mutableStateOf(true) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onDeleteClick(item)
                    isVisible = true
                    true
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    onEditClick(item)
                    isVisible = true
                    true
                }
                else -> false
            }
        }
    )

    AnimatedVisibility(
        visible = isVisible,
        exit = fadeOut(animationSpec = tween(durationMillis = 500)) +
                slideOutHorizontally(animationSpec = tween(durationMillis = 500))
    ) {
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                val color = when (dismissState.dismissDirection) {
                    SwipeToDismissBoxValue.StartToEnd -> Color.Green.copy(alpha = 0.5f)
                    SwipeToDismissBoxValue.EndToStart -> Color.Red.copy(alpha = 0.5f)
                    else -> Color.Transparent
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color)
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (dismissState.dismissDirection) {
                        SwipeToDismissBoxValue.StartToEnd -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Akceptuj",
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Edytuj", color = Color.White)
                            }
                        }
                        SwipeToDismissBoxValue.EndToStart -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Odrzuć",
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Odrzuć", color = Color.White)
                            }
                        }
                        else -> {}
                    }
                }
            },
            content = {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(item.name, style = MaterialTheme.typography.headlineSmall)
                        Text(item.description, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    // Stan listy i refreshingu
    var items by remember { mutableStateOf(listOf<YourDataModel>()) }
    var items2 by remember { mutableStateOf(listOf<YourDataModel>()) }
    var isRefreshing by remember { mutableStateOf(false) }

    // Funkcja do ładowania danych
    fun loadData() {
        isRefreshing = true
        // Tutaj wywołaj swoje API
        // Przykładowe dane
        items = listOf(
            YourDataModel("Tytuł 1", "Opis 1", {}, {}),
            YourDataModel("Tytuł 2", "Opis 2", {}, {})
        )
        items2 = listOf(
            YourDataModel("Tytuł 3", "Opis 3", {}, {}),
            YourDataModel("Tytuł 4", "Opis 4", {}, {})
        )
        isRefreshing = false

        runBlocking {

            val list1 = RetrofitMazoviaApi.shared().getTFAConfirmList().map { element ->
                    YourDataModel(element.ip_address, element.user_agent.orEmpty(), {}, {})
                }
            val list2 = RetrofitMazoviaApi.shared().getConfirmList().data.map { e ->
                YourDataModel(e.type, e.contextData, {}, {})
            }


        }


    }

    // Wywołaj ładowanie przy pierwszym uruchomieniu
    LaunchedEffect(Unit) {
        loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            // Pull to refresh
            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing),
                onRefresh = { loadData() }
            ) {
                // Lista lub widok pustej listy
                if (items.isEmpty()) {
                    EmptyListContent()
                } else {
                    LazyColumn {
                        items(items) { item ->
                            SwipeableItemRow(item = item, onEditClick = { }, onDeleteClick = { })
                        }
                    }
                }
            }
        }
    }
}

// Komponenty pomocnicze
@Composable
fun EmptyListContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Brak danych", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = { /* Odśwież */ }) {
                Text("Odśwież")
            }
        }
    }
}

@Composable
fun ItemRow(item: YourDataModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(item.name, style = MaterialTheme.typography.headlineSmall)
            Text(item.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
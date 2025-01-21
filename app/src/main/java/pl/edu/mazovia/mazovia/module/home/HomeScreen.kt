package pl.edu.mazovia.mazovia.module.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import pl.edu.mazovia.mazovia.Screen
import pl.edu.mazovia.mazovia.api.TokenService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = HomeViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.logout(context) {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Logout")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Odśwież")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (uiState) {
                is HomeUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is HomeUiState.Success -> {
                    val items = (uiState as HomeUiState.Success).items
                    if (items.isEmpty()) {
                        EmptyListContent(onRefresh = { viewModel.loadData() })
                    } else {
                        LazyColumn {
                            items(items) { item ->
                                ItemWithButtons(
                                    item = item,
                                    onAccept = { viewModel.verifyRequest(item, true) },
                                    onReject = { viewModel.verifyRequest(item, false) }
                                )
                            }
                        }
                    }
                }
                is HomeUiState.Error -> {
                    val error = (uiState as HomeUiState.Error).message
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(error)
                            Button(onClick = { viewModel.loadData() }) {
                                Text("Spróbuj ponownie")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemWithButtons(
    item: HomeData,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.padding(
                    top = 8.dp,
                    end = 8.dp,
                    start = 8.dp,
                    bottom = 0.dp
                )
            ) {
                Text("Type", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.weight(1f))
                Text("Status", style = MaterialTheme.typography.bodySmall)
            }
            Row(
                modifier = Modifier.padding(
                    top = 0.dp,
                    end = 8.dp,
                    start = 8.dp,
                    bottom = 0.dp
                )
            ) {
                Text(item.type, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.weight(1f))
                Text(item.status, style = MaterialTheme.typography.headlineSmall)
            }
            Row(modifier = Modifier.padding(8.dp)) {
                Text(
                    "created: ${item.created}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Row(modifier = Modifier.padding(8.dp)) {
                Text(item.contextData, style = MaterialTheme.typography.bodySmall)
            }

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onReject,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Red
                    )
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Odrzuć",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Odrzuć")
                }

                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Green,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Akceptuj",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Akceptuj")
                }
            }
        }
    }
}

@Composable
fun EmptyListContent(onRefresh: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Brak danych", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = onRefresh) {
                Text("Odśwież")
            }
        }
    }
}
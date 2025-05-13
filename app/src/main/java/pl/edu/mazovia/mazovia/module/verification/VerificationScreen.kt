package pl.edu.mazovia.mazovia.module.verification

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import pl.edu.mazovia.mazovia.models.VerificationDetail

/**
 * This is an example of a screen that would display and manage verifications.
 * It demonstrates how to use the VerificationViewModel with Jetpack Compose UI.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationScreen(
    navController: NavController,
    viewModel: VerificationViewModel = viewModel(
        factory = VerificationViewModel.Factory(LocalContext.current)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Dialog state for showing verification token input
    var showVerifyDialog by remember { mutableStateOf(false) }
    var verificationToken by remember { mutableStateOf("") }
    var verificationType by remember { mutableStateOf("email") }
    
    // Dialog state for showing verification status
    var showStatusDialog by remember { mutableStateOf(false) }
    var statusToken by remember { mutableStateOf("") }
    
    // State for snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    // Effect to handle action state changes
    LaunchedEffect(actionState) {
        when (actionState) {
            is VerificationActionState.Success -> {
                val message = (actionState as VerificationActionState.Success).message
                snackbarHostState.showSnackbar(message)
                viewModel.resetActionState()
            }
            is VerificationActionState.Error -> {
                val message = (actionState as VerificationActionState.Error).message
                snackbarHostState.showSnackbar(message)
                viewModel.resetActionState()
            }
            is VerificationActionState.VerificationStatus -> {
                // Handle status display
                showStatusDialog = true
            }
            else -> {}
        }
    }

    // Load verifications when the screen is first displayed
    LaunchedEffect(Unit) {
        viewModel.loadPendingVerifications()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verifications") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showVerifyDialog = true }) {
                        Icon(Icons.Default.Search, contentDescription = "Verify Token")
                    }
                    IconButton(onClick = { viewModel.loadPendingVerifications() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main content based on UI state
            when (uiState) {
                is VerificationUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is VerificationUiState.Empty -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("No verifications found")
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadPendingVerifications() }) {
                                Text("Refresh")
                            }
                        }
                    }
                }
                is VerificationUiState.Success -> {
                    val verifications = (uiState as VerificationUiState.Success).verifications
                    LazyColumn {
                        items(verifications) { verification ->
                            VerificationItem(
                                verification = verification,
                                onCancel = {
                                    // Only allow cancellation for pending verifications
                                    if (verification.status == "pending") {
                                        scope.launch {
                                            // In a real app, you would get the token from the verification
                                            // Here we just simulate with the ID for demonstration
                                            viewModel.cancelVerification(verification.id)
                                        }
                                    } else {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                "Only pending verifications can be cancelled"
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
                is VerificationUiState.Error -> {
                    val errorMessage = (uiState as VerificationUiState.Error).message
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(errorMessage, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadPendingVerifications() }) {
                                Text("Try Again")
                            }
                        }
                    }
                }
            }
            
            // Loading indicator overlay
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
    
    // Verify token dialog
    if (showVerifyDialog) {
        AlertDialog(
            onDismissRequest = { showVerifyDialog = false },
            title = { Text("Verify Token") },
            text = {
                Column {
                    OutlinedTextField(
                        value = verificationType,
                        onValueChange = { verificationType = it },
                        label = { Text("Type") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                    OutlinedTextField(
                        value = verificationToken,
                        onValueChange = { verificationToken = it },
                        label = { Text("Token") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (verificationToken.isNotBlank() && verificationType.isNotBlank()) {
                            viewModel.verifyToken(verificationType, verificationToken)
                            showVerifyDialog = false
                        }
                    }
                ) {
                    Text("Verify")
                }
            },
            dismissButton = {
                TextButton(onClick = { showVerifyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Show verification status dialog
    if (showStatusDialog) {
        val verification = (actionState as? VerificationActionState.VerificationStatus)?.verification
        
        AlertDialog(
            onDismissRequest = { 
                showStatusDialog = false
                viewModel.resetActionState()
            },
            title = { Text("Verification Status") },
            text = {
                if (verification != null) {
                    Column {
                        Text("ID: ${verification.id}")
                        Text("Type: ${verification.type}")
                        Text("Status: ${verification.status}")
                        Text("Created: ${verification.createdAt}")
                        Text("Expires: ${verification.expiresAt}")
                        Text("Context: ${verification.contextData}")
                    }
                } else {
                    Text("No verification details available")
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        showStatusDialog = false
                        viewModel.resetActionState()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun VerificationItem(
    verification: VerificationDetail,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with type and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = verification.type,
                    style = MaterialTheme.typography.titleMedium
                )
                
                StatusBadge(status = verification.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Created date
            Text(
                text = "Created: ${verification.createdAt}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            // Expires date
            Text(
                text = "Expires: ${verification.expiresAt}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            // Context data
            Text(
                text = "Context: ${verification.contextData}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (verification.status == "pending") {
                    OutlinedButton(
                        onClick = onCancel,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Red
                        )
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cancel",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (backgroundColor, textColor) = when (status.lowercase()) {
        "pending" -> Color(0xFFFFA000) to Color.White
        "verified" -> Color(0xFF4CAF50) to Color.White
        "expired" -> Color(0xFFE91E63) to Color.White
        "cancelled" -> Color(0xFF9E9E9E) to Color.White
        else -> Color(0xFF2196F3) to Color.White
    }
    
    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
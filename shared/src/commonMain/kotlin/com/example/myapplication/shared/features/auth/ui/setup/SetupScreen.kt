package com.example.myapplication.shared.features.auth.ui.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.myapplication.shared.core.navigation.Navigator
import com.example.myapplication.shared.core.navigation.Screen
import com.example.myapplication.shared.features.auth.viewmodel.SetupUiEffect
import com.example.myapplication.shared.features.auth.viewmodel.SetupUiEvent
import com.example.myapplication.shared.features.auth.viewmodel.SetupViewModel
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource
import myapplication.shared.generated.resources.Res
import myapplication.shared.generated.resources.jkw_logo
import com.example.myapplication.shared.presentation.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    navigator: Navigator,
    viewModel: SetupViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val currentState = state ?: return

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is SetupUiEffect.NavigateToLogin -> {
                    navigator.navigateTo(Screen.Login)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .widthIn(max = 400.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(Res.drawable.jkw_logo),
                        contentDescription = "JKW Logo",
                        modifier = Modifier.height(48.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "First-Time Setup",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Create the Admin account to secure this system.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    if (currentState.errorMessage != null) {
                        Text(
                            text = currentState.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    OutlinedTextField(
                        value = currentState.username,
                        onValueChange = { viewModel.onEvent(SetupUiEvent.OnUsernameChanged(it)) },
                        label = { Text("Admin Username") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = currentState.password,
                        onValueChange = { viewModel.onEvent(SetupUiEvent.OnPasswordChanged(it)) },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (currentState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { viewModel.onEvent(SetupUiEvent.OnTogglePasswordVisibility) }) {
                                Icon(
                                    imageVector = if (currentState.isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        }
                    )

                    OutlinedTextField(
                        value = currentState.confirmPassword,
                        onValueChange = { viewModel.onEvent(SetupUiEvent.OnConfirmPasswordChanged(it)) },
                        label = { Text("Confirm Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (currentState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        isError = currentState.password.isNotEmpty() && currentState.confirmPassword.isNotEmpty() && currentState.password != currentState.confirmPassword
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.onEvent(SetupUiEvent.OnFinishClicked) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        enabled = !currentState.isLoading,
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)
                    ) {
                        if (currentState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Finish Setup", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

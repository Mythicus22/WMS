package com.example.myapplication.shared.features.shuttle.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.shared.core.navigation.Navigator
import com.example.myapplication.shared.core.navigation.Screen
import com.example.myapplication.shared.features.shuttle.model.Shuttle
import com.example.myapplication.shared.features.shuttle.viewmodel.ShuttleSortType
import com.example.myapplication.shared.features.shuttle.viewmodel.ShuttleUiEffect
import com.example.myapplication.shared.features.shuttle.viewmodel.ShuttleUiEvent
import com.example.myapplication.shared.features.shuttle.viewmodel.ShuttleUiState
import com.example.myapplication.shared.features.shuttle.viewmodel.ShuttleViewModel
import com.example.myapplication.shared.presentation.components.AppToolbar
import com.example.myapplication.shared.presentation.theme.AppColors
import com.example.myapplication.shared.presentation.theme.AppDimensions
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShuttleScreen(
    navigator: Navigator,
    viewModel: ShuttleViewModel,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val state by viewModel.uiState.collectAsState()
    val currentState = state ?: ShuttleUiState()
    
    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is ShuttleUiEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navigator.navigateTo(Screen.AddEditShuttle()) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Shuttle")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            AppToolbar(
                title = "Shuttle Management",
                onNavigationClick = { navigator.goBack() }
            )
            
            // Search and Sort
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppDimensions.spacing16),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = currentState.searchQuery,
                    onValueChange = { viewModel.onEvent(ShuttleUiEvent.OnSearchQueryChanged(it)) },
                    placeholder = { Text("Search by Name or ID") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.width(AppDimensions.spacing8))
                
                var expanded by remember { mutableStateOf(false) }
                Box {
                    Button(onClick = { expanded = true }) {
                        Text("Sort: ${currentState.sortType.name}")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sort by Name") },
                            onClick = {
                                viewModel.onEvent(ShuttleUiEvent.OnSortTypeChanged(ShuttleSortType.NAME))
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sort by ID") },
                            onClick = {
                                viewModel.onEvent(ShuttleUiEvent.OnSortTypeChanged(ShuttleSortType.ID))
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(AppDimensions.spacing16))

            if (currentState.filteredShuttles.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Shuttles Found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(AppDimensions.spacing16),
                    verticalArrangement = Arrangement.spacedBy(AppDimensions.spacing8)
                ) {
                    items(currentState.filteredShuttles, key = { it.id }) { shuttle ->
                        ShuttleItemCard(
                            shuttle = shuttle,
                            onEdit = { navigator.navigateTo(Screen.AddEditShuttle(shuttle.id)) },
                            onDelete = { viewModel.onEvent(ShuttleUiEvent.OnDeleteShuttle(shuttle.id)) },
                            onToggleStatus = { viewModel.onEvent(ShuttleUiEvent.OnToggleShuttleStatus(shuttle.id, !shuttle.isEnabled)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShuttleItemCard(
    shuttle: Shuttle,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleStatus: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(AppDimensions.spacing16)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = shuttle.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ID: ${shuttle.id}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (shuttle.isEnabled) "ENABLED" else "DISABLED",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (shuttle.isEnabled) AppColors.Success else MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .background(
                                color = (if (shuttle.isEnabled) AppColors.Success else MaterialTheme.colorScheme.error).copy(alpha = 0.1f),
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = shuttle.isEnabled,
                        onCheckedChange = { onToggleStatus() }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(AppDimensions.spacing8))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "IP: ${shuttle.plcIpAddress}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "FW: ${shuttle.firmwareVersion}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.height(AppDimensions.spacing8))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(AppDimensions.spacing8))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Shuttle", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Shuttle", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

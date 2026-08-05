package com.example.myapplication.shared.features.usermanagement.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.shared.core.navigation.Navigator
import com.example.myapplication.shared.core.navigation.Screen
import com.example.myapplication.shared.domain.model.FeaturePermission
import com.example.myapplication.shared.domain.model.SettingPermission
import com.example.myapplication.shared.domain.model.User
import com.example.myapplication.shared.domain.model.UserRole
import com.example.myapplication.shared.features.usermanagement.viewmodel.UserManagementUiEvent
import com.example.myapplication.shared.features.usermanagement.viewmodel.UserManagementViewModel
import com.example.myapplication.shared.presentation.components.AppToolbar
import com.example.myapplication.shared.presentation.components.PrimaryButton
import com.example.myapplication.shared.presentation.components.SecondaryButton
import com.example.myapplication.shared.presentation.theme.AppColors
import com.example.myapplication.shared.presentation.theme.AppDimensions

@Composable
fun UserManagementScreen(
    navigator: Navigator,
    viewModel: UserManagementViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val currentState = state ?: com.example.myapplication.shared.features.usermanagement.viewmodel.UserManagementUiState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Header Toolbar
        AppToolbar(
            title = "User Directory & RBAC",
            onNavigationClick = { navigator.goBack() }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppDimensions.spacing16)
        ) {
            // Success / Error Messages
            currentState.errorMessage?.let { error ->
                MessageBanner(text = error, isError = true, onDismiss = { viewModel.onEvent(UserManagementUiEvent.OnDismissMessages) })
                Spacer(modifier = Modifier.height(AppDimensions.spacing12))
            }

            currentState.successMessage?.let { msg ->
                MessageBanner(text = msg, isError = false, onDismiss = { viewModel.onEvent(UserManagementUiEvent.OnDismissMessages) })
                Spacer(modifier = Modifier.height(AppDimensions.spacing12))
            }

            // Top Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SYSTEM USERS (${currentState.users.size})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )

                Button(
                    onClick = { viewModel.onEvent(UserManagementUiEvent.OnAddUserClicked) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(AppDimensions.spacing4))
                    Text("Add User", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(modifier = Modifier.height(AppDimensions.spacing16))

            // Users List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(AppDimensions.spacing12),
                modifier = Modifier.fillMaxSize()
            ) {
                items(currentState.users) { user ->
                    UserListItemCard(
                        user = user,
                        onEdit = { viewModel.onEvent(UserManagementUiEvent.OnEditUserClicked(user)) },
                        onDelete = { viewModel.onEvent(UserManagementUiEvent.OnDeleteUserClicked(user)) }
                    )
                }
            }
        }
    }

    // Add User Dialog
    if (currentState.isAddUserDialogOpen) {
        UserEditorDialog(
            userToEdit = null,
            onDismiss = { viewModel.onEvent(UserManagementUiEvent.OnDismissDialogs) },
            onSave = { username, password, role, features, settings ->
                viewModel.onEvent(
                    UserManagementUiEvent.OnCreateUserSubmitted(
                        username = username,
                        passwordRaw = password,
                        role = role,
                        grantedFeatures = features,
                        grantedSettings = settings
                    )
                )
            }
        )
    }

    // Edit User Dialog
    currentState.editingUser?.let { user ->
        UserEditorDialog(
            userToEdit = user,
            onDismiss = { viewModel.onEvent(UserManagementUiEvent.OnDismissDialogs) },
            onSave = { _, password, role, features, settings ->
                viewModel.onEvent(
                    UserManagementUiEvent.OnUpdateUserSubmitted(
                        userId = user.id,
                        role = role,
                        grantedFeatures = features,
                        grantedSettings = settings,
                        newPasswordRaw = password.ifBlank { null }
                    )
                )
            },
            onDelete = { viewModel.onEvent(UserManagementUiEvent.OnDeleteUserClicked(user)) }
        )
    }

    // Delete Confirmation Dialog
    currentState.deletingUser?.let { user ->
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(UserManagementUiEvent.OnDismissDialogs) },
            title = { Text("Delete User", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete user '${user.username}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.onEvent(UserManagementUiEvent.OnConfirmDeleteUser(user.id)) },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(UserManagementUiEvent.OnDismissDialogs) }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun MessageBanner(text: String, isError: Boolean, onDismiss: () -> Unit) {
    val bgColor = if (isError) AppColors.Error.copy(alpha = 0.15f) else AppColors.Success.copy(alpha = 0.15f)
    val contentColor = if (isError) AppColors.Error else AppColors.Success

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = AppDimensions.spacing12, vertical = AppDimensions.spacing8),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isError) Icons.Default.Warning else Icons.Default.Check,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(AppDimensions.spacing8))
            Text(text = text, style = MaterialTheme.typography.bodySmall, color = contentColor)
        }
        TextButton(onClick = onDismiss) {
            Text("OK", style = MaterialTheme.typography.labelSmall, color = contentColor)
        }
    }
}

@Composable
private fun UserListItemCard(
    user: User,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(AppDimensions.spacing16)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (user.isAdmin) AppColors.Primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (user.isAdmin) Icons.Default.Shield else Icons.Default.Person,
                            contentDescription = null,
                            tint = if (user.isAdmin) AppColors.Primary else MaterialTheme.colorScheme.secondary
                        )
                    }

                    Spacer(modifier = Modifier.width(AppDimensions.spacing12))

                    Column {
                        Text(
                            text = user.username,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "ID: ${user.id} • Role: ${user.role.displayName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit User",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppDimensions.spacing12))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(AppDimensions.spacing12))

            // Granted Permissions Badges Summary
            Text(
                text = "Granted Features (${if (user.isAdmin) "All (Admin)" else user.grantedFeatures.size}):",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(AppDimensions.spacing4))
            Text(
                text = if (user.isAdmin) "All features unlocked" else user.grantedFeatures.joinToString(", ") { it.displayName }.ifEmpty { "None" },
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(AppDimensions.spacing8))

            Text(
                text = "Granted Settings (${if (user.isAdmin) "All (Admin)" else user.grantedSettings.size}):",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(AppDimensions.spacing4))
            Text(
                text = if (user.isAdmin) "All settings unlocked" else user.grantedSettings.joinToString(", ") { it.displayName }.ifEmpty { "None" },
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Dialog for creating or editing user permissions with Checkboxes for features & settings.
 */
@Composable
private fun UserEditorDialog(
    userToEdit: User?,
    onDismiss: () -> Unit,
    onSave: (
        username: String,
        passwordRaw: String,
        role: UserRole,
        grantedFeatures: Set<FeaturePermission>,
        grantedSettings: Set<SettingPermission>
    ) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val isEditMode = userToEdit != null

    var username by remember { mutableStateOf(userToEdit?.username ?: "") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(userToEdit?.role ?: UserRole.OPERATOR) }

    var selectedFeatures by remember {
        mutableStateOf(userToEdit?.grantedFeatures ?: setOf(FeaturePermission.OPERATOR))
    }

    var selectedSettings by remember {
        mutableStateOf(userToEdit?.grantedSettings ?: emptySet())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditMode) "Edit User: ${userToEdit?.username}" else "Add New User",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (!isEditMode) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(AppDimensions.spacing12))
                }

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(if (isEditMode) "New Password (optional)" else "Password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(AppDimensions.spacing16))

                Text(
                    text = "Role",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(AppDimensions.spacing8))

                val isAdminMain = userToEdit?.username?.lowercase() == "admin"

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppDimensions.spacing4)
                ) {
                    UserRole.entries.forEach { role ->
                        val isSelected = selectedRole == role
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(enabled = !isAdminMain) { selectedRole = role },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = AppDimensions.spacing8),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = role.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isAdminMain && !isSelected) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else Color.Unspecified
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppDimensions.spacing20))

                var showAdminConfirmationDialog by remember { mutableStateOf(false) }

                if (showAdminConfirmationDialog) {
                    AlertDialog(
                        onDismissRequest = { showAdminConfirmationDialog = false },
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = AppColors.Error)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Confirm Admin Powers", fontWeight = FontWeight.Bold)
                            }
                        },
                        text = {
                            Text("Warning: Granting 'User Management (Admin Powers)' permits this account to manage all system users, modify roles, and grant permissions. Are you sure you want to proceed?")
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    selectedFeatures = selectedFeatures + FeaturePermission.USER_MANAGEMENT
                                    showAdminConfirmationDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Error)
                            ) {
                                Text("Grant Admin Powers")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showAdminConfirmationDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                // Feature Permissions Checkboxes
                Text(
                    text = "Granted Feature Modules",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Check the modules allowed for this user:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(AppDimensions.spacing8))

                // Highlighted Admin Powers Checkbox (User Management) at the top
                val isAdminPowerChecked = selectedFeatures.contains(FeaturePermission.USER_MANAGEMENT)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(
                            width = 1.dp,
                            color = AppColors.Error.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable(enabled = !isAdminMain) {
                            if (!isAdminPowerChecked) {
                                showAdminConfirmationDialog = true
                            } else {
                                selectedFeatures = selectedFeatures - FeaturePermission.USER_MANAGEMENT
                            }
                        },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.Error.copy(alpha = if (isAdminMain) 0.05f else 0.12f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppDimensions.spacing8, vertical = AppDimensions.spacing8),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isAdminPowerChecked,
                            enabled = !isAdminMain,
                            onCheckedChange = { checked ->
                                if (checked == true) {
                                    showAdminConfirmationDialog = true
                                } else {
                                    selectedFeatures = selectedFeatures - FeaturePermission.USER_MANAGEMENT
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = AppColors.Error,
                                checkmarkColor = Color.White,
                                disabledCheckedColor = AppColors.Error.copy(alpha = 0.5f)
                            )
                        )
                        Spacer(modifier = Modifier.width(AppDimensions.spacing4))
                        Column(modifier = Modifier.padding(start = 4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = if (isAdminMain) AppColors.Error.copy(alpha = 0.5f) else AppColors.Error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "User Management (Admin Powers)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAdminMain) AppColors.Error.copy(alpha = 0.5f) else AppColors.Error
                                )
                            }
                            Text(
                                text = if (isAdminMain) "Cannot be disabled for the main admin account." else "Grants full administrative privileges (create, edit, delete users and grant permissions).",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppDimensions.spacing4))

                // Remaining Feature Permissions Checkboxes
                FeaturePermission.entries.filterNot { it == FeaturePermission.USER_MANAGEMENT }.forEach { feature ->
                    val isChecked = selectedFeatures.contains(feature)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedFeatures = if (isChecked) {
                                    selectedFeatures - feature
                                } else {
                                    selectedFeatures + feature
                                }
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checked ->
                                selectedFeatures = if (checked == true) {
                                    selectedFeatures + feature
                                } else {
                                    selectedFeatures - feature
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(AppDimensions.spacing4))
                        Column {
                            Text(text = feature.displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(text = feature.description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppDimensions.spacing16))

                // Setting Permissions Checkboxes
                Text(
                    text = "Granted Setting Sections",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Check the setting screens allowed for this user:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(AppDimensions.spacing8))

                SettingPermission.entries.forEach { setting ->
                    val isChecked = selectedSettings.contains(setting)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedSettings = if (isChecked) {
                                    selectedSettings - setting
                                } else {
                                    selectedSettings + setting
                                }
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checked ->
                                selectedSettings = if (checked == true) {
                                    selectedSettings + setting
                                } else {
                                    selectedSettings - setting
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(AppDimensions.spacing4))
                        Column {
                            Text(text = setting.displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(text = setting.description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(username, password, selectedRole, selectedFeatures, selectedSettings)
                },
                enabled = isEditMode || (username.isNotBlank() && password.isNotBlank())
            ) {
                Text(if (isEditMode) "Update Permissions" else "Add User")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isEditMode && userToEdit?.username?.lowercase() != "admin") {
                    TextButton(onClick = { 
                        onDismiss()
                        onDelete?.invoke() 
                    }) {
                        Text("Delete User", color = AppColors.Error)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

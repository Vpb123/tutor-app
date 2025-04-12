package com.mytutor.app.ui.screens.auth.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mytutor.app.data.remote.models.UserRole

@Composable
fun RoleSelector(
    selectedRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Select Role",
            style = MaterialTheme.typography.labelLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            UserRole.values().forEach { role ->
                FilterChip(
                    selected = selectedRole == role,
                    onClick = { onRoleSelected(role) },
                    label = {
                        Text(
                            text = role.name.lowercase().replaceFirstChar { it.uppercase() }
                        )
                    }
                )
            }
        }
    }
}

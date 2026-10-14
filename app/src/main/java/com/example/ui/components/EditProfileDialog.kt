package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.UserSettingsEntity
import com.example.ui.theme.AppTheme
import java.io.File

@Composable
fun EditProfileDialog(
    userSettings: UserSettingsEntity?,
    onDismissRequest: () -> Unit,
    onSaveProfile: (name: String, email: String, profilePictureUri: String) -> Unit
) {
    val context = LocalContext.current
    val colors = AppTheme.colors
    val current = userSettings ?: UserSettingsEntity()

    var name by remember { mutableStateOf(current.name) }
    var email by remember { mutableStateOf(current.email) }
    var profilePhotoPath by remember { mutableStateOf(current.profilePictureUri) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                // Save and validate picked image
                val savedPath = ProfilePhotoHelper.saveProfilePhotoToInternalStorage(context, uri)
                selectedImageUri = uri
                profilePhotoPath = savedPath
                errorMessage = null
                Toast.makeText(context, "Photo selected! Save changes to apply.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to process selected image."
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "Edit Profile",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = colors.textCrispWhite
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Avatar with Camera Badge Overlay
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .clickable { photoPickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(2.dp, Color(0xFFA855F7), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val photoFile = remember(profilePhotoPath) {
                            if (profilePhotoPath.isNotBlank()) File(profilePhotoPath) else null
                        }

                        if (photoFile != null && photoFile.exists()) {
                            AsyncImage(
                                model = photoFile,
                                contentDescription = "Profile Picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(86.dp).clip(CircleShape)
                            )
                        } else if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Profile Picture Preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(86.dp).clip(CircleShape)
                            )
                        } else {
                            Text(
                                text = current.name.trim().take(1).uppercase().ifEmpty { "U" },
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFA855F7)
                            )
                        }
                    }

                    // Camera Badge Overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFA855F7))
                            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Upload Photo",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                Text(
                    text = "Tap photo to change avatar (Max 5 MB)",
                    fontSize = 11.sp,
                    color = colors.textMutedLavender
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFA855F7),
                        unfocusedBorderColor = colors.surfaceBorder,
                        focusedLabelColor = Color(0xFFA855F7)
                    )
                )

                // Email Input (Read-only if guest / account)
                OutlinedTextField(
                    value = email.ifBlank { if (current.isGuest) "Guest User" else "user@example.com" },
                    onValueChange = {},
                    label = { Text("Email Address") },
                    singleLine = true,
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = colors.surfaceBorder,
                        disabledLabelColor = colors.textMutedLavender
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Name cannot be empty."
                        return@Button
                    }
                    isSaving = true
                    onSaveProfile(name.trim(), email.trim(), profilePhotoPath)
                    isSaving = false
                    Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    onDismissRequest()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7)),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(6.dp))
                }
                Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismissRequest,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel", color = colors.textMutedLavender)
            }
        }
    )
}

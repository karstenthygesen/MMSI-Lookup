package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.ui.theme.MaritimeCyan
import com.example.ui.theme.MaritimeGreen
import com.example.ui.theme.MaritimeRed
import com.example.ui.theme.NauticalGold
import com.example.ui.theme.SlateTextMuted
import com.example.ui.theme.SlateTextSecondary

@Composable
fun MmsiInfoDialog(
    lastUpdatedDate: String,
    databaseVersion: String,
    isUpdating: Boolean,
    updateResultMessage: String?,
    updateResultSuccess: Boolean?,
    onUpdateClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val contactEmail = "karthy@gmail.com"
    val contactName = "Karsten Thygesen"
    val appVersion = "v${try { BuildConfig.VERSION_NAME } catch (e: Throwable) { "1.2.0" }}"
    val buildDate = try { BuildConfig.BUILD_DATE } catch (e: Throwable) { "September 12, 2026" }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("mmsi_info_dialog"),
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaritimeCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaritimeCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "About MMSI Lookup",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Version $appVersion • Built $buildDate",
                        fontSize = 12.sp,
                        color = SlateTextSecondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Database Status & Last Updated Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "MARITIME DATABASE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = MaritimeCyan
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaritimeGreen.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Offline Ready",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaritimeGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Last Updated:",
                                fontSize = 11.sp,
                                color = SlateTextSecondary
                            )
                            Text(
                                text = lastUpdatedDate,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.testTag("last_updated_date_text")
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "ITU Specification:",
                                fontSize = 11.sp,
                                color = SlateTextSecondary
                            )
                            Text(
                                text = databaseVersion,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaritimeGreen,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Build Auto-Sync: Fresh ITU release bundled with this build",
                                fontSize = 11.sp,
                                color = MaritimeGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Online Database Update Section
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Internet Database Update",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Connect to the Internet to verify and synchronize the latest maritime identification codes from the registry.",
                        fontSize = 12.sp,
                        color = SlateTextSecondary,
                        lineHeight = 16.sp
                    )

                    Button(
                        onClick = onUpdateClick,
                        enabled = !isUpdating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("update_database_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaritimeCyan,
                            contentColor = Color(0xFF07111E)
                        )
                    ) {
                        if (isUpdating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF07111E)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Updating Database...", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Update Database Online", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Result message banner
                    if (updateResultMessage != null) {
                        val isSuccess = updateResultSuccess == true
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSuccess) MaritimeGreen.copy(alpha = 0.12f) else MaritimeRed.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSuccess) MaritimeGreen.copy(alpha = 0.4f) else MaritimeRed.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = if (isSuccess) MaritimeGreen else MaritimeRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = updateResultMessage,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.testTag("update_result_message")
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    thickness = 1.dp
                )

                // Feedback & Contact Karsten Thygesen
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "COMMENTS & ENHANCEMENTS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = NauticalGold
                        )

                        Text(
                            text = "Please contact me for comments, questions, or suggestions for enhancements:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = contactName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.testTag("contact_name_text")
                                )
                                Text(
                                    text = contactEmail,
                                    fontSize = 13.sp,
                                    color = MaritimeCyan,
                                    modifier = Modifier.testTag("contact_email_text")
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                // Copy Email Button
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(contactEmail))
                                        Toast.makeText(context, "Email copied: $contactEmail", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.testTag("copy_email_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy email address",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Send Email Intent Button
                                IconButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                                            data = Uri.parse("mailto:$contactEmail")
                                            putExtra(Intent.EXTRA_SUBJECT, "MMSI App - Comments & Suggestions")
                                        }
                                        try {
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            clipboardManager.setText(AnnotatedString(contactEmail))
                                            Toast.makeText(context, "Email copied: $contactEmail", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.testTag("send_email_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = "Send email to Karsten Thygesen",
                                        tint = NauticalGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_info_dialog_btn")
            ) {
                Text("Close", color = MaritimeCyan, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

package com.example.sweetbakes.Settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sweetbakes.R

@Composable
fun SettingsScreen(
    isDarkMode: Boolean,
    onDarkModeToggle: (Boolean) -> Unit,
    selectedLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    var showLanguageDialog by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    var languageUpdateKey by remember { mutableStateOf(0) }

    val languages = listOf("English", "Tagalog", "Waraynon")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Dark Mode",
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = isDarkMode,
                onCheckedChange = { onDarkModeToggle(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Divider(modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = context.getString(R.string.language),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable { showLanguageDialog = true },
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 2.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "Language icon",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${LocalContext.current.getString(R.string.selected_language)}: $selectedLanguage",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (showLanguageDialog) {
            AlertDialog(
                onDismissRequest = { showLanguageDialog = false },
                title = { Text(context.getString(R.string.choose_language), color = MaterialTheme.colorScheme.onSurface) },
                text = {
                    Column {
                        languages.forEach { language ->
                            TextButton(
                                onClick = {
                                    onLanguageChange(language)
                                    showLanguageDialog = false
                                    languageUpdateKey++
                                    LocaleHelper.setLocale(context, language)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = language,
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showLanguageDialog = false }) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            )
        }
    }
}
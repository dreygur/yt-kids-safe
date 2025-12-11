package com.ytkidssafe.ui.screens.parent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ytkidssafe.BuildConfig
import com.ytkidssafe.ui.theme.Background
import com.ytkidssafe.ui.theme.Primary
import com.ytkidssafe.ui.theme.TextLight

data class Library(
    val name: String,
    val description: String,
    val license: String
)

private val libraries = listOf(
    Library("Jetpack Compose", "Modern Android UI toolkit", "Apache 2.0"),
    Library("Kotlin Coroutines", "Asynchronous programming", "Apache 2.0"),
    Library("Hilt", "Dependency injection for Android", "Apache 2.0"),
    Library("Room", "SQLite database abstraction", "Apache 2.0"),
    Library("DataStore", "Data storage solution", "Apache 2.0"),
    Library("Navigation Compose", "Navigation for Compose", "Apache 2.0"),
    Library("Media3 ExoPlayer", "Media playback library", "Apache 2.0"),
    Library("Coil", "Image loading for Compose", "Apache 2.0"),
    Library("OkHttp", "HTTP client", "Apache 2.0"),
    Library("NewPipe Extractor", "YouTube stream extraction", "GPL 3.0"),
    Library("WorkManager", "Background task scheduling", "Apache 2.0"),
    Library("Kotlinx Serialization", "JSON serialization", "Apache 2.0")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditsScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Open Source Libraries") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary)
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                Text(
                    text = "YT Kids Safe is built with the following open source libraries:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextLight,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(libraries) { library ->
                LibraryCard(library)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "YT Kids Safe v${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextLight,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun LibraryCard(library: Library) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = library.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = library.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextLight
            )
            Text(
                text = "License: ${library.license}",
                style = MaterialTheme.typography.bodySmall,
                color = TextLight.copy(alpha = 0.7f)
            )
        }
    }
}

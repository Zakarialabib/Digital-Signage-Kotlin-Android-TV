package com.signagepro.app.features.content.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.signagepro.app.features.content.model.ContentItem
import com.signagepro.app.features.content.viewmodel.ContentListViewModel

@Composable
fun ContentListScreen(
    viewModel: ContentListViewModel,
    onContentSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        // Top Bar with refresh and settings
        SmallTopAppBar(
            title = { Text("Content Library") },
            actions = {
                IconButton(onClick = { viewModel.refreshContent() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh"
                    )
                }
                IconButton(onClick = { /* Navigate to settings */ }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings"
                    )
                }
            }
        )

        when (uiState) {
            is ContentListUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ContentListUiState.Error -> {
                ErrorView(
                    message = (uiState as ContentListUiState.Error).message,
                    onRetry = { viewModel.refreshContent() }
                )
            }
            is ContentListUiState.Success -> {
                ContentGrid(
                    contents = (uiState as ContentListUiState.Success).contents,
                    onContentSelected = onContentSelected
                )
            }
        }
    }
}

@Composable
private fun ContentGrid(
    contents: List<ContentItem>,
    onContentSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(contents) { content ->
            ContentCard(
                content = content,
                onClick = { onContentSelected(content.id) }
            )
        }
    }
}

@Composable
private fun ContentCard(
    content: ContentItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ContentListViewModel = hiltViewModel()
) {
    val downloadStatus by viewModel.getDownloadStatus(content.id).collectAsState(initial = DownloadStatus.NotStarted)
    
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Thumbnail
            AsyncImage(
                model = content.thumbnailUrl,
                contentDescription = content.title,
                modifier = Modifier.fillMaxSize()
            )

            // Overlay with title and download status
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = content.title,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    ContentDownloadProgress(
                        downloadStatus = downloadStatus,
                        onDownloadClick = { viewModel.startDownload(content.id) },
                        onCancelClick = { viewModel.cancelDownload(content.id) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

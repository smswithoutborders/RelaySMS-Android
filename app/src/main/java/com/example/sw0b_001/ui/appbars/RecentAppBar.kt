package com.example.sw0b_001.ui.appbars

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.theme.AppTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun RecentAppBar(
    onSearchQueryChanged: (String) -> Unit,
    searchQuery: String,
    isSearchActive: Boolean,
    onToggleSearch: () -> Unit,
    onSearchDone: () -> Unit,
    isSelectionMode: Boolean = false,
    selectedCount: Int = 0,
    onSelectAll: (() -> Unit)? = null,
    onDeleteSelected: (() -> Unit)? = null,
    onCancelSelection: (() -> Unit)? = null,
    onComposeClicked: (() -> Unit)? = null,
) {
    val focusManager = LocalFocusManager.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Column(modifier = Modifier.fillMaxWidth()) {
        if (isSelectionMode) {
            CenterAlignedTopAppBar(
                title = {
                    Text(stringResource(R.string.selected_messages, selectedCount))
                },
                navigationIcon = {
                    IconButton(onClick = { onCancelSelection?.invoke() }) {
                        Icon(
                            imageVector = Icons.Filled.Cancel,
                            contentDescription = stringResource(R.string.cancel)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onSelectAll?.invoke() }) {
                        Icon(
                            imageVector = Icons.Filled.SelectAll,
                            contentDescription = stringResource(R.string.select_all)
                        )
                    }
                    IconButton(onClick = { onDeleteSelected?.invoke() }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.delete)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(),
                scrollBehavior = scrollBehavior,
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
            )
        } else {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.recents_text),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(42.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = CircleShape,
                                clip = false
                            )
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = { onComposeClicked?.invoke() }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = stringResource(R.string.compose_new),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(),
                scrollBehavior = scrollBehavior,
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
            )
        }

        if (isSearchActive) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChanged,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(R.string.search_message)) },
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        focusManager.clearFocus()
                        onSearchDone()
                    }),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = {
                    onToggleSearch()
                    onSearchQueryChanged("")
                    focusManager.clearFocus()
                }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.close_search)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecentsAppBarPreview() {
    AppTheme(darkTheme = false) {
        var searchQuery by remember { mutableStateOf("") }
        var isSearchActive by remember { mutableStateOf(false) }
        RecentAppBar(
            onSearchQueryChanged = { searchQuery = it },
            searchQuery = searchQuery,
            isSearchActive = isSearchActive,
            onToggleSearch = { isSearchActive = !isSearchActive },
            onSearchDone = {},
            onComposeClicked = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RecentsAppBarSelectionModePreview() {
    AppTheme(darkTheme = false) {
        RecentAppBar(
            onSearchQueryChanged = { },
            searchQuery = "",
            isSearchActive = false,
            onToggleSearch = { },
            onSearchDone = {},
            isSelectionMode = true,
            selectedCount = 3,
            onSelectAll = { },
            onDeleteSelected = { },
            onCancelSelection = { }
        )
    }
}
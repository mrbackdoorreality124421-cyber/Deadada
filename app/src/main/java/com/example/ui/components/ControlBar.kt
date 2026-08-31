package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BoardTheme
import com.example.model.Difficulty

@Composable
fun ControlBar(
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    currentDifficulty: Difficulty,
    onDifficultyChanged: (Difficulty) -> Unit,
    currentTheme: BoardTheme,
    onThemeChanged: (BoardTheme) -> Unit,
    onNewGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDifficultyMenu by remember { mutableStateOf(false) }
    var showThemeMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Undo button
            IconButton(
                onClick = onUndo,
                enabled = canUndo,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                ),
                modifier = Modifier.testTag("undo_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Undo move",
                    modifier = Modifier.size(26.dp)
                )
            }

            // Redo button
            IconButton(
                onClick = onRedo,
                enabled = canRedo,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                ),
                modifier = Modifier.testTag("redo_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Redo,
                    contentDescription = "Redo move",
                    modifier = Modifier.size(26.dp)
                )
            }

            // Difficulty selector Chip
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .clickable { showDifficultyMenu = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("difficulty_button"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Difficulty",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = currentDifficulty.name,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                DropdownMenu(
                    expanded = showDifficultyMenu,
                    onDismissRequest = { showDifficultyMenu = false }
                ) {
                    Difficulty.ALL.forEach { diff ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = diff.name,
                                        fontWeight = if (diff == currentDifficulty) FontWeight.Bold else FontWeight.Normal,
                                        color = if (diff == currentDifficulty) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${diff.moveTimeMs}ms • Depth ${diff.searchDepth}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                onDifficultyChanged(diff)
                                showDifficultyMenu = false
                            }
                        )
                    }
                }
            }

            // Theme selector Chip
            Box {
                IconButton(
                    onClick = { showThemeMenu = true },
                    modifier = Modifier.testTag("theme_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Board Theme",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = showThemeMenu,
                    onDismissRequest = { showThemeMenu = false }
                ) {
                    BoardTheme.ALL.forEach { theme ->
                        DropdownMenuItem(
                            leadingIcon = {
                                Row {
                                    Box(modifier = Modifier.size(14.dp).background(theme.lightSquare, CircleShape))
                                    Box(modifier = Modifier.size(14.dp).background(theme.darkSquare, CircleShape))
                                }
                            },
                            text = {
                                Text(
                                    text = theme.displayName,
                                    fontWeight = if (theme == currentTheme) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                onThemeChanged(theme)
                                showThemeMenu = false
                            }
                        )
                    }
                }
            }

            // New Game button
            IconButton(
                onClick = onNewGame,
                modifier = Modifier.testTag("new_game_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "New Game",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

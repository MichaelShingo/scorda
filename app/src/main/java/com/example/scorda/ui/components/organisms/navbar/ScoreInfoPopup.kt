package com.example.scorda.ui.components.organisms.navbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.scorda.data.database.relations.ScoreWithDetails
import com.example.scorda.util.getCommaSeparatedFullName

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScoreInfoPopup(
    scoreWithDetails: ScoreWithDetails,
    onEditClick: () -> Unit
) {
    val score = scoreWithDetails.score
    val composer = scoreWithDetails.composer
    val instruments = scoreWithDetails.instruments
    val genres = scoreWithDetails.genres
    val tags = scoreWithDetails.tags

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = score.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (composer != null) {
                    Text(
                        text = getCommaSeparatedFullName(composer),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            IconButton(
                onClick = onEditClick,
                modifier = Modifier
            ) {
                Icon(Icons.Rounded.Edit, contentDescription = "Edit Details")
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        if (score.keySignature != null) {
            InfoSection(label = "Key Signature", value = score.keySignature.toString())
        }

        if (instruments.isNotEmpty()) {
            InfoSectionLabel("Instruments")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                instruments.forEach { instrument ->
                    AssistChip(
                        onClick = {},
                        label = { Text(instrument.name) }
                    )
                }
            }
        }

        if (genres.isNotEmpty()) {
            InfoSectionLabel("Genres")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                genres.forEach { genre ->
                    AssistChip(
                        onClick = {},
                        label = { Text(genre.name) }
                    )
                }
            }
        }

        if (tags.isNotEmpty()) {
            InfoSectionLabel("Tags")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                tags.forEach { tag ->
                    AssistChip(
                        onClick = {},
                        label = { Text(tag.name) }
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoSectionLabel(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun InfoSection(label: String, value: String) {
    Column {
        InfoSectionLabel(label)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

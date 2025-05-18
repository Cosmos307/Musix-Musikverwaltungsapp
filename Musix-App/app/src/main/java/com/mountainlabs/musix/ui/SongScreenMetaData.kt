package com.mountainlabs.musix.ui

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.koin.androidx.compose.koinViewModel

//schwarze umrandung Herz

/*@Preview
@Composable
fun PreviewMetadata() {
    Metadata(
        onDismiss = {},
        onConfirm = {"Preview1", "Preview2", "[Preview4, Preview5]"}
    )
}*/
@Composable
fun Metadata(
    onDismiss:()->Unit,
    onConfirm:(String, String, String, List<String>)->Unit,
    viewModel: SongScreenViewModel = koinViewModel(),
) {
    val trackInfo by viewModel.trackInfo.collectAsState()
    var name by remember { mutableStateOf(trackInfo?.Name ?: "") }
    var interpret by remember { mutableStateOf(trackInfo?.ArtistName ?: "") }
    var duration by remember { mutableStateOf(trackInfo?.Duration ?: "") }
    var tags by remember { mutableStateOf(trackInfo?.Tags?.joinToString(", ") ?: "") }

    Dialog(
        onDismissRequest = {
            onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            shape = RoundedCornerShape(15.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 5.dp
            ),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .border(2.dp, color = Color.Black, shape = RoundedCornerShape(15.dp))
        ){

            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    placeholder = { Text("Gib den Namen an") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = duration.toString(),
                    onValueChange = { duration = it },
                    label = { Text("Duration") },
                    placeholder = { Text("Gib das Duration an") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags") },
                    placeholder = { Text("Gib die Tags an") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    // Cancel Button
                    Button(
                        onClick = {
                            onDismiss()
                            // Handle cancel action here
                        },
                        modifier = Modifier.weight(1f) // Hier wird der Button auf die Hälfte der Breite gesetzt
                    ) {
                        Text("Cancel")
                    }
                    // Submit Button
                    Button(
                        onClick = {
                            // Handle form submission here
                            val tagsList = tags.split(", ").map { it.trim() }
                            Log.w("Metadata", "Name: $name, Interpret: $interpret, Tags: $tagsList")
                            onConfirm(name, interpret, duration.toString(), tagsList)
                        },
                        modifier = Modifier.weight(1f) // Hier wird der Button auf die Hälfte der Breite gesetzt
                    ) {
                        Text("Submit")
                    }
                }
            }
        }
    }
}



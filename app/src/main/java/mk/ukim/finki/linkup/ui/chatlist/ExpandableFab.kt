package mk.ukim.finki.linkup.ui.chatlist

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*

@Composable
fun ExpandableFab(onCreateEvent: () -> Unit, onCreateGroup: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    FloatingActionButton(onClick = { expanded = !expanded }) {
        Icon(if (expanded) Icons.Default.Close else Icons.Default.Add, contentDescription = null)
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(text = { Text("Create Event") }, onClick = { expanded = false; onCreateEvent() })
        DropdownMenuItem(text = { Text("Create Group") }, onClick = { expanded = false; onCreateGroup() })
    }
}

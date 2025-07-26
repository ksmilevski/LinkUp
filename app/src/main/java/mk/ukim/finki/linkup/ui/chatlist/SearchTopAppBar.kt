package mk.ukim.finki.linkup.ui.chatlist

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SearchTopAppBar(query: String, onQueryChange: (String) -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search chats") },
                singleLine = true,
                colors = TextFieldDefaults.colors(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}

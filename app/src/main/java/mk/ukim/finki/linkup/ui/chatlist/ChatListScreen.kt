package mk.ukim.finki.linkup.ui.chatlist

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.runtime.remember
import androidx.compose.material3.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import mk.ukim.finki.linkup.repository.ChatListViewModel

@Composable
fun ChatListScreen(
    viewModel: ChatListViewModel,
    onCreateEvent: () -> Unit,
    onCreateGroup: () -> Unit
) {
    val chats by viewModel.chatRooms.collectAsState()
    val query = remember { viewModel.searchQuery }

    Scaffold(
        topBar = {
            SearchTopAppBar(query.value) { viewModel.searchQuery.value = it }
        },
        floatingActionButton = {
            ExpandableFab(onCreateEvent, onCreateGroup)
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            items(chats) { chat ->
                ChatCard(chat)
            }
        }
    }
}

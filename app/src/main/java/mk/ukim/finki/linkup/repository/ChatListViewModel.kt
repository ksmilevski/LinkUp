package mk.ukim.finki.linkup.repository

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mk.ukim.finki.linkup.models.ChatRoomModel
import mk.ukim.finki.linkup.utils.FirebaseUtil

class ChatListViewModel(private val repository: ChatRepository) : ViewModel() {
    val searchQuery = mutableStateOf("")
    private val _chatRooms = MutableStateFlow<List<ChatRoomModel>>(emptyList())
    val chatRooms: StateFlow<List<ChatRoomModel>> = _chatRooms

    fun loadChats() {
        val userId = FirebaseUtil.currentUserId() ?: return
        viewModelScope.launch {
            repository.getUserChatRoomsFlow(userId).collect { rooms ->
                _chatRooms.value = rooms
            }
        }
    }
}

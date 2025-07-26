package mk.ukim.finki.linkup.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mk.ukim.finki.linkup.models.ChatRoomModel
import mk.ukim.finki.linkup.models.UserModel
import mk.ukim.finki.linkup.utils.FirebaseUtil

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {

    private val _chatRoom = MutableLiveData<ChatRoomModel>()
    val chatRoom: LiveData<ChatRoomModel> = _chatRoom

    fun loadChatRoom(chatroomId: String, otherUser: UserModel?) {
        viewModelScope.launch {
            var chatRoom = repository.getChatRoom(chatroomId)
            if (chatRoom == null && otherUser != null) {
                val currentId = FirebaseUtil.currentUserId() ?: return@launch
                chatRoom = ChatRoomModel(
                    chatroomId = chatroomId,
                    userIds = listOf(currentId, otherUser.userId)
                )
                repository.createChatRoom(chatRoom)
            }
            chatRoom?.let { _chatRoom.postValue(it) }
        }
    }

    fun sendMessage(chatroomId: String, message: String) {
        viewModelScope.launch {
            val senderId = FirebaseUtil.currentUserId() ?: return@launch
            repository.sendMessage(chatroomId, message, senderId)
        }
    }
}

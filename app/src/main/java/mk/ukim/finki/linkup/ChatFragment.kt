package mk.ukim.finki.linkup

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import mk.ukim.finki.linkup.repository.ChatListViewModel
import mk.ukim.finki.linkup.repository.ChatListViewModelFactory
import mk.ukim.finki.linkup.repository.ChatRepository
import mk.ukim.finki.linkup.ui.chatlist.ChatListScreen
import mk.ukim.finki.linkup.ui.theme.LinkUpTheme

class ChatFragment : Fragment() {

    private val viewModel: ChatListViewModel by viewModels {
        ChatListViewModelFactory(ChatRepository())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel.loadChats()
        return ComposeView(requireContext()).apply {
            setContent {
                LinkUpTheme {
                    ChatListScreen(
                        viewModel = viewModel,
                        onCreateEvent = {
                            startActivity(Intent(context, CreateEventActivity::class.java))
                        },
                        onCreateGroup = {
                            startActivity(Intent(context, CreateGroupActivity::class.java))
                        }
                    )
                }
            }
        }
    }
}

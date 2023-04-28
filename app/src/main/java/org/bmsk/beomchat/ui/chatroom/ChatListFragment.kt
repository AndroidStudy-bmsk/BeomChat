package org.bmsk.beomchat.ui.chatroom

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.bmsk.beomchat.R
import org.bmsk.beomchat.databinding.FragmentChatListBinding

class ChatListFragment : Fragment(R.layout.fragment_chat_list) {
    private lateinit var binding: FragmentChatListBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentChatListBinding.bind(view)

        val chatListAdapter = ChatListAdapter()
        binding.chatListRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatListAdapter
        }
    }
}
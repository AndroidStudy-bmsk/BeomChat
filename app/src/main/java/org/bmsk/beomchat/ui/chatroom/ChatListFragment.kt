package org.bmsk.beomchat.ui.chatroom

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.bmsk.beomchat.R
import org.bmsk.beomchat.data.db.Key.Companion.DB_CHAT_ROOMS
import org.bmsk.beomchat.data.db.Key.Companion.DB_URL
import org.bmsk.beomchat.data.db.Key.Companion.PUT_EXTRA_CHAT_ROOM_ID
import org.bmsk.beomchat.data.db.Key.Companion.PUT_EXTRA_OTHER_USER_ID
import org.bmsk.beomchat.data.model.ChatRoomItem
import org.bmsk.beomchat.databinding.FragmentChatListBinding
import org.bmsk.beomchat.ui.chatdetail.ChatActivity

class ChatListFragment : Fragment(R.layout.fragment_chat_list) {
    private lateinit var binding: FragmentChatListBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChatListBinding.bind(view)

        setupChatListAdapter()
        fetchChatRooms()
    }

    private fun setupChatListAdapter() {
        val chatListAdapter = ChatListAdapter { chatRoomItem ->
            val intent = Intent(context, ChatActivity::class.java).apply {
                putExtra(PUT_EXTRA_CHAT_ROOM_ID, chatRoomItem.chatRoomId)
                putExtra(PUT_EXTRA_OTHER_USER_ID, chatRoomItem.otherUserId)
            }
            startActivity(intent)
        }

        binding.chatListRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatListAdapter
        }
    }

    private fun fetchChatRooms() {
        val currentUserId = Firebase.auth.currentUser?.uid ?: return
        val chatRoomsDB = Firebase.database(DB_URL).reference
            .child(DB_CHAT_ROOMS)
            .child(currentUserId)
        chatRoomsDB.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatRoomList = snapshot.children.map {
                    it.getValue(ChatRoomItem::class.java)
                }
                (binding.chatListRecyclerView.adapter as? ChatListAdapter)?.submitList(chatRoomList)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
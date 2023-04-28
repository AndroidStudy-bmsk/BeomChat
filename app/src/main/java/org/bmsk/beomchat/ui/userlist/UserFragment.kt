package org.bmsk.beomchat.ui.userlist

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
import com.google.firebase.ktx.app
import org.bmsk.beomchat.R
import org.bmsk.beomchat.data.db.Key.Companion.DB_CHAT_ROOMS
import org.bmsk.beomchat.data.db.Key.Companion.DB_URL
import org.bmsk.beomchat.data.db.Key.Companion.DB_USERS
import org.bmsk.beomchat.data.db.Key.Companion.PUT_EXTRA_CHAT_ROOM_ID
import org.bmsk.beomchat.data.db.Key.Companion.PUT_EXTRA_OTHER_USER_ID
import org.bmsk.beomchat.data.model.ChatRoomItem
import org.bmsk.beomchat.data.model.UserItem
import org.bmsk.beomchat.databinding.FragmentUserBinding
import org.bmsk.beomchat.ui.chatdetail.ChatActivity
import java.util.UUID

class UserFragment : Fragment(R.layout.fragment_user) {

    private lateinit var binding: FragmentUserBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentUserBinding.bind(view)

        val userAdapter = UserAdapter { otherUser ->
            val myUserId = Firebase.auth.currentUser?.uid ?: ""
            val chatRoomDB =
                Firebase.database(DB_URL).reference.child(DB_CHAT_ROOMS).child(myUserId)
                    .child(otherUser.userId ?: "")

            chatRoomDB.get().addOnSuccessListener {
                var chatRoomId = ""
                if (it.value != null) {
                    val chatRoom = it.getValue(ChatRoomItem::class.java)
                    chatRoomId = chatRoom?.chatRoomId ?: ""
                } else {
                    chatRoomId = UUID.randomUUID().toString()
                    val newChatRoom = ChatRoomItem(
                        chatRoomId = chatRoomId,
                        otherUserId = otherUser.userId,
                        otherUserName = otherUser.userName
                    )
                    chatRoomDB.setValue(newChatRoom)
                }

                val intent = Intent(context, ChatActivity::class.java).apply {
                    putExtra(PUT_EXTRA_OTHER_USER_ID, otherUser.userId)
                    putExtra(PUT_EXTRA_CHAT_ROOM_ID, chatRoomId)
                }

                startActivity(intent)
            }
        }

        binding.userListRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = userAdapter
        }

        val currentUserId = Firebase.auth.currentUser?.uid ?: ""

        Firebase.database(DB_URL).reference
            .child(DB_USERS)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val userItemList = mutableListOf<UserItem>()

                    snapshot.children.forEach {
                        val user = it.getValue(UserItem::class.java)
                        user ?: return

                        if (user.userId != currentUserId) {
                            userItemList.add(user)
                        }
                    }

                    userAdapter.submitList(userItemList)
                }

                override fun onCancelled(error: DatabaseError) {
                }
            }
            )
    }
}
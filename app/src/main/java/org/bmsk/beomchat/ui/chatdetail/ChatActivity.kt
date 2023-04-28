package org.bmsk.beomchat.ui.chatdetail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.bmsk.beomchat.data.db.Key.Companion.DB_CHATS
import org.bmsk.beomchat.data.db.Key.Companion.DB_CHAT_ROOMS
import org.bmsk.beomchat.data.db.Key.Companion.DB_CHAT_ROOM_ID
import org.bmsk.beomchat.data.db.Key.Companion.DB_LAST_MESSAGE
import org.bmsk.beomchat.data.db.Key.Companion.DB_OTHER_USER_ID
import org.bmsk.beomchat.data.db.Key.Companion.DB_OTHER_USER_KEY
import org.bmsk.beomchat.data.db.Key.Companion.DB_OTHER_USER_NAME
import org.bmsk.beomchat.data.db.Key.Companion.DB_URL
import org.bmsk.beomchat.data.db.Key.Companion.DB_USERS
import org.bmsk.beomchat.data.db.Key.Companion.PUT_EXTRA_CHAT_ROOM_ID
import org.bmsk.beomchat.data.db.Key.Companion.PUT_EXTRA_OTHER_USER_ID
import org.bmsk.beomchat.data.model.ChatItem
import org.bmsk.beomchat.data.model.UserItem
import org.bmsk.beomchat.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding

    private var chatRoomId: String = ""
    private var otherUserId: String = ""
    private var myUserId: String = ""
    private var myUserName: String = ""

    private val chatItemList = mutableListOf<ChatItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val chatAdapter = ChatAdapter()

        chatRoomId = intent.getStringExtra(PUT_EXTRA_CHAT_ROOM_ID) ?: return
        otherUserId = intent.getStringExtra(PUT_EXTRA_OTHER_USER_ID) ?: return
        myUserId = Firebase.auth.currentUser?.uid ?: ""

        Firebase.database(DB_URL).reference.child(DB_USERS).child(myUserId).get()
            .addOnSuccessListener {
                val myUserItem = it.getValue(UserItem::class.java)
                myUserName = myUserItem?.userName ?: ""
            }
        Firebase.database(DB_URL).reference.child(DB_USERS).child(otherUserId).get()
            .addOnSuccessListener {
                val otherUserItem = it.getValue(UserItem::class.java)

                chatAdapter.otherUserItem = otherUserItem
            }

        Firebase.database(DB_URL).reference.child(DB_CHATS).child(chatRoomId)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val chatItem = snapshot.getValue(ChatItem::class.java)
                    chatItem ?: return

                    chatItemList.add(chatItem)

                    chatAdapter.submitList(chatItemList)
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })

        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatAdapter
        }

        binding.sendButton.setOnClickListener {
            val message = binding.messageEditText.text.toString()

            if (message.isEmpty()) {
                return@setOnClickListener
            }

            val newChatItem = ChatItem(
                message = message,
                userId = myUserId,
            )

            Firebase.database(DB_URL).reference.child(DB_CHATS).child(chatRoomId).push().apply {
                newChatItem.chatId = key
                setValue(newChatItem)
            }

            val updates: MutableMap<String, Any> = hashMapOf(
                "$DB_CHAT_ROOMS/$myUserId/$otherUserId/$DB_LAST_MESSAGE" to message,
                "$DB_CHAT_ROOMS/$otherUserId/$myUserId/$DB_LAST_MESSAGE" to message,
                "$DB_CHAT_ROOMS/$otherUserId/$myUserId/$DB_CHAT_ROOM_ID" to chatRoomId,
                "$DB_CHAT_ROOMS/$otherUserId/$myUserId/$DB_OTHER_USER_ID" to myUserId,
                "$DB_CHAT_ROOMS/$otherUserId/$myUserId/$DB_OTHER_USER_NAME" to myUserName,
            )

            Firebase.database(DB_URL).reference.updateChildren(updates)

            binding.messageEditText.text.clear()
        }
    }
}
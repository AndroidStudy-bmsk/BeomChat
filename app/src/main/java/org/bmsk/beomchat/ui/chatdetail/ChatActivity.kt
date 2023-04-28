package org.bmsk.beomchat.ui.chatdetail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.bmsk.beomchat.R
import org.bmsk.beomchat.data.db.Key.Companion.DB_CHATS
import org.bmsk.beomchat.data.db.Key.Companion.DB_CHAT_ROOMS
import org.bmsk.beomchat.data.db.Key.Companion.DB_CHAT_ROOM_ID
import org.bmsk.beomchat.data.db.Key.Companion.DB_LAST_MESSAGE
import org.bmsk.beomchat.data.db.Key.Companion.DB_OTHER_USER_ID
import org.bmsk.beomchat.data.db.Key.Companion.DB_OTHER_USER_NAME
import org.bmsk.beomchat.data.db.Key.Companion.DB_URL
import org.bmsk.beomchat.data.db.Key.Companion.DB_USERS
import org.bmsk.beomchat.data.db.Key.Companion.PUT_EXTRA_CHAT_ROOM_ID
import org.bmsk.beomchat.data.db.Key.Companion.PUT_EXTRA_OTHER_USER_ID
import org.bmsk.beomchat.data.model.ChatItem
import org.bmsk.beomchat.data.model.UserItem
import org.bmsk.beomchat.databinding.ActivityChatBinding
import org.json.JSONObject
import java.io.IOException

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager

    private var chatRoomId: String = ""
    private var otherUserId: String = ""
    private var otherUserFcmToken: String = ""
    private var myUserId: String = ""
    private var myUserName: String = ""
    private var isInit = false

    private val chatItemList = mutableListOf<ChatItem>()
    private val okHttpClient = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatAdapter = ChatAdapter()
        linearLayoutManager = LinearLayoutManager(applicationContext)

        chatRoomId = intent.getStringExtra(PUT_EXTRA_CHAT_ROOM_ID) ?: return
        otherUserId = intent.getStringExtra(PUT_EXTRA_OTHER_USER_ID) ?: return

        myUserId = Firebase.auth.currentUser?.uid ?: ""

        initFirebase()
        initChatRecyclerView()
        initSendButton()
    }

    private fun initFirebase() {
        Firebase.database(DB_URL).reference.child(DB_USERS).child(myUserId).get()
            .addOnSuccessListener {
                val myUserItem = it.getValue(UserItem::class.java)
                myUserName = myUserItem?.userName ?: ""

                getOtherUserData()
            }
    }

    private fun getOtherUserData() {
        Firebase.database(DB_URL).reference.child(DB_USERS).child(otherUserId).get()
            .addOnSuccessListener {
                val otherUserItem = it.getValue(UserItem::class.java)
                otherUserFcmToken = otherUserItem?.fcmToken.orEmpty()
                chatAdapter.otherUserItem = otherUserItem

                isInit = true
                getChatData()
            }
    }

    private fun getChatData() {
        Firebase.database(DB_URL).reference.child(DB_CHATS).child(chatRoomId)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val chatItem = snapshot.getValue(ChatItem::class.java)
                    chatItem ?: return

                    chatItemList.add(chatItem)
                    chatAdapter.submitList(chatItemList.toMutableList())
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
    }

    private fun initChatRecyclerView() {
        chatAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)

                linearLayoutManager.smoothScrollToPosition(
                    binding.chatRecyclerView,
                    null,
                    chatAdapter.itemCount
                )
            }
        })

        binding.chatRecyclerView.apply {
            layoutManager = linearLayoutManager
            adapter = chatAdapter
        }
    }

    private fun initSendButton() {
        binding.sendButton.setOnClickListener {
            val message = binding.messageEditText.text.toString()

            if (message.isEmpty() || !isInit) {
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

            val root = JSONObject()
            val notification = JSONObject()
            notification.put("title", getString(R.string.app_name))
            notification.put("body", message)

            root.put("to", otherUserFcmToken)
            root.put("priority", "high")
            root.put("notification", notification)

            val requestBody =
                root.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request =
                Request.Builder().post(requestBody).url(FCM_SEND_URL)
                    .header("Authorization", "key=${getString(R.string.fcm_server_key)}")
                    .build()
            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.stackTraceToString()
                }

                override fun onResponse(call: Call, response: Response) {
                }
            })

            binding.messageEditText.text.clear()
        }
    }

    companion object {
        const val FCM_SEND_URL = "https://fcm.googleapis.com/fcm/send"
    }
}
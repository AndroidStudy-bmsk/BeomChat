package org.bmsk.beomchat.ui.chatdetail

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.bmsk.beomchat.data.model.ChatItem
import org.bmsk.beomchat.data.model.UserItem
import org.bmsk.beomchat.databinding.ItemChatBinding

class ChatAdapter : ListAdapter<ChatItem, ChatAdapter.ViewHolder>(diffUtil) {
    var otherUserItem: UserItem? = null

    inner class ViewHolder(private val binding: ItemChatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChatItem) {
            if (item.userId == otherUserItem?.userId) {
                binding.userNameTextView.isVisible = true
                binding.root.alpha = 0.6f
                binding.userNameTextView.text = otherUserItem?.userName
                binding.messageTextView.text = item.message
                binding.messageTextView.gravity = Gravity.START
            } else {
                binding.userNameTextView.isVisible = false
                binding.root.alpha = 1f
                binding.messageTextView.text = item.message
                binding.messageTextView.gravity = Gravity.END
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemChatBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<ChatItem>() {
            override fun areItemsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
                return oldItem.chatId == newItem.chatId
            }

            override fun areContentsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}
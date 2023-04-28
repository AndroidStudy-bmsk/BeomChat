package org.bmsk.beomchat.ui.mypage

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.bmsk.beomchat.R
import org.bmsk.beomchat.data.db.Key.Companion.DB_URL
import org.bmsk.beomchat.data.db.Key.Companion.DB_USERS
import org.bmsk.beomchat.data.db.Key.Companion.DB_USER_DESCRIPTION
import org.bmsk.beomchat.data.db.Key.Companion.DB_USER_NAME
import org.bmsk.beomchat.data.model.UserItem
import org.bmsk.beomchat.databinding.FragmentMyPageBinding
import org.bmsk.beomchat.ui.SignInActivity

class MyPageFragment : Fragment(R.layout.fragment_my_page) {
    private lateinit var binding: FragmentMyPageBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMyPageBinding.bind(view)

        val currentUserId = Firebase.auth.currentUser?.uid ?: ""
        val currentUserDB =
            Firebase.database(DB_URL).reference.child(DB_USERS).child(currentUserId)
        currentUserDB.get().addOnSuccessListener {
            val currentUserItem = it.getValue(UserItem::class.java) ?: return@addOnSuccessListener

            binding.usernameEditText.setText(currentUserItem.userName)
            binding.descriptionTextView.setText(currentUserItem.description)
        }

        binding.applyButton.setOnClickListener {
            val userName = binding.usernameEditText.text.toString()
            val description = binding.descriptionTextView.text.toString()

            if (userName.isEmpty()) {
                Toast.makeText(view.context, "유저 이름은 빈 값으로 두실 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = mutableMapOf<String, Any>()
            user[DB_USER_NAME] = userName
            user[DB_USER_DESCRIPTION] = description

            currentUserDB.updateChildren(user)
        }
        binding.signOutButton.setOnClickListener {
            Firebase.auth.signOut()
            startActivity(Intent(view.context, SignInActivity::class.java))
            activity?.finish()
        }
    }
}
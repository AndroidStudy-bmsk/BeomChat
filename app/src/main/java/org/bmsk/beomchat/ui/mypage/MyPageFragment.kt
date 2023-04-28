package org.bmsk.beomchat.ui.mypage

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.bmsk.beomchat.R
import org.bmsk.beomchat.databinding.FragmentMyPageBinding
import org.bmsk.beomchat.ui.SignInActivity

class MyPageFragment : Fragment(R.layout.fragment_my_page) {
    private lateinit var binding: FragmentMyPageBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentMyPageBinding.bind(view)
        binding.applyButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString()
            val description = binding.descriptionTextView.text.toString()

            if (username.isEmpty()) {
                Toast.makeText(view.context, "유저 이름은 빈 값으로 두실 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // TODO realtiem database update
        }
        binding.signOutButton.setOnClickListener {
            Firebase.auth.signOut()
            startActivity(Intent(view.context, SignInActivity::class.java))
            activity?.finish()
        }
    }
}
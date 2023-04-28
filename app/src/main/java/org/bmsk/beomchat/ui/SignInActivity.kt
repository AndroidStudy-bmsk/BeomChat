package org.bmsk.beomchat.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import org.bmsk.beomchat.R
import org.bmsk.beomchat.data.db.Key.Companion.DB_FCM_TOKEN
import org.bmsk.beomchat.data.db.Key.Companion.DB_URL
import org.bmsk.beomchat.data.db.Key.Companion.DB_USERS
import org.bmsk.beomchat.data.db.Key.Companion.DB_USER_ID
import org.bmsk.beomchat.data.db.Key.Companion.DB_USER_NAME
import org.bmsk.beomchat.databinding.ActivitySignInBinding

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signInButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, R.string.email_or_password_not_input, Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            Firebase.auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    val currentUser = Firebase.auth.currentUser
                    if (task.isSuccessful && currentUser != null) {
                        val userId = currentUser.uid

                        Firebase.messaging.token.addOnCompleteListener {
                            val token = it.result
                            val user = mutableMapOf<String, Any>()
                            user[DB_USER_ID] = userId
                            user[DB_USER_NAME] = email
                            user[DB_FCM_TOKEN] = token

                            Firebase.database(DB_URL).reference
                                .child(DB_USERS)
                                .child(userId)
                                .updateChildren(user)

                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }.addOnFailureListener {
                            it.printStackTrace()
                        }
                    } else {
                        Log.e("SignInActivity", task.exception.toString())
                        Toast.makeText(this, R.string.fail_sign_in, Toast.LENGTH_SHORT).show()
                    }
                }
        }
        binding.signUpButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, R.string.email_or_password_not_input, Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            Firebase.auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, R.string.successful_sign_up, Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(this, R.string.fail_sign_up, Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
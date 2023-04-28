# BeomChat
- Firebase 사용해보기

## Firebase

- Firebase는 Google이 제공하는 클라우드 기반의 개발 플랫폼으로, 안드로이드, iOS, 웹 어플리케이션 개발에 사용된다.
- Firebase는 개발자들이 빠르게 애플리케이션을 구축하고, 성능을 개선하며, 사용자를 관리하고 사용자 경험을 분석할 수 있도록 다양한 기능을 제공한다.

### 주요 기능

1. **실시간 데이터베이스(Realtime Database)**: 클라우드에 호스팅되는 NoSQL 데이터베이스로 실시간으로 데이터를 동기화하며 오프라인에서도 작동한다.
2. **인증(Authentication)**: 이메일, 비밀번호, 소셜 미디어 계정, 전화번호 등을 통해 사용자 인증을 쉽게 구현할 수 있다.
3. **클라우드 메시징(Cloud Messaging)**: 무료로 푸시 알림을 보낼 수 있는 서비스를 제공
4. 스토리지(Storage): 이미지, 동영상, 파일 등의 콘텐츠를 안전하게 저장하고 관리할 수 있는 클라우드 저장소.
5. 호스팅(Hosting): 웹 앱에 대한 정적 및 동적 호스팅을 제공한다.
6. 원격 구성(Remote Config): 앱의 동작과 모양을 원격으로 변경할 수 있는 기능.
7. 테스트 랩(Test Lab): 다양한 기기 및 환경에서 앱을 테스트할 수 있게 한다.
8. Crashlytics: 앱의 실시간 충돌 보고 및 분석을 제공한다.
9. Google Analytics: 사용자 행동 분석을 위한 데이터를 수집 및 처리한다.
10. Performance Monitoring: 앱의 성능 문제를 감지하고 개선할 수 있도록 도와주는 도구.

## Firebase Realtime Database

## Firebase Authentication

https://firebase.google.com/docs/auth/android/password-auth?hl=ko&authuser=0

```kotlin
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
            if (task.isSuccessful) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Log.e("SignInActitivy", task.exception.toString())
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
                // 회원가입 성공
                Toast.makeText(this, R.string.successful_sign_up, Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(this, R.string.fail_sign_up, Toast.LENGTH_SHORT).show()
            }
        }
}
```

## Firebase cloud Message (FCM)
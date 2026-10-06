package com.example.classlink;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

// 👇 New imports for Firebase Cloud Messaging and Firestore
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class LoginActivity extends AppCompatActivity {

    private EditText emailTextView, passwordTextView;
    private Button loginButton;
    private TextView switchToRegisterTextView;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize FirebaseAuth instance
        auth = FirebaseAuth.getInstance();

        emailTextView = findViewById(R.id.emailedittext);
        passwordTextView = findViewById(R.id.passwordedittext);
        loginButton = findViewById(R.id.loginbutton);
        switchToRegisterTextView = findViewById(R.id.switchToRegister);

        loginButton.setOnClickListener(v -> loginUserAccount());

        // "Not yet registered? Sign Up"
        switchToRegisterTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginUserAccount() {
        String email = emailTextView.getText().toString().trim();
        String password = passwordTextView.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter credentials", Toast.LENGTH_LONG).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null && user.isEmailVerified()) {
                                // ✅ Email verified
                                Toast.makeText(LoginActivity.this, "✅Login successful!!", Toast.LENGTH_LONG).show();

                                // 👇 NEW CODE: Fetch user's class and subscribe to FCM topic
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                db.collection("users").document(user.getUid()).get()
                                        .addOnCompleteListener(task_class -> {
                                            if (task_class.isSuccessful()) {
                                                DocumentSnapshot document = task_class.getResult();
                                                if (document.exists()) {
                                                    // Assumes a 'class' field exists in the user document
                                                    String userClass = document.getString("class");
                                                    if (userClass != null) {
                                                        // Convert class name to a topic-friendly format
                                                        String topicName = userClass.replace(" ", "_").toLowerCase();
                                                        FirebaseMessaging.getInstance().subscribeToTopic(topicName);
                                                    }
                                                }
                                            }
                                        });

                                startActivity(new Intent(LoginActivity.this, ClassjoinadActivity.class));
                                finish();
                            } else {
                                // ❌ Email not verified
                                Toast.makeText(LoginActivity.this, "Please verify your email before login", Toast.LENGTH_LONG).show();
                                auth.signOut(); // prevent unverified user from staying logged in
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "❌Login failed!!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}



/* working
package com.example.classlink;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;



public class LoginActivity extends AppCompatActivity {

    private EditText emailTextView, passwordTextView;
    private Button loginButton;
    private TextView switchToRegisterTextView;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize FirebaseAuth instance
        auth = FirebaseAuth.getInstance();

        emailTextView = findViewById(R.id.emailedittext);
        passwordTextView = findViewById(R.id.passwordedittext);
        loginButton = findViewById(R.id.loginbutton);
        switchToRegisterTextView = findViewById(R.id.switchToRegister);

        loginButton.setOnClickListener(v -> loginUserAccount());

        // "Not yet registered? Sign Up"
        switchToRegisterTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginUserAccount() {
        String email = emailTextView.getText().toString().trim();
        String password = passwordTextView.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter credentials", Toast.LENGTH_LONG).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null && user.isEmailVerified()) {
                                // ✅ Email verified
                                Toast.makeText(LoginActivity.this, "✅Login successful!!", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(LoginActivity.this, ClassjoinadActivity.class));
                                finish();
                            } else {
                                // ❌ Email not verified
                                Toast.makeText(LoginActivity.this, "Please verify your email before login", Toast.LENGTH_LONG).show();
                                auth.signOut(); // prevent unverified user from staying logged in
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "❌Login failed!!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}*/

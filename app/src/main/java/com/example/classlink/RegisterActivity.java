


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

public class RegisterActivity extends AppCompatActivity {

    private EditText emailTextView, passwordTextView;
    private Button registerButton;
    private TextView switchToLoginTextView;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize FirebaseAuth instance
        auth = FirebaseAuth.getInstance();

        emailTextView = findViewById(R.id.emailedittext);
        passwordTextView = findViewById(R.id.passwordedittext);
        registerButton = findViewById(R.id.registerbutton);
        switchToLoginTextView = findViewById(R.id.switchToLogin);

        registerButton.setOnClickListener(v -> registerNewUser());

        // Switch to Login
        switchToLoginTextView.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void registerNewUser() {
        String email = emailTextView.getText().toString().trim();
        String password = passwordTextView.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter credentials", Toast.LENGTH_LONG).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                // send verification email
                                user.sendEmailVerification().addOnCompleteListener(verifyTask -> {
                                    if (verifyTask.isSuccessful()) {
                                        Toast.makeText(RegisterActivity.this,
                                                "Registration successful! Please verify your email before login.",
                                                Toast.LENGTH_LONG).show();

                                        // log out user until verified
                                        auth.signOut();

                                        // redirect to login
                                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(RegisterActivity.this,
                                                "Failed to send verification email.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(RegisterActivity.this,
                                    "Registration failed! " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}


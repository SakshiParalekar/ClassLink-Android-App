package com.example.classlink;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

public class OnboardingActivity extends AppCompatActivity {

    ImageButton nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        nextButton = findViewById(R.id.nextButton);

        nextButton.setOnClickListener(v -> {
            Intent intent = new Intent(OnboardingActivity.this, RegisterActivity.class); // Next screen
            startActivity(intent);
            finish(); // Optional if you don't want user to return
        });
    }
}
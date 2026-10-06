/*
package com.example.classlink;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView appNameText;
    private ImageView appLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appLogo = findViewById(R.id.imageView);
        appNameText = findViewById(R.id.textView);


        Animation moveUp = AnimationUtils.loadAnimation(this, R.anim.move_up);

        moveUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // nothing
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // ✅ Open login page immediately after animation
                Intent intent = new Intent(MainActivity.this, OnboardingActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // nothing
            }
        });

        appLogo.startAnimation(moveUp);
        appNameText.startAnimation(moveUp);
    }
}*/



/*working*/
package com.example.classlink;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView appNameText;
    private ImageView appLogo;
    private static final int DELAY_AFTER_ANIMATION = 1000; // 5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appLogo = findViewById(R.id.imageView);
        appNameText = findViewById(R.id.textView);

        Animation moveUp = AnimationUtils.loadAnimation(this, R.anim.move_up);

        moveUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // nothing
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // ⏳ Wait 5 seconds after animation ends
                new Handler().postDelayed(() -> {
                    Intent intent = new Intent(MainActivity.this, OnboardingActivity.class);
                    startActivity(intent);
                    finish();
                }, DELAY_AFTER_ANIMATION);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // nothing
            }
        });

        appLogo.startAnimation(moveUp);
        appNameText.startAnimation(moveUp);
    }
}

package com.example.classlink;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ClassjoinadActivity extends AppCompatActivity {

    private View animCircle;
    private Button btnGetStarted;
    private Button btnSkip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classjoinad);

        animCircle = findViewById(R.id.animCircle);
        btnGetStarted = findViewById(R.id.btnGetStarted);
        btnSkip = findViewById(R.id.btnSkip);


        btnGetStarted.setOnClickListener(v -> {
            animCircle.setVisibility(View.VISIBLE);

            // Animate without moving layout (use scale + translation overlay)
            animCircle.post(() -> {
                // Pivot from bottom center
                animCircle.setPivotX(animCircle.getWidth() / 2f);
                animCircle.setPivotY(animCircle.getHeight());

                animCircle.setScaleX(1f);
                animCircle.setScaleY(1f);

                animCircle.animate()
                        .scaleX(20f) // bigger expansion
                        .scaleY(20f)
                        .setDuration(1200) // slower animation
                        .withEndAction(() -> {
                            startActivity(new Intent(ClassjoinadActivity.this, CreateClassActivity.class));
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        })
                        .start();
            });
        });

        btnSkip.setOnClickListener(v -> {
            Intent intent = new Intent(ClassjoinadActivity.this, CreateClassActivity.class); // Next screen
            startActivity(intent);
            finish();
        });
    }
}





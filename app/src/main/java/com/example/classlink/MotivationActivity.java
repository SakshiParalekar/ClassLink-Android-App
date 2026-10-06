package com.example.classlink;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class MotivationActivity extends AppCompatActivity {

    TextView tvSuggestion;
    Button btnTired, btnBored, btnMotivated, btnStressed;
    Random random = new Random();

    String[] tiredSuggestions = {
            "Start with the easiest note first 📖",
            "Take a 5-min stretch break 💪 then continue study",
            "Drink some water and refresh 💧 and review a short summary",
    };

    String[] boredSuggestions = {
            "Try reading one short note 📘",
            "Do a quick revision challenge 📝",
            "Change subject for a fresh start 🔄",
            "Listen to calm music for 2 min 🎶"
    };

    String[] motivatedSuggestions = {
            "Great! Try finishing 2 notes today 🚀",
            "Revise yesterday's notes quickly 🔄",
            "Summarize one note in your own words 📝",
            "Help a friend by sharing notes 🤝"
    };

    String[] stressedSuggestions = {
            "Take a deep breath 🌿",
            "Focus on one note at a time 🎯",
            "Write down a small to-do list ✍️"

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motivation);

        tvSuggestion = findViewById(R.id.tvSuggestion);
        btnTired = findViewById(R.id.btnTired);
        btnBored = findViewById(R.id.btnBored);
        btnMotivated = findViewById(R.id.btnMotivated);
        btnStressed = findViewById(R.id.btnStressed);

        btnTired.setOnClickListener(v -> showSuggestion(tiredSuggestions));
        btnBored.setOnClickListener(v -> showSuggestion(boredSuggestions));
        btnMotivated.setOnClickListener(v -> showSuggestion(motivatedSuggestions));
        btnStressed.setOnClickListener(v -> showSuggestion(stressedSuggestions));
    }

    private void showSuggestion(String[] suggestions) {
        int index = random.nextInt(suggestions.length);
        tvSuggestion.setText(suggestions[index]);
    }
}

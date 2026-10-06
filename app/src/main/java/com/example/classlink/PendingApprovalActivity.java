package com.example.classlink;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;

public class PendingApprovalActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_approval);

        TextView text = findViewById(R.id.pendingText);
        text.setText("Your account is awaiting admin approval.\nPlease try again later.");
    }
}

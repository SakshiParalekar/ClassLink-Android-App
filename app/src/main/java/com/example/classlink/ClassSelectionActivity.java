package com.example.classlink;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.Button; // Add this import
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClassSelectionActivity extends AppCompatActivity {

    private ListView classListView;
    private ArrayAdapter<String> classAdapter;
    private List<String> classNames;
    private HashMap<String, String> classNameToIdMap;

    private FirebaseAuth mAuth;
    private DatabaseReference database;
    private Button joinClassButton; // New: Button for joining a class

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_selection);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase
                .getInstance("https://claink-49383-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference();

        classListView = findViewById(R.id.classListView);
        joinClassButton = findViewById(R.id.joinClassButton); // New: Find the button
        classNames = new ArrayList<>();
        classNameToIdMap = new HashMap<>();

        classAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, classNames);
        classListView.setAdapter(classAdapter);

        loadUserClasses();

        classListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedClassName = classNames.get(position);
                String selectedClassId = classNameToIdMap.get(selectedClassName);

                if (selectedClassId != null) {
                    Intent intent = new Intent(ClassSelectionActivity.this, DashboardActivity.class);
                    intent.putExtra("classId", selectedClassId);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(ClassSelectionActivity.this, "Error: Class ID not found.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // New: Set a click listener for the join class button
        joinClassButton.setOnClickListener(v -> {
            Intent intent = new Intent(ClassSelectionActivity.this, CreateClassActivity.class);
            startActivity(intent);
        });
    }

    private void loadUserClasses() {
        String currentUserEmail = mAuth.getCurrentUser().getEmail();
        if (currentUserEmail == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        database.child("classes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                classNames.clear();
                classNameToIdMap.clear();

                for (DataSnapshot classSnapshot : snapshot.getChildren()) {
                    String classId = classSnapshot.getKey();
                    String className = classSnapshot.child("className").getValue(String.class);
                    String adminEmail = classSnapshot.child("adminEmail").getValue(String.class);

                    boolean isMember = false;
                    DataSnapshot membersSnapshot = classSnapshot.child("members");
                    if (membersSnapshot.exists()) {
                        for (DataSnapshot memberSnapshot : membersSnapshot.getChildren()) {
                            if (currentUserEmail.equals(memberSnapshot.getValue(String.class))) {
                                isMember = true;
                                break;
                            }
                        }
                    }

                    if (className != null && (adminEmail != null && adminEmail.equals(currentUserEmail) || isMember)) {
                        classNames.add(className);
                        classNameToIdMap.put(className, classId);
                    }
                }
                if (classNames.isEmpty()) {
                    Toast.makeText(ClassSelectionActivity.this, "You have not created or joined any classes.", Toast.LENGTH_LONG).show();
                }
                classAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ClassSelectionActivity.this, "Failed to load classes.", Toast.LENGTH_SHORT).show();
                Log.e("ClassSelectionActivity", "Failed to load classes.", error.toException());
            }
        });
    }
}

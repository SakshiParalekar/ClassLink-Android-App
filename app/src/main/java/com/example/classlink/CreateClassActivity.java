
package com.example.classlink;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.Random;

public class CreateClassActivity extends AppCompatActivity {









    EditText classNameEditText, joinCodeEditText;
    Button createClassButton, joinButton, shareCodeButton; // ✅ NEW
    TextView generatedCodeText;

    DatabaseReference database;
    FirebaseAuth mAuth;

    String latestClassCode = ""; // ✅ NEW (store last generated code)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_class);

        classNameEditText = findViewById(R.id.classNameEditText);
        joinCodeEditText = findViewById(R.id.joinCodeEditText);
        createClassButton = findViewById(R.id.createClassButton);
        joinButton = findViewById(R.id.joinButton);
        generatedCodeText = findViewById(R.id.generatedCodeText);
        shareCodeButton = findViewById(R.id.shareCodeButton); // ✅ NEW

        mAuth = FirebaseAuth.getInstance();

        database = FirebaseDatabase
                .getInstance("https://claink-49383-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("classes");

        // ✅ Create new class
        createClassButton.setOnClickListener(v -> {
            String className = classNameEditText.getText().toString().trim();

            if (TextUtils.isEmpty(className)) {
                classNameEditText.setError("Please enter class name");
                return;
            }

            database.orderByChild("className").equalTo(className)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Toast.makeText(CreateClassActivity.this, "Class already exists!", Toast.LENGTH_SHORT).show();
                            } else {
                                createNewClass(className);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(CreateClassActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // ✅ Join existing class
        joinButton.setOnClickListener(v -> {
            String enteredCode = joinCodeEditText.getText().toString().trim();
            if (TextUtils.isEmpty(enteredCode)) {
                joinCodeEditText.setError("Please enter class code");
                return;
            }

            database.orderByChild("classCode").equalTo(enteredCode)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot snap : snapshot.getChildren()) {
                                    String classId = snap.getKey();
                                    String className = snap.child("className").getValue(String.class);

                                    // ✅ Student Info
                                    String uid = mAuth.getCurrentUser().getUid();
                                    String email = mAuth.getCurrentUser().getEmail();
                                    String name = extractNameFromEmail(email);
                                    long joinedAt = System.currentTimeMillis();

                                    Student student = new Student(uid, name, email, joinedAt);
                                    database.child(classId).child("students").child(uid).setValue(student)
                                            .addOnCompleteListener(task -> {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(CreateClassActivity.this, "Joined class: " + className, Toast.LENGTH_SHORT).show();

                                                    Intent intent = new Intent(CreateClassActivity.this, DashboardActivity.class);
                                                    intent.putExtra("classId", classId);
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    Toast.makeText(CreateClassActivity.this, "Failed to save student!", Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                    break;
                                }
                            } else {
                                Toast.makeText(CreateClassActivity.this, "Invalid Class Code!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(CreateClassActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // ✅ Share class code button
        shareCodeButton.setOnClickListener(v -> {
            if (latestClassCode.isEmpty()) {
                Toast.makeText(this, "Create a class first!", Toast.LENGTH_SHORT).show();
            } else {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT,
                        "📚 Join my class on ClassLink!\n\nClass Code: " + latestClassCode);
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, "Share Class Code via"));
            }
        });
    }

    // ✅ Create new class
    private void createNewClass(String className) {
        String classCode = generateClassCode();
        latestClassCode = classCode; // ✅ store code for sharing
        String classId = database.push().getKey();
        String adminEmail = mAuth.getCurrentUser().getEmail();

        ClassModel newClass = new ClassModel(className, classCode, adminEmail);

        assert classId != null;
        database.child(classId).setValue(newClass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        generatedCodeText.setText("Your Class Code: " + classCode);
                        Toast.makeText(CreateClassActivity.this, "✓ Class Created Successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CreateClassActivity.this, "Failed to create class", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String generateClassCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        Random rand = new Random();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(rand.nextInt(chars.length())));
        }
        return code.toString();
    }

    private String extractNameFromEmail(String email) {
        if (email == null) return "Unknown";
        String beforeAt = email.split("@")[0];
        beforeAt = beforeAt.replaceAll("\\d", "");
        String[] parts = beforeAt.split("\\.");
        String name = parts.length > 0 ? parts[parts.length - 1] : beforeAt;
        if (!name.isEmpty()) {
            name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
        }
        return name;
    }

    public static class ClassModel {
        public String className;
        public String classCode;
        public String adminEmail;

        public ClassModel() {}
        public ClassModel(String className, String classCode, String adminEmail) {
            this.className = className;
            this.classCode = classCode;
            this.adminEmail = adminEmail;
        }
    }

    public static class Student {
        public String uid;
        public String name;
        public String email;
        public long joinedAt;

        public Student() {}
        public Student(String uid, String name, String email, long joinedAt) {
            this.uid = uid;
            this.name = name;
            this.email = email;
            this.joinedAt = joinedAt;
        }
    }
}

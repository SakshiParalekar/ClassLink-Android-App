/*

package com.example.classlink;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.*;

public class UploadTextNotesActivity extends AppCompatActivity {
    private EditText noteTitleEditText, noteDescriptionEditText, notePurposeEditText;
    private TextView charCountTextView, readingTimeTextView;
    private Button submitTextNoteBtn, imageNoteBtn;
    private Spinner subjectSpinner;
    private ImageButton addSubjectBtn;
    private ArrayList<String> subjectList;
    private ArrayAdapter<String> subjectAdapter;
    private DatabaseReference notesRef, subjectsRef;
    private String classId;
    private FirebaseAuth mAuth;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_textnotes);

        // Initialize views
        noteTitleEditText = findViewById(R.id.noteTitleEditText);
        noteDescriptionEditText = findViewById(R.id.noteDescriptionEditText);
        notePurposeEditText = findViewById(R.id.notePurposeEditText);
        charCountTextView = findViewById(R.id.charCountTextView);
        readingTimeTextView = findViewById(R.id.readingTimeTextView);
        submitTextNoteBtn = findViewById(R.id.submitTextNoteBtn);
        imageNoteBtn = findViewById(R.id.imageNoteBtn);
        subjectSpinner = findViewById(R.id.subjectSpinner);
        addSubjectBtn = findViewById(R.id.addSubjectBtn);

        // Get class ID from intent
        classId = getIntent().getStringExtra("classId");

        // Initialize Firebase references
        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase db = FirebaseDatabase.getInstance("https://claink-49383-default-rtdb.asia-southeast1.firebasedatabase.app");
        notesRef = db.getReference("notes").child(classId);  // Reference to notes specific to this class
        subjectsRef = db.getReference("subjects").child(classId);  // Reference to subjects specific to this class

        // Initialize subject list and adapter
        subjectList = new ArrayList<>();
        subjectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, subjectList);
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjectSpinner.setAdapter(subjectAdapter);

        // Load subjects from Firebase
        loadSubjectsFromFirebase();

        // Add subject button logic
        addSubjectBtn.setOnClickListener(v -> showAddSubjectDialog());

        // TextChangedListener for character count and reading time estimation
        noteDescriptionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = s.length();
                charCountTextView.setText(length + "/2000");

                int words = s.toString().trim().isEmpty() ? 0 : s.toString().trim().split("\\s+").length;
                int minutes = (int) Math.ceil(words / 200.0);
                minutes = Math.max(1, minutes);
                readingTimeTextView.setText("Estimated Reading Time: " + minutes + " min(s)");
            }
        });

        // Submit note button logic
        submitTextNoteBtn.setOnClickListener(v -> {
            String title = noteTitleEditText.getText().toString().trim();
            String description = noteDescriptionEditText.getText().toString().trim();
            String purpose = notePurposeEditText.getText().toString().trim();
            String subject = subjectSpinner.getSelectedItem().toString();

            if (title.isEmpty() || description.isEmpty() || subject.isEmpty()) {
                Toast.makeText(this, "Please fill all fields and select subject.", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "anonymous";
            String noteId = notesRef.push().getKey();  // Generate unique key for the note

            Map<String, Object> noteData = new HashMap<>();
            noteData.put("title", title);
            noteData.put("description", description);
            noteData.put("purpose", purpose);
            noteData.put("subject", subject);
            noteData.put("userId", userId);
            noteData.put("timestamp", System.currentTimeMillis());

            notesRef.child(noteId).setValue(noteData).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Note uploaded successfully!", Toast.LENGTH_SHORT).show();
                    finish();  // Go back to the previous screen
                } else {
                    Toast.makeText(this, "Upload failed. Try again.", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Image note button click logic
        imageNoteBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, ImageNotesUploadActivity.class);
            intent.putExtra("classId", classId);  // Pass classId to image note upload
            startActivity(intent);
        });
    }

    // Load subjects from Firebase
    private void loadSubjectsFromFirebase() {
        subjectList.clear();
        subjectsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot subSnap : snapshot.getChildren()) {
                    String subject = subSnap.getValue(String.class);
                    if (subject != null) subjectList.add(subject);
                }
                subjectAdapter.notifyDataSetChanged();  // Update spinner
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UploadTextNotesActivity.this, "Failed to load subjects", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Show dialog to add a new subject
    private void showAddSubjectDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_subject, null);
        EditText editTextNewSubject = dialogView.findViewById(R.id.editTextNewSubject);
        Button cancelBtn = dialogView.findViewById(R.id.cancelBtn);
        Button addBtn = dialogView.findViewById(R.id.addBtn);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        addBtn.setOnClickListener(v -> {
            String newSubject = editTextNewSubject.getText().toString().trim();
            if (newSubject.isEmpty()) {
                Toast.makeText(this, "Enter a valid subject name.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if subject already exists
            if (subjectList.contains(newSubject)) {
                Toast.makeText(this, "Subject already exists.", Toast.LENGTH_SHORT).show();
                return;
            }

            String subjectId = subjectsRef.push().getKey();
            subjectsRef.child(subjectId).setValue(newSubject).addOnSuccessListener(aVoid -> {
                subjectList.add(newSubject);
                subjectAdapter.notifyDataSetChanged();
                subjectSpinner.setSelection(subjectList.indexOf(newSubject));  // Select the newly added subject
                Toast.makeText(this, "Subject added!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }).addOnFailureListener(e -> Toast.makeText(this, "Failed to add subject.", Toast.LENGTH_SHORT).show());
        });
        dialog.show();
    }
}

*//*
*/
/*
*//*

*/
/*


package com.example.classlink;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;

public class UploadTextNotesActivity extends AppCompatActivity {

    private Spinner subjectSpinner;
    private ImageView addSubjectBtn;
    private EditText noteTitleEditText, noteDescriptionEditText, notePurposeEditText;
    private TextView charCountTextView, readingTimeTextView;
    private Button submitTextNoteBtn, textNoteBtn, imageNoteBtn;

    private ArrayList<String> subjectList;
    private ArrayAdapter<String> subjectAdapter;

    private DatabaseReference subjectRef, notesRef, userRef;
    private FirebaseUser currentUser;
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_textnotes); // Your XML layout filename

        // Firebase init
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        subjectRef = FirebaseDatabase.getInstance().getReference("Subjects");
        notesRef = FirebaseDatabase.getInstance().getReference("Notes");

        // UI elements
        subjectSpinner = findViewById(R.id.subjectSpinner);
        addSubjectBtn = findViewById(R.id.addSubjectBtn);
        noteTitleEditText = findViewById(R.id.noteTitleEditText);
        noteDescriptionEditText = findViewById(R.id.noteDescriptionEditText);
        notePurposeEditText = findViewById(R.id.notePurposeEditText);
        charCountTextView = findViewById(R.id.charCountTextView);
        readingTimeTextView = findViewById(R.id.readingTimeTextView);
        submitTextNoteBtn = findViewById(R.id.submitTextNoteBtn);
        textNoteBtn = findViewById(R.id.textNoteBtn);
        imageNoteBtn = findViewById(R.id.imageNoteBtn);

        subjectList = new ArrayList<>();
        subjectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, subjectList);
        subjectSpinner.setAdapter(subjectAdapter);

        checkUserRole(); // Check if current user is admin
        loadSubjects();  // Load all subjects into Spinner

        // Handle "Add Subject" button
        addSubjectBtn.setOnClickListener(v -> {
            if (isAdmin) showAddSubjectDialog();
        });

        // Handle character count and reading time
        noteDescriptionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                charCountTextView.setText(s.length() + "/2000");
                readingTimeTextView.setText("Estimated Reading Time: " + getEstimatedReadingTime(s.toString()));
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // Submit text note
        submitTextNoteBtn.setOnClickListener(v -> uploadNote());

        // Toggle to Image Note activity
        imageNoteBtn.setOnClickListener(v -> {
            Intent intent = new Intent(UploadTextNotesActivity.this, ImageNotesUploadActivity.class);
            startActivity(intent);
        });

        // Optional UI toggle button colors
        textNoteBtn.setBackgroundTintList(getColorStateList(R.color.blue));
        imageNoteBtn.setBackgroundTintList(getColorStateList(R.color.grey));
    }

    private void checkUserRole() {
        userRef.child(currentUser.getUid()).child("role")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String role = snapshot.getValue(String.class);
                        isAdmin = "admin".equalsIgnoreCase(role);

                        // Only show addSubject button to admins
                        addSubjectBtn.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(UploadTextNotesActivity.this, "Failed to check role", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadSubjects() {
        subjectRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                subjectList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    subjectList.add(snap.getValue(String.class));
                }
                subjectAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UploadTextNotesActivity.this, "Failed to load subjects", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddSubjectDialog() {
        final EditText input = new EditText(this);
        input.setHint("Enter subject name");

        new android.app.AlertDialog.Builder(this)
                .setTitle("Add Subject")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String subject = input.getText().toString().trim();
                    if (!subject.isEmpty()) {
                        subjectRef.push().setValue(subject);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void uploadNote() {
        String title = noteTitleEditText.getText().toString().trim();
        String description = noteDescriptionEditText.getText().toString().trim();
        String purpose = notePurposeEditText.getText().toString().trim();
        String subject = subjectSpinner.getSelectedItem() != null ? subjectSpinner.getSelectedItem().toString() : "";

        if (title.isEmpty() || description.isEmpty() || subject.isEmpty()) {
            Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String noteId = notesRef.push().getKey();
        HashMap<String, Object> note = new HashMap<>();
        note.put("title", title);
        note.put("description", description);
        note.put("purpose", purpose);
        note.put("subject", subject);
        note.put("readingTime", getEstimatedReadingTime(description));
        note.put("userId", currentUser.getUid());

        notesRef.child(noteId).setValue(note)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Note uploaded", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // 🚀 Reading time logic — capped at 3 mins
    private String getEstimatedReadingTime(String text) {
        int wordCount = text.trim().isEmpty() ? 0 : text.trim().split("\\s+").length;

        if (wordCount <= 500) return "1 min";
        else if (wordCount <= 1000) return "2 mins";
        else return "3 mins";
    }
}
*//*
*/
/*

//Estimated time working
*//*

*/
/*package com.example.classlink;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class UploadTextNotesActivity extends AppCompatActivity {

    private Spinner subjectSpinner;
    private ImageView addSubjectBtn;
    private EditText noteTitleEditText, noteDescriptionEditText, notePurposeEditText;
    private TextView charCountTextView, readingTimeTextView;
    private Button submitTextNoteBtn, textNoteBtn, imageNoteBtn;

    private ArrayList<String> subjectList;
    private ArrayAdapter<String> subjectAdapter;

    // Simulated role check — in real app, use Firebase user role
    private boolean isAdmin = false; // Change to true if user is admin

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_textnotes); // Ensure your XML is named correctly

        // Initialize views
        subjectSpinner = findViewById(R.id.subjectSpinner);
        addSubjectBtn = findViewById(R.id.addSubjectBtn);
        noteTitleEditText = findViewById(R.id.noteTitleEditText);
        noteDescriptionEditText = findViewById(R.id.noteDescriptionEditText);
        notePurposeEditText = findViewById(R.id.notePurposeEditText);
        charCountTextView = findViewById(R.id.charCountTextView);
        readingTimeTextView = findViewById(R.id.readingTimeTextView);
        submitTextNoteBtn = findViewById(R.id.submitTextNoteBtn);
        textNoteBtn = findViewById(R.id.textNoteBtn);
        imageNoteBtn = findViewById(R.id.imageNoteBtn);

        // Spinner setup
        subjectList = new ArrayList<>();
        subjectList.add("Math");
        subjectList.add("Physics");
        subjectList.add("Biology");

        subjectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, subjectList);
        subjectSpinner.setAdapter(subjectAdapter);

        // Hide Add Subject button if not admin
        if (isAdmin) {
            addSubjectBtn.setVisibility(View.VISIBLE);
        } else {
            addSubjectBtn.setVisibility(View.GONE);
        }

        // Add Subject Logic
        addSubjectBtn.setOnClickListener(v -> {
            EditText input = new EditText(this);
            input.setHint("Enter new subject");

            new android.app.AlertDialog.Builder(this)
                    .setTitle("Add Subject")
                    .setView(input)
                    .setPositiveButton("Add", (dialog, which) -> {
                        String newSubject = input.getText().toString().trim();
                        if (!newSubject.isEmpty()) {
                            subjectList.add(newSubject);
                            subjectAdapter.notifyDataSetChanged();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // TextWatcher for description field
        noteDescriptionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                int length = s.length();
                charCountTextView.setText(length + "/2000");
                readingTimeTextView.setText("Estimated Reading Time: " + getEstimatedReadingTime(s.toString()));
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // Text Note Button Click (can color toggle)
        textNoteBtn.setOnClickListener(v -> {
            Toast.makeText(this, "You're already in Text Note mode", Toast.LENGTH_SHORT).show();
        });

        // Image Note Button Redirect
        imageNoteBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, ImageNotesUploadActivity.class);
            startActivity(intent);
        });

        // Submit Button
        submitTextNoteBtn.setOnClickListener(v -> {
            String title = noteTitleEditText.getText().toString().trim();
            String description = noteDescriptionEditText.getText().toString().trim();
            String purpose = notePurposeEditText.getText().toString().trim();
            String subject = subjectSpinner.getSelectedItem().toString();

            if (title.isEmpty() || description.isEmpty() || purpose.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: Upload to Firebase or local DB
            Toast.makeText(this, "Note uploaded for subject: " + subject, Toast.LENGTH_LONG).show();

            // Clear fields
            noteTitleEditText.setText("");
            noteDescriptionEditText.setText("");
            notePurposeEditText.setText("");
            charCountTextView.setText("0/2000");
            readingTimeTextView.setText("Estimated Reading Time: 1 min");
        });
    }

    // Logic for estimated reading time (character-based)
    private String getEstimatedReadingTime(String text) {
        int length = text.trim().length();

        if (length <= 500) return "1 min";
        else if (length <= 1000) return "2 mins";
        else return "3 mins";
    }
}*//*
*/
/*


//firebase
*//*

*/
/*package com.example.classlink;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UploadTextNotesActivity extends AppCompatActivity {

    private Spinner subjectSpinner;
    private ImageView addSubjectBtn;
    private EditText noteTitleEditText, noteDescriptionEditText, notePurposeEditText;
    private TextView charCountTextView, readingTimeTextView;
    private Button submitTextNoteBtn, textNoteBtn, imageNoteBtn;

    private ArrayList<String> subjectList;
    private ArrayAdapter<String> subjectAdapter;

    // Simulated role check — in real app, use Firebase user role
    private boolean isAdmin = false; // Change to true if user is admin

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_textnotes);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        subjectSpinner = findViewById(R.id.subjectSpinner);
        addSubjectBtn = findViewById(R.id.addSubjectBtn);
        noteTitleEditText = findViewById(R.id.noteTitleEditText);
        noteDescriptionEditText = findViewById(R.id.noteDescriptionEditText);
        notePurposeEditText = findViewById(R.id.notePurposeEditText);
        charCountTextView = findViewById(R.id.charCountTextView);
        readingTimeTextView = findViewById(R.id.readingTimeTextView);
        submitTextNoteBtn = findViewById(R.id.submitTextNoteBtn);
        textNoteBtn = findViewById(R.id.textNoteBtn);
        imageNoteBtn = findViewById(R.id.imageNoteBtn);

        // Spinner setup
        subjectList = new ArrayList<>();
        // subjectList.add("Math");  // optional: remove demo values if using admin-added only
        subjectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, subjectList);
        subjectSpinner.setAdapter(subjectAdapter);

        // Hide or show Add Subject
        if (isAdmin) {
            addSubjectBtn.setVisibility(View.VISIBLE);
        } else {
            addSubjectBtn.setVisibility(View.GONE);
        }

        // Add Subject
        addSubjectBtn.setOnClickListener(v -> {
            EditText input = new EditText(this);
            input.setHint("Enter new subject");

            new android.app.AlertDialog.Builder(this)
                    .setTitle("Add Subject")
                    .setView(input)
                    .setPositiveButton("Add", (dialog, which) -> {
                        String newSubject = input.getText().toString().trim();
                        if (!newSubject.isEmpty()) {
                            subjectList.add(newSubject);
                            subjectAdapter.notifyDataSetChanged();
                            Toast.makeText(this, "Subject added!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Word counter & reading time
        noteDescriptionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                int length = s.length();
                charCountTextView.setText(length + "/2000");
                readingTimeTextView.setText("Estimated Reading Time: " + getEstimatedReadingTime(s.toString()));
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // Toggle Buttons
        textNoteBtn.setOnClickListener(v ->
                Toast.makeText(this, "You're already in Text Note mode", Toast.LENGTH_SHORT).show()
        );

        imageNoteBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, ImageNotesUploadActivity.class);
            startActivity(intent);
        });

        // Submit Button
        submitTextNoteBtn.setOnClickListener(v -> {
            String title = noteTitleEditText.getText().toString().trim();
            String description = noteDescriptionEditText.getText().toString().trim();
            String purpose = notePurposeEditText.getText().toString().trim();
            String subject = subjectSpinner.getSelectedItem() != null ? subjectSpinner.getSelectedItem().toString() : "";

            if (title.isEmpty() || description.isEmpty() || purpose.isEmpty() || subject.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Firestore Save
            Map<String, Object> note = new HashMap<>();
            note.put("title", title);
            note.put("description", description);
            note.put("summary", purpose);
            note.put("readingTime", getEstimatedReadingTime(description));
            note.put("subject", subject);
            note.put("timestamp", System.currentTimeMillis());

            db.collection("Notes")
                    .add(note)
                    .addOnSuccessListener(docRef -> {
                        Toast.makeText(this, "Note uploaded successfully!", Toast.LENGTH_SHORT).show();
                        noteTitleEditText.setText("");
                        noteDescriptionEditText.setText("");
                        notePurposeEditText.setText("");
                        charCountTextView.setText("0/2000");
                        readingTimeTextView.setText("Estimated Reading Time: 1 min");
                        subjectSpinner.setSelection(0);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to upload: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });
    }

    // Character-based Reading Time (up to 3 mins max)
    private String getEstimatedReadingTime(String text) {
        int length = text.trim().length();
        if (length <= 500) return "1 min";
        else if (length <= 1000) return "2 mins";
        else return "3 mins";
    }
}*//*
*/
/*

*//*

*/
/*
package com.example.classlink;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.*;

public class UploadTextNotesActivity extends AppCompatActivity {

    private Spinner subjectSpinner;
    private ImageView addSubjectBtn;
    private EditText noteTitleEditText, noteDescriptionEditText, notePurposeEditText;
    private TextView charCountTextView, readingTimeTextView;
    private Button submitTextNoteBtn, textNoteBtn, imageNoteBtn;

    private ArrayList<String> subjectList;
    private ArrayAdapter<String> subjectAdapter;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;

    private String currentUserEmail;
    private String classId = "-OWzqKKoFbuN5OIeEE09";  // Change based on your class document ID

    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_textnotes);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();
        currentUserEmail = currentUser.getEmail();

        // Views
        subjectSpinner = findViewById(R.id.subjectSpinner);
        addSubjectBtn = findViewById(R.id.addSubjectBtn);
        noteTitleEditText = findViewById(R.id.noteTitleEditText);
        noteDescriptionEditText = findViewById(R.id.noteDescriptionEditText);
        notePurposeEditText = findViewById(R.id.notePurposeEditText);
        charCountTextView = findViewById(R.id.charCountTextView);
        readingTimeTextView = findViewById(R.id.readingTimeTextView);
        submitTextNoteBtn = findViewById(R.id.submitTextNoteBtn);
        textNoteBtn = findViewById(R.id.textNoteBtn);
        imageNoteBtn = findViewById(R.id.imageNoteBtn);

        // Spinner setup
        subjectList = new ArrayList<>();
        subjectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, subjectList);
        subjectSpinner.setAdapter(subjectAdapter);

        fetchClassInfo();

        noteDescriptionEditText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable s) {
                int len = s.length();
                charCountTextView.setText(len + "/2000");
                readingTimeTextView.setText("Estimated Reading Time: " + getEstimatedReadingTime(len));
            }
        });

        addSubjectBtn.setOnClickListener(v -> {
            if (!isAdmin) {
                Toast.makeText(this, "Only class admin can add subjects.", Toast.LENGTH_SHORT).show();
                return;
            }
            showAddSubjectDialog();
        });

        submitTextNoteBtn.setOnClickListener(v -> uploadNote());

        imageNoteBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, ImageNotesUploadActivity.class));
        });

        textNoteBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Already on Text Note", Toast.LENGTH_SHORT).show();
        });
    }

    private void fetchClassInfo() {
        firestore.collection("classes").document(classId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String adminEmail = documentSnapshot.getString("adminEmail");
                        isAdmin = adminEmail != null && adminEmail.equals(currentUserEmail);

                        List<String> subjects = (List<String>) documentSnapshot.get("subjects");
                        if (subjects != null) {
                            subjectList.clear();
                            subjectList.addAll(subjects);
                            subjectAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    private void showAddSubjectDialog() {
        EditText input = new EditText(this);
        input.setHint("Enter subject");

        new AlertDialog.Builder(this)
                .setTitle("Add Subject")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String newSubject = input.getText().toString().trim();
                    if (!newSubject.isEmpty()) {
                        subjectList.add(newSubject);
                        subjectAdapter.notifyDataSetChanged();

                        firestore.collection("classes").document(classId)
                                .update("subjects", subjectList)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Subject added", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String getEstimatedReadingTime(int charCount) {
        if (charCount < 500) return "1 min";
        else if (charCount < 1000) return "2 min";
        else return "3 min";
    }

    private void uploadNote() {
        String title = noteTitleEditText.getText().toString().trim();
        String desc = noteDescriptionEditText.getText().toString().trim();
        String purpose = notePurposeEditText.getText().toString().trim();
        String subject = subjectSpinner.getSelectedItem() != null ? subjectSpinner.getSelectedItem().toString() : "";

        if (title.isEmpty() || desc.isEmpty() || purpose.isEmpty() || subject.isEmpty()) {
            Toast.makeText(this, "Please fill all fields and select subject", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> note = new HashMap<>();
        note.put("title", title);
        note.put("description", desc);
        note.put("purpose", purpose);
        note.put("subject", subject);
        note.put("timestamp", new Date());
        note.put("uploadedBy", currentUserEmail);

        firestore.collection("classes")
                .document(classId)
                .collection("textNotes")
                .add(note)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Note uploaded", Toast.LENGTH_SHORT).show();
                    finish(); // go back
                });
    }
}*//*
*/
/*


package com.example.classlink;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UploadTextNotesActivity extends AppCompatActivity {

    EditText notesEditText;
    Spinner subjectSpinner;
    Button uploadButton;

    FirebaseAuth mAuth;
    DatabaseReference classesRef;

    String classCode;
    String classId;
    String currentUserEmail;
    String adminEmail;

    ArrayList<String> subjectList = new ArrayList<>();
    ArrayAdapter<String> subjectAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_textnotes);

        notesEditText = findViewById(R.id.noteTitleEditText);
        subjectSpinner = findViewById(R.id.subjectSpinner);
        uploadButton = findViewById(R.id.textNoteBtn);

        mAuth = FirebaseAuth.getInstance();
        currentUserEmail = mAuth.getCurrentUser().getEmail();

        classCode = getIntent().getStringExtra("classCode");
        if (TextUtils.isEmpty(classCode)) {
            Toast.makeText(this, "Class code missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        classesRef = FirebaseDatabase.getInstance().getReference("classes");

        subjectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, subjectList);
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjectSpinner.setAdapter(subjectAdapter);

        fetchClassInfo();

        uploadButton.setOnClickListener(v -> uploadNotes());
    }

    private void fetchClassInfo() {
        classesRef.orderByChild("classCode").equalTo(classCode)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot classSnap : snapshot.getChildren()) {
                                classId = classSnap.getKey();
                                adminEmail = classSnap.child("adminEmail").getValue(String.class);
                                fetchSubjects();
                                break;
                            }
                        } else {
                            Toast.makeText(UploadTextNotesActivity.this, "Class not found", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(UploadTextNotesActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchSubjects() {
        classesRef.child(classId).child("subjects")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        subjectList.clear();
                        for (DataSnapshot sub : snapshot.getChildren()) {
                            String subject = sub.getValue(String.class);
                            subjectList.add(subject);
                        }
                        subjectAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void uploadNotes() {
        String noteText = notesEditText.getText().toString().trim();
        String subject = subjectSpinner.getSelectedItem() != null ? subjectSpinner.getSelectedItem().toString() : "";

        if (TextUtils.isEmpty(noteText)) {
            notesEditText.setError("Enter some notes");
            return;
        }

        if (TextUtils.isEmpty(subject)) {
            Toast.makeText(this, "Select a subject", Toast.LENGTH_SHORT).show();
            return;
        }

        int charCount = noteText.length();
        int estimatedTime = charCount / 20; // estimate: 20 chars/sec reading speed

        DatabaseReference notesRef = classesRef.child(classId).child("notes").push();

        Map<String, Object> noteMap = new HashMap<>();
        noteMap.put("text", noteText);
        noteMap.put("subject", subject);
        noteMap.put("charCount", charCount);
        noteMap.put("estimatedReadingTime", estimatedTime);
        noteMap.put("uploadedBy", currentUserEmail);

        notesRef.setValue(noteMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Note uploaded successfully", Toast.LENGTH_SHORT).show();
                        notesEditText.setText("");
                    } else {
                        Toast.makeText(this, "Failed to upload note", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.subject_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_add_subject) {
            if (currentUserEmail.equals(adminEmail)) {
                showAddSubjectDialog();
            } else {
                Toast.makeText(this, "Only class admins can add subjects", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAddSubjectDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_subject, null);
        EditText subjectEditText = dialogView.findViewById(R.id.editTextNewSubject);
        Button cancelBtn = dialogView.findViewById(R.id.cancelBtn);
        Button addBtn = dialogView.findViewById(R.id.addBtn);

        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();

        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        addBtn.setOnClickListener(v -> {
            String newSubject = subjectEditText.getText().toString().trim();
            if (!TextUtils.isEmpty(newSubject)) {
                classesRef.child(classId).child("subjects").push().setValue(newSubject)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Subject added", Toast.LENGTH_SHORT).show();
                                subjectList.add(newSubject);
                                subjectAdapter.notifyDataSetChanged();
                                dialog.dismiss();
                            } else {
                                Toast.makeText(this, "Failed to add subject", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                subjectEditText.setError("Enter subject");
            }
        });

        dialog.show();
    }
}
*//*

package com.example.classlink;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class UploadTextNotesActivity extends AppCompatActivity {

    private Spinner subjectSpinner;
    private ImageView addSubjectBtn;
    private EditText noteTitleEditText, noteDescriptionEditText, notePurposeEditText;
    private TextView charCountTextView, readingTimeTextView;
    private Button submitTextNoteBtn, textNoteBtn, imageNoteBtn;

    private List<String> subjectList;
    private ArrayAdapter<String> subjectAdapter;

    // Firebase instances
    private FirebaseAuth mAuth;
    private DatabaseReference database;
    private String classId;

    // Admin status flag
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_textnotes);

        // --- NEW: Initialize Firebase instances ---
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase
                .getInstance("https://claink-49383-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference();
        // --- END NEW ---

        // Get the class ID from the intent
        // NOTE: This assumes the classId is passed from a previous activity.
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("classId")) {
            classId = intent.getStringExtra("classId");
        } else {
            Toast.makeText(this, "Class ID not found. Cannot load subjects.", Toast.LENGTH_LONG).show();
            return;
        }

        // Initialize views
        subjectSpinner = findViewById(R.id.subjectSpinner);
        addSubjectBtn = findViewById(R.id.addSubjectBtn);
        noteTitleEditText = findViewById(R.id.noteTitleEditText);
        noteDescriptionEditText = findViewById(R.id.noteDescriptionEditText);
        notePurposeEditText = findViewById(R.id.notePurposeEditText);
        charCountTextView = findViewById(R.id.charCountTextView);
        readingTimeTextView = findViewById(R.id.readingTimeTextView);
        submitTextNoteBtn = findViewById(R.id.submitTextNoteBtn);
        textNoteBtn = findViewById(R.id.textNoteBtn);
        imageNoteBtn = findViewById(R.id.imageNoteBtn);

        // Spinner setup
        subjectList = new ArrayList<>();
        subjectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, subjectList);
        subjectSpinner.setAdapter(subjectAdapter);

        // --- NEW: Check admin status and setup subject listener ---
        checkAdminStatusAndSetupSubjects();
        // --- END NEW ---

        // Add Subject Logic
        addSubjectBtn.setOnClickListener(v -> {
            EditText input = new EditText(this);
            input.setHint("Enter new subject");

            new android.app.AlertDialog.Builder(this)
                    .setTitle("Add Subject")
                    .setView(input)
                    .setPositiveButton("Add", (dialog, which) -> {
                        String newSubject = input.getText().toString().trim();
                        if (!newSubject.isEmpty()) {
                            // --- NEW: Push the new subject to Firebase ---
                            DatabaseReference subjectsRef = database.child("classes").child(classId).child("subjects");
                            String subjectId = subjectsRef.push().getKey();
                            subjectsRef.child(subjectId).setValue(newSubject);
                            // --- END NEW ---
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // TextWatcher for description field
        noteDescriptionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                int length = s.length();
                charCountTextView.setText(length + "/2000");
                readingTimeTextView.setText("Estimated Reading Time: " + getEstimatedReadingTime(s.toString()));
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // Text Note Button Click (can color toggle)
        textNoteBtn.setOnClickListener(v -> {
            Toast.makeText(this, "You're already in Text Note mode", Toast.LENGTH_SHORT).show();
        });

        // Image Note Button Redirect
        */
/*imageNoteBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, ImageNotesUploadActivity.class);
            startActivity(intent);
        });*//*


        // Submit Button
        submitTextNoteBtn.setOnClickListener(v -> {
            String title = noteTitleEditText.getText().toString().trim();
            String description = noteDescriptionEditText.getText().toString().trim();
            String purpose = notePurposeEditText.getText().toString().trim();
            String subject = subjectSpinner.getSelectedItem().toString();

            if (title.isEmpty() || description.isEmpty() || purpose.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: Upload to Firebase or local DB
            Toast.makeText(this, "Note uploaded for subject: " + subject, Toast.LENGTH_LONG).show();

            // Clear fields
            noteTitleEditText.setText("");
            noteDescriptionEditText.setText("");
            notePurposeEditText.setText("");
            charCountTextView.setText("0/2000");
            readingTimeTextView.setText("Estimated Reading Time: 1 min");
        });
    }

    // Logic for estimated reading time (character-based)
    private String getEstimatedReadingTime(String text) {
        int length = text.trim().length();

        if (length <= 500) return "1 min";
        else if (length <= 1000) return "2 mins";
        else return "3 mins";
    }

    */
/**
     * Checks if the current user is the admin of the class and sets up a real-time listener for subjects.
     * This method combines both checks to ensure the UI is set up correctly based on the user's role.
     *//*

    private void checkAdminStatusAndSetupSubjects() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserEmail = mAuth.getCurrentUser().getEmail();

        // Listen for changes in the class data to determine admin status
        database.child("classes").child(classId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String adminEmail = snapshot.child("adminEmail").getValue(String.class);
                if (currentUserEmail != null && currentUserEmail.equals(adminEmail)) {
                    isAdmin = true;
                } else {
                    isAdmin = false;
                }

                // Hide or show the add button based on admin status
                if (isAdmin) {
                    addSubjectBtn.setVisibility(View.VISIBLE);
                } else {
                    addSubjectBtn.setVisibility(View.GONE);
                }

                // Now that we have the class ID, set up the subject listener
                setupSubjectListener();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UploadTextNotesActivity.this, "Failed to load class data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("UploadTextNotes", "Failed to load class data.", error.toException());
                // In case of error, hide the add button
                addSubjectBtn.setVisibility(View.GONE);
            }
        });
    }

    */
/**
     * Sets up a real-time listener to fetch and display subjects from Firebase.
     * This listener ensures the spinner is always up-to-date for all users.
     *//*

    private void setupSubjectListener() {
        DatabaseReference subjectsRef = database.child("classes").child(classId).child("subjects");
        subjectsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                subjectList.clear();
                // Add a placeholder item
                subjectList.add("Select Subject");

                for (DataSnapshot subjectSnapshot : snapshot.getChildren()) {
                    String subjectName = subjectSnapshot.getValue(String.class);
                    if (subjectName != null) {
                        subjectList.add(subjectName);
                    }
                }
                subjectAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UploadTextNotesActivity.this, "Failed to load subjects: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("UploadTextNotes", "Failed to load subjects.", error.toException());
            }
        });
    }
}*/


/*package com.example.classlink;


import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;


import java.util.ArrayList;
import java.util.List;


public class UploadTextNotesActivity extends AppCompatActivity {


    private Spinner subjectSpinner, unitSpinner; // --- MODIFIED: Added unitSpinner
    private ImageView addSubjectBtn, addUnitBtn; // --- MODIFIED: Added addUnitBtn
    private EditText noteTitleEditText, noteDescriptionEditText, notePurposeEditText;
    private TextView charCountTextView, readingTimeTextView;
    private Button submitTextNoteBtn, textNoteBtn, imageNoteBtn;


    private List<String> subjectList;
    private ArrayAdapter<String> subjectAdapter;
    private List<String> unitList; // --- NEW: List for units
    private ArrayAdapter<String> unitAdapter; // --- NEW: Adapter for units


    // Firebase instances
    private FirebaseAuth mAuth;
    private DatabaseReference database;
    private String classId;


    // Admin status flag
    private boolean isAdmin = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_textnotes);


        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase
                .getInstance("https://claink-49383-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference();


        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("classId")) {
            classId = intent.getStringExtra("classId");
        } else {
            Toast.makeText(this, "Class ID not found. Cannot load subjects.", Toast.LENGTH_LONG).show();
            return;
        }


        // Initialize views
        subjectSpinner = findViewById(R.id.subjectSpinner);
        addSubjectBtn = findViewById(R.id.addSubjectBtn);
        // --- NEW: Initialized new views for unit spinner ---
        unitSpinner = findViewById(R.id.unitSpinner);
        addUnitBtn = findViewById(R.id.addUnitBtn);
        // --- END NEW ---
        noteTitleEditText = findViewById(R.id.noteTitleEditText);
        noteDescriptionEditText = findViewById(R.id.noteDescriptionEditText);
        notePurposeEditText = findViewById(R.id.notePurposeEditText);
        charCountTextView = findViewById(R.id.charCountTextView);
        readingTimeTextView = findViewById(R.id.readingTimeTextView);
        submitTextNoteBtn = findViewById(R.id.submitTextNoteBtn);
        textNoteBtn = findViewById(R.id.textNoteBtn);
        imageNoteBtn = findViewById(R.id.imageNoteBtn);


        // Spinner setup
        subjectList = new ArrayList<>();
        subjectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, subjectList);
        subjectSpinner.setAdapter(subjectAdapter);
        // --- NEW: Unit spinner setup ---
        unitList = new ArrayList<>();
        unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, unitList);
        unitSpinner.setAdapter(unitAdapter);
        // --- END NEW ---


        // Check admin status and setup both subject and unit listeners
        checkAdminStatusAndSetupListeners();


        // Add Subject Logic
        addSubjectBtn.setOnClickListener(v -> {
            EditText input = new EditText(this);
            input.setHint("Enter new subject");


            new android.app.AlertDialog.Builder(this)
                    .setTitle("Add Subject")
                    .setView(input)
                    .setPositiveButton("Add", (dialog, which) -> {
                        String newSubject = input.getText().toString().trim();
                        if (!newSubject.isEmpty()) {
                            DatabaseReference subjectsRef = database.child("classes").child(classId).child("subjects");
                            String subjectId = subjectsRef.push().getKey();
                            subjectsRef.child(subjectId).setValue(newSubject);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });


        // --- NEW: Add Unit Logic ---
        addUnitBtn.setOnClickListener(v -> {
            EditText input = new EditText(this);
            input.setHint("Enter new unit");


            new android.app.AlertDialog.Builder(this)
                    .setTitle("Add Unit")
                    .setView(input)
                    .setPositiveButton("Add", (dialog, which) -> {
                        String newUnit = input.getText().toString().trim();
                        if (!newUnit.isEmpty()) {
                            DatabaseReference unitsRef = database.child("classes").child(classId).child("units");
                            String unitId = unitsRef.push().getKey();
                            unitsRef.child(unitId).setValue(newUnit);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
        // --- END NEW ---


        // TextWatcher for description field
        noteDescriptionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                int length = s.length();
                charCountTextView.setText(length + "/2000");
                readingTimeTextView.setText("Estimated Reading Time: " + getEstimatedReadingTime(s.toString()));
            }


            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });


        // Text Note Button Click (can color toggle)
        textNoteBtn.setOnClickListener(v -> {
            Toast.makeText(this, "You're already in Text Note mode", Toast.LENGTH_SHORT).show();
        });


        // Image Note Button Redirect
        imageNoteBtn.setOnClickListener(v -> {
            Intent imageIntent = new Intent(this, ImageNotesUploadActivity.class);
            imageIntent.putExtra("classId", classId); // 👈 add this line
            startActivity(imageIntent);
        });



        // Submit Button
        // Submit Button
        submitTextNoteBtn.setOnClickListener(v -> {
            String title = noteTitleEditText.getText().toString().trim();
            String description = noteDescriptionEditText.getText().toString().trim();
            String purpose = notePurposeEditText.getText().toString().trim();
            String subject = subjectSpinner.getSelectedItem().toString();
            String unit = unitSpinner.getSelectedItem().toString();
            String uploadedBy = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "unknown";


            if (title.isEmpty() || description.isEmpty() || purpose.isEmpty() ||
                    subject.equals("Select Subject") || unit.equals("Select Unit")) {
                Toast.makeText(this, "Please fill all fields and select a subject/unit", Toast.LENGTH_SHORT).show();
                return;
            }


            // Reference in Firebase → classes/{classId}/notes/{noteId}
            DatabaseReference notesRef = database.child("classes").child(classId).child("notes").push();


            int charCount = description.length();
            int readTime = (int) Math.ceil(charCount / 200.0); // ~200 chars = 1 min


            // Note data
            java.util.Map<String, Object> noteData = new java.util.HashMap<>();
            noteData.put("title", title);
            noteData.put("description", description);
            noteData.put("purpose", purpose);
            noteData.put("subject", subject);
            noteData.put("unit", unit);
            noteData.put("uploadedBy", uploadedBy);
            noteData.put("timestamp", System.currentTimeMillis());
            noteData.put("charCount", charCount);
            noteData.put("estimatedReadTime", readTime + " min");


            // Upload
            notesRef.setValue(noteData).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(UploadTextNotesActivity.this, "Note uploaded successfully!", Toast.LENGTH_LONG).show();


                    // Clear fields
                    noteTitleEditText.setText("");
                    noteDescriptionEditText.setText("");
                    notePurposeEditText.setText("");
                    charCountTextView.setText("0/2000");
                    readingTimeTextView.setText("Estimated Reading Time: 1 min");
                } else {
                    Toast.makeText(UploadTextNotesActivity.this, "Upload failed. Try again!", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }


    // Logic for estimated reading time (character-based)
    private String getEstimatedReadingTime(String text) {
        int length = text.trim().length();


        if (length <= 500) return "1 min";
        else if (length <= 1000) return "2 mins";
        else return "3 mins";
    }




    private void checkAdminStatusAndSetupListeners() { // --- MODIFIED: Renamed method
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show();
            return;
        }


        String currentUserEmail = mAuth.getCurrentUser().getEmail();


        // Listen for changes in the class data to determine admin status
        database.child("classes").child(classId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String adminEmail = snapshot.child("adminEmail").getValue(String.class);
                if (currentUserEmail != null && currentUserEmail.equals(adminEmail)) {
                    isAdmin = true;
                } else {
                    isAdmin = false;
                }


                // Hide or show the add buttons based on admin status
                if (isAdmin) {
                    addSubjectBtn.setVisibility(View.VISIBLE);
                    addUnitBtn.setVisibility(View.VISIBLE); // --- NEW: Show add unit button ---
                } else {
                    addSubjectBtn.setVisibility(View.GONE);
                    addUnitBtn.setVisibility(View.GONE); // --- NEW: Hide add unit button ---
                }


                // Now that we have the class ID, set up the subject and unit listeners
                setupSubjectListener();
                setupUnitListener(); // --- NEW: Set up the unit listener ---
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UploadTextNotesActivity.this, "Failed to load class data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("UploadTextNotes", "Failed to load class data.", error.toException());
                // In case of error, hide the add buttons
                addSubjectBtn.setVisibility(View.GONE);
                addUnitBtn.setVisibility(View.GONE); // --- NEW: Hide add unit button ---
            }
        });
    }


    private void setupSubjectListener() {
        DatabaseReference subjectsRef = database.child("classes").child(classId).child("subjects");
        subjectsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                subjectList.clear();
                // Add a placeholder item
                subjectList.add("Select Subject");


                for (DataSnapshot subjectSnapshot : snapshot.getChildren()) {
                    String subjectName = subjectSnapshot.getValue(String.class);
                    if (subjectName != null) {
                        subjectList.add(subjectName);
                    }
                }
                subjectAdapter.notifyDataSetChanged();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UploadTextNotesActivity.this, "Failed to load subjects: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("UploadTextNotes", "Failed to load subjects.", error.toException());
            }
        });
    }


    private void setupUnitListener() {
        DatabaseReference unitsRef = database.child("classes").child(classId).child("units");
        unitsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                unitList.clear();
                // Add a placeholder item
                unitList.add("Select Unit");


                for (DataSnapshot unitSnapshot : snapshot.getChildren()) {
                    String unitName = unitSnapshot.getValue(String.class);
                    if (unitName != null) {
                        unitList.add(unitName);
                    }
                }
                unitAdapter.notifyDataSetChanged();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UploadTextNotesActivity.this, "Failed to load units: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("UploadTextNotes", "Failed to load units.", error.toException());
            }
        });
    }
    // --- END NEW ---
}*/




//firebase stroing
/*package com.example.classlink;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class UploadTextNotesActivity extends AppCompatActivity {

    private Spinner subjectSpinner, unitSpinner;
    private ImageView addSubjectBtn, addUnitBtn;
    private EditText noteTitleEditText, noteDescriptionEditText, notePurposeEditText;
    private TextView charCountTextView, readingTimeTextView;
    private Button submitTextNoteBtn, textNoteBtn, imageNoteBtn;

    private List<String> subjectList;
    private ArrayAdapter<String> subjectAdapter;
    private List<String> unitList;
    private ArrayAdapter<String> unitAdapter;

    // Firebase instances
    private FirebaseAuth mAuth;
    private DatabaseReference database;
    private String classId;

    // Admin status flag
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_textnotes);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase
                .getInstance("https://claink-49383-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("classId")) {
            classId = intent.getStringExtra("classId");
        } else {
            Toast.makeText(this, "Class ID not found. Cannot load subjects.", Toast.LENGTH_LONG).show();
            return;
        }

        // Initialize views
        subjectSpinner = findViewById(R.id.subjectSpinner);
        addSubjectBtn = findViewById(R.id.addSubjectBtn);
        unitSpinner = findViewById(R.id.unitSpinner);
        addUnitBtn = findViewById(R.id.addUnitBtn);
        noteTitleEditText = findViewById(R.id.noteTitleEditText);
        noteDescriptionEditText = findViewById(R.id.noteDescriptionEditText);
        notePurposeEditText = findViewById(R.id.notePurposeEditText);
        charCountTextView = findViewById(R.id.charCountTextView);
        readingTimeTextView = findViewById(R.id.readingTimeTextView);
        submitTextNoteBtn = findViewById(R.id.submitTextNoteBtn);
        textNoteBtn = findViewById(R.id.textNoteBtn);
        imageNoteBtn = findViewById(R.id.imageNoteBtn);

        // Spinner setup
        subjectList = new ArrayList<>();
        subjectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, subjectList);
        subjectSpinner.setAdapter(subjectAdapter);
        unitList = new ArrayList<>();
        unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, unitList);
        unitSpinner.setAdapter(unitAdapter);

        // Check admin status and setup both subject and unit listeners
        checkAdminStatusAndSetupListeners();

        // Add Subject Logic
        addSubjectBtn.setOnClickListener(v -> {
            EditText input = new EditText(this);
            input.setHint("Enter new subject");

            new android.app.AlertDialog.Builder(this)
                    .setTitle("Add Subject")
                    .setView(input)
                    .setPositiveButton("Add", (dialog, which) -> {
                        String newSubject = input.getText().toString().trim();
                        if (!newSubject.isEmpty()) {
                            DatabaseReference subjectsRef = database.child("classes").child(classId).child("subjects");
                            String subjectId = subjectsRef.push().getKey();
                            subjectsRef.child(subjectId).setValue(newSubject);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Add Unit Logic
        addUnitBtn.setOnClickListener(v -> {
            EditText input = new EditText(this);
            input.setHint("Enter new unit");

            new android.app.AlertDialog.Builder(this)
                    .setTitle("Add Unit")
                    .setView(input)
                    .setPositiveButton("Add", (dialog, which) -> {
                        String newUnit = input.getText().toString().trim();
                        if (!newUnit.isEmpty()) {
                            DatabaseReference unitsRef = database.child("classes").child(classId).child("units");
                            String unitId = unitsRef.push().getKey();
                            unitsRef.child(unitId).setValue(newUnit);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // TextWatcher for description field
        noteDescriptionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                int length = s.length();
                charCountTextView.setText(length + "/2000");
                readingTimeTextView.setText("Estimated Reading Time: " + getEstimatedReadingTime(s.toString()));
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // Text Note Button Click (can color toggle)
        textNoteBtn.setOnClickListener(v -> {
            Toast.makeText(this, "You're already in Text Note mode", Toast.LENGTH_SHORT).show();
        });

        // Image Note Button Redirect
        imageNoteBtn.setOnClickListener(v -> {
            Intent imageIntent = new Intent(this, ImageNotesUploadActivity.class);
            imageIntent.putExtra("classId", classId);
            startActivity(imageIntent);
        });

        // Submit Button
        submitTextNoteBtn.setOnClickListener(v -> {
            String title = noteTitleEditText.getText().toString().trim();
            String description = noteDescriptionEditText.getText().toString().trim();
            String subject = subjectSpinner.getSelectedItem().toString();
            String unit = unitSpinner.getSelectedItem().toString();
            String uploadedBy = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "unknown";

            if (title.isEmpty() || description.isEmpty() ||
                    subject.equals("Select Subject") || unit.equals("Select Unit")) {
                Toast.makeText(this, "Please fill all fields and select a subject/unit", Toast.LENGTH_SHORT).show();
                return;
            }

            // Use the unified Note class
            Note note = new Note(title, subject, unit, uploadedBy, System.currentTimeMillis(), description);

            // Reference in Firebase
            DatabaseReference notesRef = database.child("classes").child(classId).child("notes").push();

            // Upload
            notesRef.setValue(note).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(UploadTextNotesActivity.this, "Note uploaded successfully!", Toast.LENGTH_LONG).show();

                    // Clear fields
                    noteTitleEditText.setText("");
                    noteDescriptionEditText.setText("");
                    notePurposeEditText.setText("");
                    charCountTextView.setText("0/2000");
                    readingTimeTextView.setText("Estimated Reading Time: 1 min");
                } else {
                    Toast.makeText(UploadTextNotesActivity.this, "Upload failed. Try again!", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // Logic for estimated reading time (character-based)
    private String getEstimatedReadingTime(String text) {
        int length = text.trim().length();

        if (length <= 500) return "1 min";
        else if (length <= 1000) return "2 mins";
        else return "3 mins";
    }

    private void checkAdminStatusAndSetupListeners() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserEmail = mAuth.getCurrentUser().getEmail();

        database.child("classes").child(classId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String adminEmail = snapshot.child("adminEmail").getValue(String.class);
                isAdmin = currentUserEmail != null && currentUserEmail.equals(adminEmail);

                if (isAdmin) {
                    addSubjectBtn.setVisibility(View.VISIBLE);
                    addUnitBtn.setVisibility(View.VISIBLE);
                } else {
                    addSubjectBtn.setVisibility(View.GONE);
                    addUnitBtn.setVisibility(View.GONE);
                }

                setupSubjectListener();
                setupUnitListener();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UploadTextNotesActivity.this, "Failed to load class data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("UploadTextNotes", "Failed to load class data.", error.toException());
                addSubjectBtn.setVisibility(View.GONE);
                addUnitBtn.setVisibility(View.GONE);
            }
        });
    }

    private void setupSubjectListener() {
        DatabaseReference subjectsRef = database.child("classes").child(classId).child("subjects");
        subjectsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                subjectList.clear();
                subjectList.add("Select Subject");

                for (DataSnapshot subjectSnapshot : snapshot.getChildren()) {
                    String subjectName = subjectSnapshot.getValue(String.class);
                    if (subjectName != null) {
                        subjectList.add(subjectName);
                    }
                }
                subjectAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UploadTextNotesActivity.this, "Failed to load subjects: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("UploadTextNotes", "Failed to load subjects.", error.toException());
            }
        });
    }

    private void setupUnitListener() {
        DatabaseReference unitsRef = database.child("classes").child(classId).child("units");
        unitsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                unitList.clear();
                unitList.add("Select Unit");

                for (DataSnapshot unitSnapshot : snapshot.getChildren()) {
                    String unitName = unitSnapshot.getValue(String.class);
                    if (unitName != null) {
                        unitList.add(unitName);
                    }
                }
                unitAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UploadTextNotesActivity.this, "Failed to load units: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("UploadTextNotes", "Failed to load units.", error.toException());
            }
        });
    }
}*/


/*WORKING CODE
package com.example.classlink;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;


public class UploadTextNotesActivity extends AppCompatActivity {

    private Spinner subjectSpinner, unitSpinner;
    private ImageView addSubjectBtn, addUnitBtn;
    private EditText noteTitleEditText, noteDescriptionEditText, notePurposeEditText;
    private TextView charCountTextView, readingTimeTextView;
    private Button submitTextNoteBtn, textNoteBtn, imageNoteBtn;

    private List<String> subjectList;
    private ArrayAdapter<String> subjectAdapter;
    private List<String> unitList;
    private ArrayAdapter<String> unitAdapter;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference database;
    private String classId;

    // Admin status
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_textnotes);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase
                .getInstance("https://claink-49383-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("classId")) {
            classId = intent.getStringExtra("classId");
        } else {
            Toast.makeText(this, "Class ID not found. Cannot load subjects.", Toast.LENGTH_LONG).show();
            return;
        }

        // Initialize views
        subjectSpinner = findViewById(R.id.subjectSpinner);
        addSubjectBtn = findViewById(R.id.addSubjectBtn);
        unitSpinner = findViewById(R.id.unitSpinner);
        addUnitBtn = findViewById(R.id.addUnitBtn);
        noteTitleEditText = findViewById(R.id.noteTitleEditText);
        noteDescriptionEditText = findViewById(R.id.noteDescriptionEditText);
        notePurposeEditText = findViewById(R.id.notePurposeEditText);
        charCountTextView = findViewById(R.id.charCountTextView);
        readingTimeTextView = findViewById(R.id.readingTimeTextView);
        submitTextNoteBtn = findViewById(R.id.submitTextNoteBtn);
        textNoteBtn = findViewById(R.id.textNoteBtn);
        imageNoteBtn = findViewById(R.id.imageNoteBtn);

        // Setup spinners
        subjectList = new ArrayList<>();
        subjectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, subjectList);
        subjectSpinner.setAdapter(subjectAdapter);

        unitList = new ArrayList<>();
        unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, unitList);
        unitSpinner.setAdapter(unitAdapter);

        // Check admin status and load data
        checkAdminStatusAndSetupListeners();

        // Add Subject Button
        addSubjectBtn.setOnClickListener(v -> {
            if (!isAdmin) {
                Toast.makeText(this, "Only admin can add subjects", Toast.LENGTH_SHORT).show();
                return;
            }
            EditText input = new EditText(this);
            input.setHint("Enter new subject");

            new android.app.AlertDialog.Builder(this)
                    .setTitle("Add Subject")
                    .setView(input)
                    .setPositiveButton("Add", (dialog, which) -> {
                        String newSubject = input.getText().toString().trim();
                        if (!newSubject.isEmpty()) {
                            DatabaseReference subjectsRef = database.child("classes").child(classId).child("subjects");
                            String subjectId = subjectsRef.push().getKey();
                            subjectsRef.child(subjectId).setValue(newSubject);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Add Unit Button
        addUnitBtn.setOnClickListener(v -> {
            if (!isAdmin) {
                Toast.makeText(this, "Only admin can add units", Toast.LENGTH_SHORT).show();
                return;
            }
            EditText input = new EditText(this);
            input.setHint("Enter new unit");

            new android.app.AlertDialog.Builder(this)
                    .setTitle("Add Unit")
                    .setView(input)
                    .setPositiveButton("Add", (dialog, which) -> {
                        String newUnit = input.getText().toString().trim();
                        if (!newUnit.isEmpty()) {
                            DatabaseReference unitsRef = database.child("classes").child(classId).child("units");
                            String unitId = unitsRef.push().getKey();
                            unitsRef.child(unitId).setValue(newUnit);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // TextWatcher for description
        noteDescriptionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                int length = s.length();
                charCountTextView.setText(length + "/2000");
                readingTimeTextView.setText("Estimated Reading Time: " + getEstimatedReadingTime(s.toString()));
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // Text Note Button
        textNoteBtn.setOnClickListener(v -> {
            Toast.makeText(this, "You're already in Text Note mode", Toast.LENGTH_SHORT).show();
        });

        // Image Note Button
        imageNoteBtn.setOnClickListener(v -> {
            Intent imageIntent = new Intent(this, ImageNotesUploadActivity.class);
            imageIntent.putExtra("classId", classId);
            startActivity(imageIntent);
        });

        // Submit Button
        submitTextNoteBtn.setOnClickListener(v -> {
            String title = noteTitleEditText.getText().toString().trim();
            String description = noteDescriptionEditText.getText().toString().trim();
            String purpose = notePurposeEditText.getText().toString().trim();

            Object subjObj = subjectSpinner.getSelectedItem();
            Object unitObj = unitSpinner.getSelectedItem();
            String subject = subjObj == null ? "" : subjObj.toString();
            String unit = unitObj == null ? "" : unitObj.toString();

            String uploadedBy = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "unknown";

            if (title.isEmpty() || description.isEmpty()
                    || subject.equals("Select Subject") || unit.equals("Select Unit")) {
                Toast.makeText(this, "Please fill all fields and select a subject/unit", Toast.LENGTH_SHORT).show();
                return;
            }

            int charCount = description.length();
            String estimated = getEstimatedReadingTime(description);
            String type = "text"; // <-- mark this note as text

            Note note = new Note(
                    title,
                    subject,
                    unit,
                    uploadedBy,
                    System.currentTimeMillis(),
                    description,
                    charCount,
                    estimated,
                    purpose,
                    type   // <-- pass type
            );


            DatabaseReference notesRef = database.child("classes").child(classId).child("notes").push();
            notesRef.setValue(note).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(UploadTextNotesActivity.this, "✅Note uploaded successfully!", Toast.LENGTH_LONG).show();

                    noteTitleEditText.setText("");
                    noteDescriptionEditText.setText("");
                    notePurposeEditText.setText("");
                    charCountTextView.setText("0/2000");
                    readingTimeTextView.setText("Estimated Reading Time: 1 min");
                } else {
                    Toast.makeText(UploadTextNotesActivity.this, "❌Upload failed. Try again!", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private String getEstimatedReadingTime(String text) {
        int length = text.trim().length();
        if (length <= 500) return "1 min";
        else if (length <= 1000) return "2 mins";
        else return "3 mins";
    }

    private void checkAdminStatusAndSetupListeners() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserEmail = mAuth.getCurrentUser().getEmail();

        database.child("classes").child(classId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String adminEmail = snapshot.child("adminEmail").getValue(String.class);
                isAdmin = currentUserEmail != null && currentUserEmail.equals(adminEmail);

                if (isAdmin) {
                    addSubjectBtn.setVisibility(View.VISIBLE);
                    addUnitBtn.setVisibility(View.VISIBLE);
                } else {
                    addSubjectBtn.setVisibility(View.GONE);
                    addUnitBtn.setVisibility(View.GONE);
                }

                setupSubjectListener();
                setupUnitListener();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UploadTextNotesActivity.this, "Failed to load class data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("UploadTextNotes", "Failed to load class data.", error.toException());
                addSubjectBtn.setVisibility(View.GONE);
                addUnitBtn.setVisibility(View.GONE);
            }
        });
    }

    private void setupSubjectListener() {
        DatabaseReference subjectsRef = database.child("classes").child(classId).child("subjects");
        subjectsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                subjectList.clear();
                subjectList.add("Select Subject");
                for (DataSnapshot subjectSnapshot : snapshot.getChildren()) {
                    String subjectName = subjectSnapshot.getValue(String.class);
                    if (subjectName != null) {
                        subjectList.add(subjectName);
                    }
                }
                subjectAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UploadTextNotesActivity.this, "Failed to load subjects: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("UploadTextNotes", "Failed to load subjects.", error.toException());
            }
        });
    }

    private void setupUnitListener() {
        DatabaseReference unitsRef = database.child("classes").child(classId).child("units");
        unitsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                unitList.clear();
                unitList.add("Select Unit");
                for (DataSnapshot unitSnapshot : snapshot.getChildren()) {
                    String unitName = unitSnapshot.getValue(String.class);
                    if (unitName != null) {
                        unitList.add(unitName);
                    }
                }
                unitAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UploadTextNotesActivity.this, "Failed to load units: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("UploadTextNotes", "Failed to load units.", error.toException());
            }
        });
    }
}*/


/*
package com.example.classlink;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;


public class UploadTextNotesActivity extends AppCompatActivity {

    private Spinner subjectSpinner, unitSpinner;
    private ImageView addSubjectBtn, addUnitBtn;
    private ImageView deleteSubjectBtn, deleteUnitBtn; // Added delete buttons
    private EditText noteTitleEditText, noteDescriptionEditText, notePurposeEditText;
    private TextView charCountTextView, readingTimeTextView;
    private Button submitTextNoteBtn, textNoteBtn, imageNoteBtn;

    private List<String> subjectList;
    private ArrayAdapter<String> subjectAdapter;
    private List<String> unitList;
    private ArrayAdapter<String> unitAdapter;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference database;
    private String classId;

    // Admin status
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_textnotes);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase
                .getInstance("https://claink-49383-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("classId")) {
            classId = intent.getStringExtra("classId");
        } else {
            Toast.makeText(this, "Class ID not found. Cannot load subjects.", Toast.LENGTH_LONG).show();
            return;
        }

        // Initialize views
        subjectSpinner = findViewById(R.id.subjectSpinner);
        addSubjectBtn = findViewById(R.id.addSubjectBtn);
        unitSpinner = findViewById(R.id.unitSpinner);
        addUnitBtn = findViewById(R.id.addUnitBtn);
        noteTitleEditText = findViewById(R.id.noteTitleEditText);
        noteDescriptionEditText = findViewById(R.id.noteDescriptionEditText);
        notePurposeEditText = findViewById(R.id.notePurposeEditText);
        charCountTextView = findViewById(R.id.charCountTextView);
        readingTimeTextView = findViewById(R.id.readingTimeTextView);
        submitTextNoteBtn = findViewById(R.id.submitTextNoteBtn);
        textNoteBtn = findViewById(R.id.textNoteBtn);
        imageNoteBtn = findViewById(R.id.imageNoteBtn);
        deleteSubjectBtn = findViewById(R.id.deleteSubjectBtn);
        deleteUnitBtn = findViewById(R.id.deleteUnitBtn);

        // Setup spinners
        subjectList = new ArrayList<>();
        subjectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, subjectList);
        subjectSpinner.setAdapter(subjectAdapter);

        unitList = new ArrayList<>();
        unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, unitList);
        unitSpinner.setAdapter(unitAdapter);

        // Check admin status and load data
        checkAdminStatusAndSetupListeners();

        // Add Subject Button
        addSubjectBtn.setOnClickListener(v -> {
            if (!isAdmin) {
                Toast.makeText(this, "Only admin can add subjects", Toast.LENGTH_SHORT).show();
                return;
            }
            EditText input = new EditText(this);
            input.setHint("Enter new subject");

            new android.app.AlertDialog.Builder(this)
                    .setTitle("Add Subject")
                    .setView(input)
                    .setPositiveButton("Add", (dialog, which) -> {
                        String newSubject = input.getText().toString().trim();
                        if (!newSubject.isEmpty()) {
                            DatabaseReference subjectsRef = database.child("classes").child(classId).child("subjects");
                            String subjectId = subjectsRef.push().getKey();
                            subjectsRef.child(subjectId).setValue(newSubject);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Add Unit Button
        addUnitBtn.setOnClickListener(v -> {
            if (!isAdmin) {
                Toast.makeText(this, "Only admin can add units", Toast.LENGTH_SHORT).show();
                return;
            }
            EditText input = new EditText(this);
            input.setHint("Enter new unit");

            new android.app.AlertDialog.Builder(this)
                    .setTitle("Add Unit")
                    .setView(input)
                    .setPositiveButton("Add", (dialog, which) -> {
                        String newUnit = input.getText().toString().trim();
                        if (!newUnit.isEmpty()) {
                            DatabaseReference unitsRef = database.child("classes").child(classId).child("units");
                            String unitId = unitsRef.push().getKey();
                            unitsRef.child(unitId).setValue(newUnit);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // TextWatcher for description
        noteDescriptionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                int length = s.length();
                charCountTextView.setText(length + "/2000");
                readingTimeTextView.setText("Estimated Reading Time: " + getEstimatedReadingTime(s.toString()));
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // Text Note Button
        textNoteBtn.setOnClickListener(v -> {
            Toast.makeText(this, "You're already in Text Note mode", Toast.LENGTH_SHORT).show();
        });

        // Image Note Button
        imageNoteBtn.setOnClickListener(v -> {
            Intent imageIntent = new Intent(this, ImageNotesUploadActivity.class);
            imageIntent.putExtra("classId", classId);
            startActivity(imageIntent);
        });

        // Submit Button
        submitTextNoteBtn.setOnClickListener(v -> {
            String title = noteTitleEditText.getText().toString().trim();
            String description = noteDescriptionEditText.getText().toString().trim();
            String purpose = notePurposeEditText.getText().toString().trim();

            Object subjObj = subjectSpinner.getSelectedItem();
            Object unitObj = unitSpinner.getSelectedItem();
            String subject = subjObj == null ? "" : subjObj.toString();
            String unit = unitObj == null ? "" : unitObj.toString();

            String uploadedBy = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "unknown";

            if (title.isEmpty() || description.isEmpty()
                    || subject.equals("Select Subject") || unit.equals("Select Unit")) {
                Toast.makeText(this, "Please fill all fields and select a subject/unit", Toast.LENGTH_SHORT).show();
                return;
            }

            int charCount = description.length();
            String estimated = getEstimatedReadingTime(description);
            String type = "text"; // <-- mark this note as text

            Note note = new Note(
                    title,
                    subject,
                    unit,
                    uploadedBy,
                    System.currentTimeMillis(),
                    description,
                    charCount,
                    estimated,
                    purpose,
                    type   // <-- pass type
            );


            DatabaseReference notesRef = database.child("classes").child(classId).child("notes").push();
            notesRef.setValue(note).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(UploadTextNotesActivity.this, "✅Note uploaded successfully!", Toast.LENGTH_LONG).show();

                    noteTitleEditText.setText("");
                    noteDescriptionEditText.setText("");
                    notePurposeEditText.setText("");
                    charCountTextView.setText("0/2000");
                    readingTimeTextView.setText("Estimated Reading Time: 1 min");
                } else {
                    Toast.makeText(UploadTextNotesActivity.this, "❌Upload failed. Try again!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Delete Subject Button
        deleteSubjectBtn.setOnClickListener(v -> {
            if (!isAdmin) {
                Toast.makeText(this, "Only admin can delete subjects", Toast.LENGTH_SHORT).show();
                return;
            }
            int selectedPosition = subjectSpinner.getSelectedItemPosition();
            if (selectedPosition > 0) { // Position 0 is "Select Subject"
                String selectedSubject = subjectList.get(selectedPosition);
                new android.app.AlertDialog.Builder(this)
                        .setTitle("Delete Subject")
                        .setMessage("Are you sure you want to delete '" + selectedSubject + "'? This cannot be undone.")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            // Find the subject in Firebase and remove it
                            database.child("classes").child(classId).child("subjects")
                                    .orderByValue().equalTo(selectedSubject)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChildren()) {
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    snapshot.getRef().removeValue();
                                                    Toast.makeText(UploadTextNotesActivity.this, "Subject deleted successfully.", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Toast.makeText(UploadTextNotesActivity.this, "Failed to delete: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                Toast.makeText(this, "Please select a subject to delete.", Toast.LENGTH_SHORT).show();
            }
        });

        // Delete Unit Button
        deleteUnitBtn.setOnClickListener(v -> {
            if (!isAdmin) {
                Toast.makeText(this, "Only admin can delete units", Toast.LENGTH_SHORT).show();
                return;
            }
            int selectedPosition = unitSpinner.getSelectedItemPosition();
            if (selectedPosition > 0) { // Position 0 is "Select Unit"
                String selectedUnit = unitList.get(selectedPosition);
                new android.app.AlertDialog.Builder(this)
                        .setTitle("Delete Unit")
                        .setMessage("Are you sure you want to delete '" + selectedUnit + "'? This cannot be undone.")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            // Find the unit in Firebase and remove it
                            database.child("classes").child(classId).child("units")
                                    .orderByValue().equalTo(selectedUnit)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChildren()) {
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    snapshot.getRef().removeValue();
                                                    Toast.makeText(UploadTextNotesActivity.this, "Unit deleted successfully.", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Toast.makeText(UploadTextNotesActivity.this, "Failed to delete: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                Toast.makeText(this, "Please select a unit to delete.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getEstimatedReadingTime(String text) {
        int length = text.trim().length();
        if (length <= 500) return "1 min";
        else if (length <= 1000) return "2 mins";
        else return "3 mins";
    }

    private void checkAdminStatusAndSetupListeners() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserEmail = mAuth.getCurrentUser().getEmail();

        database.child("classes").child(classId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String adminEmail = snapshot.child("adminEmail").getValue(String.class);
                isAdmin = currentUserEmail != null && currentUserEmail.equals(adminEmail);

                if (isAdmin) {
                    addSubjectBtn.setVisibility(View.VISIBLE);
                    addUnitBtn.setVisibility(View.VISIBLE);
                    deleteSubjectBtn.setVisibility(View.VISIBLE);
                    deleteUnitBtn.setVisibility(View.VISIBLE);
                } else {
                    addSubjectBtn.setVisibility(View.GONE);
                    addUnitBtn.setVisibility(View.GONE);
                    deleteSubjectBtn.setVisibility(View.GONE);
                    deleteUnitBtn.setVisibility(View.GONE);
                }

                setupSubjectListener();
                setupUnitListener();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UploadTextNotesActivity.this, "Failed to load class data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("UploadTextNotes", "Failed to load class data.", error.toException());
                addSubjectBtn.setVisibility(View.GONE);
                addUnitBtn.setVisibility(View.GONE);
                deleteSubjectBtn.setVisibility(View.GONE);
                deleteUnitBtn.setVisibility(View.GONE);
            }
        });
    }

    private void setupSubjectListener() {
        DatabaseReference subjectsRef = database.child("classes").child(classId).child("subjects");
        subjectsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                subjectList.clear();
                subjectList.add("Select Subject");
                for (DataSnapshot subjectSnapshot : snapshot.getChildren()) {
                    String subjectName = subjectSnapshot.getValue(String.class);
                    if (subjectName != null) {
                        subjectList.add(subjectName);
                    }
                }
                subjectAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UploadTextNotesActivity.this, "Failed to load subjects: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("UploadTextNotes", "Failed to load subjects.", error.toException());
            }
        });
    }

    private void setupUnitListener() {
        DatabaseReference unitsRef = database.child("classes").child(classId).child("units");
        unitsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                unitList.clear();
                unitList.add("Select Unit");
                for (DataSnapshot unitSnapshot : snapshot.getChildren()) {
                    String unitName = unitSnapshot.getValue(String.class);
                    if (unitName != null) {
                        unitList.add(unitName);
                    }
                }
                unitAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UploadTextNotesActivity.this, "Failed to load units: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("UploadTextNotes", "Failed to load units.", error.toException());
            }
        });
    }
}
*/


/*last working code
package com.example.classlink;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;


public class UploadTextNotesActivity extends AppCompatActivity {

    private Spinner subjectSpinner, unitSpinner;
    private ImageView addSubjectBtn, addUnitBtn;
    private ImageView deleteSubjectBtn, deleteUnitBtn; // Added delete buttons
    private EditText noteTitleEditText, noteDescriptionEditText, notePurposeEditText;
    private TextView charCountTextView, readingTimeTextView;
    private Button submitTextNoteBtn, textNoteBtn, imageNoteBtn;

    private List<String> subjectList;
    private ArrayAdapter<String> subjectAdapter;
    private List<String> unitList;
    private ArrayAdapter<String> unitAdapter;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference database;
    private String classId;

    // Admin status
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_textnotes);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase
                .getInstance("https://claink-49383-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("classId")) {
            classId = intent.getStringExtra("classId");
        } else {
            Toast.makeText(this, "Class ID not found. Cannot load subjects.", Toast.LENGTH_LONG).show();
            return;
        }

        // Initialize views
        subjectSpinner = findViewById(R.id.subjectSpinner);
        addSubjectBtn = findViewById(R.id.addSubjectBtn);
        unitSpinner = findViewById(R.id.unitSpinner);
        addUnitBtn = findViewById(R.id.addUnitBtn);
        noteTitleEditText = findViewById(R.id.noteTitleEditText);
        noteDescriptionEditText = findViewById(R.id.noteDescriptionEditText);
        notePurposeEditText = findViewById(R.id.notePurposeEditText);
        charCountTextView = findViewById(R.id.charCountTextView);
        readingTimeTextView = findViewById(R.id.readingTimeTextView);
        submitTextNoteBtn = findViewById(R.id.submitTextNoteBtn);
        textNoteBtn = findViewById(R.id.textNoteBtn);
        imageNoteBtn = findViewById(R.id.imageNoteBtn);
        deleteSubjectBtn = findViewById(R.id.deleteSubjectBtn);
        deleteUnitBtn = findViewById(R.id.deleteUnitBtn);

        // Setup spinners
        subjectList = new ArrayList<>();
        subjectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, subjectList);
        subjectSpinner.setAdapter(subjectAdapter);

        unitList = new ArrayList<>();
        unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, unitList);
        unitSpinner.setAdapter(unitAdapter);

        // Check admin status and load data
        checkAdminStatusAndSetupListeners();

        // Add Subject Button
        addSubjectBtn.setOnClickListener(v -> {
            if (!isAdmin) {
                Toast.makeText(this, "Only admin can add subjects", Toast.LENGTH_SHORT).show();
                return;
            }
            EditText input = new EditText(this);
            input.setHint("Enter new subject");

            new android.app.AlertDialog.Builder(this)
                    .setTitle("Add Subject")
                    .setView(input)
                    .setPositiveButton("Add", (dialog, which) -> {
                        String newSubject = input.getText().toString().trim();
                        if (!newSubject.isEmpty()) {
                            DatabaseReference subjectsRef = database.child("classes").child(classId).child("subjects");
                            String subjectId = subjectsRef.push().getKey();
                            subjectsRef.child(subjectId).setValue(newSubject);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Add Unit Button
        addUnitBtn.setOnClickListener(v -> {
            if (!isAdmin) {
                Toast.makeText(this, "Only admin can add units", Toast.LENGTH_SHORT).show();
                return;
            }
            EditText input = new EditText(this);
            input.setHint("Enter new unit");

            new android.app.AlertDialog.Builder(this)
                    .setTitle("Add Unit")
                    .setView(input)
                    .setPositiveButton("Add", (dialog, which) -> {
                        String newUnit = input.getText().toString().trim();
                        if (!newUnit.isEmpty()) {
                            DatabaseReference unitsRef = database.child("classes").child(classId).child("units");
                            String unitId = unitsRef.push().getKey();
                            unitsRef.child(unitId).setValue(newUnit);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // TextWatcher for description
        noteDescriptionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                int length = s.length();
                charCountTextView.setText(length + "/2000");
                readingTimeTextView.setText("Estimated Reading Time: " + getEstimatedReadingTime(s.toString()));
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int count, int before) {}
        });

        // Text Note Button
        textNoteBtn.setOnClickListener(v -> {
            Toast.makeText(this, "You're already in Text Note mode", Toast.LENGTH_SHORT).show();
        });

        // Image Note Button
        imageNoteBtn.setOnClickListener(v -> {
            Intent imageIntent = new Intent(this, ImageNotesUploadActivity.class);
            imageIntent.putExtra("classId", classId);
            startActivity(imageIntent);
        });

        // Submit Button
        submitTextNoteBtn.setOnClickListener(v -> {
            String title = noteTitleEditText.getText().toString().trim();
            String description = noteDescriptionEditText.getText().toString().trim();
            String purpose = notePurposeEditText.getText().toString().trim();

            Object subjObj = subjectSpinner.getSelectedItem();
            Object unitObj = unitSpinner.getSelectedItem();
            String subject = subjObj == null ? "" : subjObj.toString();
            String unit = unitObj == null ? "" : unitObj.toString();

            String uploadedBy = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "unknown";

            if (title.isEmpty() || description.isEmpty()
                    || subject.equals("Select Subject") || unit.equals("Select Unit")) {
                Toast.makeText(this, "Please fill all fields and select a subject/unit", Toast.LENGTH_SHORT).show();
                return;
            }

            int charCount = description.length();
            String estimated = getEstimatedReadingTime(description);
            String type = "text"; // <-- mark this note as text

            Note note = new Note(
                    title,
                    subject,
                    unit,
                    uploadedBy,
                    System.currentTimeMillis(),
                    description,
                    charCount,
                    estimated,
                    purpose,
                    type   // <-- pass type
            );


            DatabaseReference notesRef = database.child("classes").child(classId).child("notes").push();
            notesRef.setValue(note).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(UploadTextNotesActivity.this, "✅Note uploaded successfully!", Toast.LENGTH_LONG).show();

                    // Trigger notification
                    sendNotification(title, description);

                    noteTitleEditText.setText("");
                    noteDescriptionEditText.setText("");
                    notePurposeEditText.setText("");
                    charCountTextView.setText("0/2000");
                    readingTimeTextView.setText("Estimated Reading Time: 1 min");
                } else {
                    Toast.makeText(UploadTextNotesActivity.this, "❌Upload failed. Try again!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Delete Subject Button
        deleteSubjectBtn.setOnClickListener(v -> {
            if (!isAdmin) {
                Toast.makeText(this, "Only admin can delete subjects", Toast.LENGTH_SHORT).show();
                return;
            }
            int selectedPosition = subjectSpinner.getSelectedItemPosition();
            if (selectedPosition > 0) { // Position 0 is "Select Subject"
                String selectedSubject = subjectList.get(selectedPosition);
                new android.app.AlertDialog.Builder(this)
                        .setTitle("Delete Subject")
                        .setMessage("Are you sure you want to delete '" + selectedSubject + "'? This cannot be undone.")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            // Find the subject in Firebase and remove it
                            database.child("classes").child(classId).child("subjects")
                                    .orderByValue().equalTo(selectedSubject)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChildren()) {
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    snapshot.getRef().removeValue();
                                                    Toast.makeText(UploadTextNotesActivity.this, "Subject deleted successfully.", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Toast.makeText(UploadTextNotesActivity.this, "Failed to delete: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                Toast.makeText(this, "Please select a subject to delete.", Toast.LENGTH_SHORT).show();
            }
        });

        // Delete Unit Button
        deleteUnitBtn.setOnClickListener(v -> {
            if (!isAdmin) {
                Toast.makeText(this, "Only admin can delete units", Toast.LENGTH_SHORT).show();
                return;
            }
            int selectedPosition = unitSpinner.getSelectedItemPosition();
            if (selectedPosition > 0) { // Position 0 is "Select Unit"
                String selectedUnit = unitList.get(selectedPosition);
                new android.app.AlertDialog.Builder(this)
                        .setTitle("Delete Unit")
                        .setMessage("Are you sure you want to delete '" + selectedUnit + "'? This cannot be undone.")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            // Find the unit in Firebase and remove it
                            database.child("classes").child(classId).child("units")
                                    .orderByValue().equalTo(selectedUnit)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChildren()) {
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    snapshot.getRef().removeValue();
                                                    Toast.makeText(UploadTextNotesActivity.this, "Unit deleted successfully.", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Toast.makeText(UploadTextNotesActivity.this, "Failed to delete: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                Toast.makeText(this, "Please select a unit to delete.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getEstimatedReadingTime(String text) {
        int length = text.trim().length();
        if (length <= 500) return "1 min";
        else if (length <= 1000) return "2 mins";
        else return "3 mins";
    }

    private void checkAdminStatusAndSetupListeners() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserEmail = mAuth.getCurrentUser().getEmail();

        database.child("classes").child(classId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String adminEmail = snapshot.child("adminEmail").getValue(String.class);
                isAdmin = currentUserEmail != null && currentUserEmail.equals(adminEmail);

                if (isAdmin) {
                    addSubjectBtn.setVisibility(View.VISIBLE);
                    addUnitBtn.setVisibility(View.VISIBLE);
                    deleteSubjectBtn.setVisibility(View.VISIBLE);
                    deleteUnitBtn.setVisibility(View.VISIBLE);
                } else {
                    addSubjectBtn.setVisibility(View.GONE);
                    addUnitBtn.setVisibility(View.GONE);
                    deleteSubjectBtn.setVisibility(View.GONE);
                    deleteUnitBtn.setVisibility(View.GONE);
                }

                setupSubjectListener();
                setupUnitListener();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UploadTextNotesActivity.this, "Failed to load class data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("UploadTextNotes", "Failed to load class data.", error.toException());
                addSubjectBtn.setVisibility(View.GONE);
                addUnitBtn.setVisibility(View.GONE);
                deleteSubjectBtn.setVisibility(View.GONE);
                deleteUnitBtn.setVisibility(View.GONE);
            }
        });
    }

    private void setupSubjectListener() {
        DatabaseReference subjectsRef = database.child("classes").child(classId).child("subjects");
        subjectsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                subjectList.clear();
                subjectList.add("Select Subject");
                for (DataSnapshot subjectSnapshot : snapshot.getChildren()) {
                    String subjectName = subjectSnapshot.getValue(String.class);
                    if (subjectName != null) {
                        subjectList.add(subjectName);
                    }
                }
                subjectAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UploadTextNotesActivity.this, "Failed to load subjects: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("UploadTextNotes", "Failed to load subjects.", error.toException());
            }
        });
    }

    private void setupUnitListener() {
        DatabaseReference unitsRef = database.child("classes").child(classId).child("units");
        unitsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                unitList.clear();
                unitList.add("Select Unit");
                for (DataSnapshot unitSnapshot : snapshot.getChildren()) {
                    String unitName = unitSnapshot.getValue(String.class);
                    if (unitName != null) {
                        unitList.add(unitName);
                    }
                }
                unitAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UploadTextNotesActivity.this, "Failed to load units: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("UploadTextNotes", "Failed to load units.", error.toException());
            }
        });
    }

    // New method to send a notification
    private void sendNotification(String title, String message) {
        NotificationHelper.sendNotification(this, title, message);
    }
} dont use this codes*/


/*
working*/
/*
package com.example.classlink;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

public class UploadTextNotesActivity extends AppCompatActivity {

    private Spinner subjectSpinner, unitSpinner;
    private ImageView addSubjectBtn, addUnitBtn;
    private ImageView deleteSubjectBtn, deleteUnitBtn;
    private EditText noteTitleEditText, noteDescriptionEditText, notePurposeEditText;
    private TextView charCountTextView, readingTimeTextView;
    private Button submitTextNoteBtn, textNoteBtn, imageNoteBtn;

    private List<String> subjectList;
    private ArrayAdapter<String> subjectAdapter;
    private List<String> unitList;
    private ArrayAdapter<String> unitAdapter;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference database;
    private String classId;

    // Admin status
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_textnotes);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase
                .getInstance("https://claink-49383-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("classId")) {
            classId = intent.getStringExtra("classId");
        } else {
            Toast.makeText(this, "Class ID not found. Cannot load subjects.", Toast.LENGTH_LONG).show();
            return;
        }

        // Initialize views
        subjectSpinner = findViewById(R.id.subjectSpinner);
        addSubjectBtn = findViewById(R.id.addSubjectBtn);
        unitSpinner = findViewById(R.id.unitSpinner);
        addUnitBtn = findViewById(R.id.addUnitBtn);
        noteTitleEditText = findViewById(R.id.noteTitleEditText);
        noteDescriptionEditText = findViewById(R.id.noteDescriptionEditText);
        notePurposeEditText = findViewById(R.id.notePurposeEditText);
        charCountTextView = findViewById(R.id.charCountTextView);
        readingTimeTextView = findViewById(R.id.readingTimeTextView);
        submitTextNoteBtn = findViewById(R.id.submitTextNoteBtn);
        textNoteBtn = findViewById(R.id.textNoteBtn);
        imageNoteBtn = findViewById(R.id.imageNoteBtn);
        deleteSubjectBtn = findViewById(R.id.deleteSubjectBtn);
        deleteUnitBtn = findViewById(R.id.deleteUnitBtn);

        // Setup spinners
        subjectList = new ArrayList<>();
        subjectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, subjectList);
        subjectSpinner.setAdapter(subjectAdapter);

        unitList = new ArrayList<>();
        unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, unitList);
        unitSpinner.setAdapter(unitAdapter);




        // Check admin status and load data
        checkAdminStatusAndSetupListeners();

        // Get and save the user's FCM token
        if (mAuth.getCurrentUser() != null) {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                    return;
                }
                String token = task.getResult();
                String userId = mAuth.getCurrentUser().getUid();
                database.child("users").child(userId).child("fcmToken").setValue(token);
                database.child("users").child(userId).child("classId").setValue(classId);
            });
        }


        // Add Subject Button
        addSubjectBtn.setOnClickListener(v -> {
            if (!isAdmin) {
                Toast.makeText(this, "Only admin can add subjects", Toast.LENGTH_SHORT).show();
                return;
            }
            EditText input = new EditText(this);
            input.setHint("Enter new subject");

            new android.app.AlertDialog.Builder(this)
                    .setTitle("Add Subject")
                    .setView(input)
                    .setPositiveButton("Add", (dialog, which) -> {
                        String newSubject = input.getText().toString().trim();
                        if (!newSubject.isEmpty()) {
                            DatabaseReference subjectsRef = database.child("classes").child(classId).child("subjects");
                            String subjectId = subjectsRef.push().getKey();
                            subjectsRef.child(subjectId).setValue(newSubject);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Add Unit Button
        addUnitBtn.setOnClickListener(v -> {
            if (!isAdmin) {
                Toast.makeText(this, "Only admin can add units", Toast.LENGTH_SHORT).show();
                return;
            }
            EditText input = new EditText(this);
            input.setHint("Enter new unit");

            new android.app.AlertDialog.Builder(this)
                    .setTitle("Add Unit")
                    .setView(input)
                    .setPositiveButton("Add", (dialog, which) -> {
                        String newUnit = input.getText().toString().trim();
                        if (!newUnit.isEmpty()) {
                            DatabaseReference unitsRef = database.child("classes").child(classId).child("units");
                            String unitId = unitsRef.push().getKey();
                            unitsRef.child(unitId).setValue(newUnit);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // TextWatcher for description
        noteDescriptionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                int length = s.length();
                charCountTextView.setText(length + "/2000");
                readingTimeTextView.setText("Estimated Reading Time: " + getEstimatedReadingTime(s.toString()));
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int count, int before) {}
        });

        // Text Note Button
        textNoteBtn.setOnClickListener(v -> {
            Toast.makeText(this, "You're already in Text Note mode", Toast.LENGTH_SHORT).show();
        });

        // Image Note Button
        imageNoteBtn.setOnClickListener(v -> {
            Intent imageIntent = new Intent(this, ImageNotesUploadActivity.class);
            imageIntent.putExtra("classId", classId);
            startActivity(imageIntent);
        });

        // Submit Button
        submitTextNoteBtn.setOnClickListener(v -> {
            String title = noteTitleEditText.getText().toString().trim();
            String description = noteDescriptionEditText.getText().toString().trim();
            String purpose = notePurposeEditText.getText().toString().trim();

            Object subjObj = subjectSpinner.getSelectedItem();
            Object unitObj = unitSpinner.getSelectedItem();
            String subject = subjObj == null ? "" : subjObj.toString();
            String unit = unitObj == null ? "" : unitObj.toString();

            String uploadedBy = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "unknown";

            if (title.isEmpty() || description.isEmpty()
                    || subject.equals("Select Subject") || unit.equals("Select Unit")) {
                Toast.makeText(this, "Please fill all fields and select a subject/unit", Toast.LENGTH_SHORT).show();
                return;
            }

            int charCount = description.length();
            String estimated = getEstimatedReadingTime(description);
            String type = "text"; // <-- mark this note as text

            Note note = new Note(
                    title,
                    subject,
                    unit,
                    uploadedBy,
                    System.currentTimeMillis(),
                    description,
                    charCount,
                    estimated,
                    purpose,
                    type   // <-- pass type
            );

            DatabaseReference notesRef = database.child("classes").child(classId).child("notes").push();
            notesRef.setValue(note).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(UploadTextNotesActivity.this, "✅Note uploaded successfully!", Toast.LENGTH_LONG).show();

                    // Notifications will be handled by a Firebase Cloud Function.
                    // No need to call sendNotification() from here.

                    noteTitleEditText.setText("");
                    noteDescriptionEditText.setText("");
                    notePurposeEditText.setText("");
                    charCountTextView.setText("0/2000");
                    readingTimeTextView.setText("Estimated Reading Time: 1 min");
                } else {
                    Toast.makeText(UploadTextNotesActivity.this, "❌Upload failed. Try again!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Delete Subject Button
        deleteSubjectBtn.setOnClickListener(v -> {
            if (!isAdmin) {
                Toast.makeText(this, "Only admin can delete subjects", Toast.LENGTH_SHORT).show();
                return;
            }
            int selectedPosition = subjectSpinner.getSelectedItemPosition();
            if (selectedPosition > 0) { // Position 0 is "Select Subject"
                String selectedSubject = subjectList.get(selectedPosition);
                new android.app.AlertDialog.Builder(this)
                        .setTitle("Delete Subject")
                        .setMessage("Are you sure you want to delete '" + selectedSubject + "'? This cannot be undone.")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            // Find the subject in Firebase and remove it
                            database.child("classes").child(classId).child("subjects")
                                    .orderByValue().equalTo(selectedSubject)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChildren()) {
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    snapshot.getRef().removeValue();
                                                    Toast.makeText(UploadTextNotesActivity.this, "Subject deleted successfully.", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Toast.makeText(UploadTextNotesActivity.this, "Failed to delete: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                Toast.makeText(this, "Please select a subject to delete.", Toast.LENGTH_SHORT).show();
            }
        });

        // Delete Unit Button
        deleteUnitBtn.setOnClickListener(v -> {
            if (!isAdmin) {
                Toast.makeText(this, "Only admin can delete units", Toast.LENGTH_SHORT).show();
                return;
            }
            int selectedPosition = unitSpinner.getSelectedItemPosition();
            if (selectedPosition > 0) { // Position 0 is "Select Unit"
                String selectedUnit = unitList.get(selectedPosition);
                new android.app.AlertDialog.Builder(this)
                        .setTitle("Delete Unit")
                        .setMessage("Are you sure you want to delete '" + selectedUnit + "'? This cannot be undone.")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            // Find the unit in Firebase and remove it
                            database.child("classes").child(classId).child("units")
                                    .orderByValue().equalTo(selectedUnit)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChildren()) {
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    snapshot.getRef().removeValue();
                                                    Toast.makeText(UploadTextNotesActivity.this, "Unit deleted successfully.", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Toast.makeText(UploadTextNotesActivity.this, "Failed to delete: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                Toast.makeText(this, "Please select a unit to delete.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getEstimatedReadingTime(String text) {
        int length = text.trim().length();
        if (length <= 500) return "1 min";
        else if (length <= 1000) return "2 mins";
        else return "3 mins";
    }

    private void checkAdminStatusAndSetupListeners() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserEmail = mAuth.getCurrentUser().getEmail();

        database.child("classes").child(classId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String adminEmail = snapshot.child("adminEmail").getValue(String.class);
                isAdmin = currentUserEmail != null && currentUserEmail.equals(adminEmail);

                if (isAdmin) {
                    addSubjectBtn.setVisibility(View.VISIBLE);
                    addUnitBtn.setVisibility(View.VISIBLE);
                    deleteSubjectBtn.setVisibility(View.VISIBLE);
                    deleteUnitBtn.setVisibility(View.VISIBLE);
                } else {
                    addSubjectBtn.setVisibility(View.GONE);
                    addUnitBtn.setVisibility(View.GONE);
                    deleteSubjectBtn.setVisibility(View.GONE);
                    deleteUnitBtn.setVisibility(View.GONE);
                }

                setupSubjectListener();
                setupUnitListener();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UploadTextNotesActivity.this, "Failed to load class data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("UploadTextNotes", "Failed to load class data.", error.toException());
                addSubjectBtn.setVisibility(View.GONE);
                addUnitBtn.setVisibility(View.GONE);
                deleteSubjectBtn.setVisibility(View.GONE);
                deleteUnitBtn.setVisibility(View.GONE);
            }
        });
    }

    private void setupSubjectListener() {
        DatabaseReference subjectsRef = database.child("classes").child(classId).child("subjects");
        subjectsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                subjectList.clear();
                subjectList.add("Select Subject");
                for (DataSnapshot subjectSnapshot : snapshot.getChildren()) {
                    String subjectName = subjectSnapshot.getValue(String.class);
                    if (subjectName != null) {
                        subjectList.add(subjectName);
                    }
                }
                subjectAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UploadTextNotesActivity.this, "Failed to load subjects: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("UploadTextNotes", "Failed to load subjects.", error.toException());
            }
        });
    }

    private void setupUnitListener() {
        DatabaseReference unitsRef = database.child("classes").child(classId).child("units");
        unitsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                unitList.clear();
                unitList.add("Select Unit");
                for (DataSnapshot unitSnapshot : snapshot.getChildren()) {
                    String unitName = unitSnapshot.getValue(String.class);
                    if (unitName != null) {
                        unitList.add(unitName);
                    }
                }
                unitAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UploadTextNotesActivity.this, "Failed to load units: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("UploadTextNotes", "Failed to load units.", error.toException());
            }
        });
    }
}*/










package com.example.classlink;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

public class UploadTextNotesActivity extends AppCompatActivity {

    private Spinner subjectSpinner, unitSpinner;
    private ImageView addSubjectBtn, addUnitBtn;
    private ImageView deleteSubjectBtn, deleteUnitBtn;
    private EditText noteTitleEditText, noteDescriptionEditText, notePurposeEditText;
    private TextView charCountTextView, readingTimeTextView;
    private Button submitTextNoteBtn, textNoteBtn, imageNoteBtn;

    private List<String> subjectList;
    private ArrayAdapter<String> subjectAdapter;
    private List<String> unitList;
    private ArrayAdapter<String> unitAdapter;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference database;
    private String classId;

    // Admin status
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_textnotes);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase
                .getInstance("https://claink-49383-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("classId")) {
            classId = intent.getStringExtra("classId");
        } else {
            Toast.makeText(this, "Class ID not found. Cannot load subjects.", Toast.LENGTH_LONG).show();
            return;
        }

        // Initialize views
        subjectSpinner = findViewById(R.id.subjectSpinner);
        addSubjectBtn = findViewById(R.id.addSubjectBtn);
        unitSpinner = findViewById(R.id.unitSpinner);
        addUnitBtn = findViewById(R.id.addUnitBtn);
        noteTitleEditText = findViewById(R.id.noteTitleEditText);
        noteDescriptionEditText = findViewById(R.id.noteDescriptionEditText);
        notePurposeEditText = findViewById(R.id.notePurposeEditText);
        charCountTextView = findViewById(R.id.charCountTextView);
        readingTimeTextView = findViewById(R.id.readingTimeTextView);
        submitTextNoteBtn = findViewById(R.id.submitTextNoteBtn);
        textNoteBtn = findViewById(R.id.textNoteBtn);
        imageNoteBtn = findViewById(R.id.imageNoteBtn);
        deleteSubjectBtn = findViewById(R.id.deleteSubjectBtn);
        deleteUnitBtn = findViewById(R.id.deleteUnitBtn);

        // --- START OF CHANGES ---

        // Setup spinners
        subjectList = new ArrayList<>();
        subjectAdapter = new BlackTextAdapter(this, android.R.layout.simple_spinner_item, subjectList);
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjectSpinner.setAdapter(subjectAdapter);

        unitList = new ArrayList<>();
        unitAdapter = new BlackTextAdapter(this, android.R.layout.simple_spinner_item, unitList);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitSpinner.setAdapter(unitAdapter);

        // --- END OF CHANGES ---

        // Check admin status and load data
        checkAdminStatusAndSetupListeners();

        // Get and save the user's FCM token
        if (mAuth.getCurrentUser() != null) {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                    return;
                }
                String token = task.getResult();
                String userId = mAuth.getCurrentUser().getUid();
                database.child("users").child(userId).child("fcmToken").setValue(token);
                database.child("users").child(userId).child("classId").setValue(classId);
            });
        }


        // Add Subject Button
        addSubjectBtn.setOnClickListener(v -> {
            if (!isAdmin) {
                Toast.makeText(this, "Only admin can add subjects", Toast.LENGTH_SHORT).show();
                return;
            }
            EditText input = new EditText(this);
            input.setHint("Enter new subject");

            new android.app.AlertDialog.Builder(this)
                    .setTitle("Add Subject")
                    .setView(input)
                    .setPositiveButton("Add", (dialog, which) -> {
                        String newSubject = input.getText().toString().trim();
                        if (!newSubject.isEmpty()) {
                            DatabaseReference subjectsRef = database.child("classes").child(classId).child("subjects");
                            String subjectId = subjectsRef.push().getKey();
                            subjectsRef.child(subjectId).setValue(newSubject);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Add Unit Button
        addUnitBtn.setOnClickListener(v -> {
            if (!isAdmin) {
                Toast.makeText(this, "Only admin can add units", Toast.LENGTH_SHORT).show();
                return;
            }
            EditText input = new EditText(this);
            input.setHint("Enter new unit");

            new android.app.AlertDialog.Builder(this)
                    .setTitle("Add Unit")
                    .setView(input)
                    .setPositiveButton("Add", (dialog, which) -> {
                        String newUnit = input.getText().toString().trim();
                        if (!newUnit.isEmpty()) {
                            DatabaseReference unitsRef = database.child("classes").child(classId).child("units");
                            String unitId = unitsRef.push().getKey();
                            unitsRef.child(unitId).setValue(newUnit);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // TextWatcher for description
        noteDescriptionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                int length = s.length();
                charCountTextView.setText(length + "/2000");
                readingTimeTextView.setText("Estimated Reading Time: " + getEstimatedReadingTime(s.toString()));
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int count, int before) {}
        });

        // Text Note Button
        textNoteBtn.setOnClickListener(v -> {
            Toast.makeText(this, "You're already in Text Note mode", Toast.LENGTH_SHORT).show();
        });

        // Image Note Button
        imageNoteBtn.setOnClickListener(v -> {
            Intent imageIntent = new Intent(this, ImageNotesUploadActivity.class);
            imageIntent.putExtra("classId", classId);
            startActivity(imageIntent);
        });

        // Submit Button
        submitTextNoteBtn.setOnClickListener(v -> {
            String title = noteTitleEditText.getText().toString().trim();
            String description = noteDescriptionEditText.getText().toString().trim();
            String purpose = notePurposeEditText.getText().toString().trim();

            Object subjObj = subjectSpinner.getSelectedItem();
            Object unitObj = unitSpinner.getSelectedItem();
            String subject = subjObj == null ? "" : subjObj.toString();
            String unit = unitObj == null ? "" : unitObj.toString();

            String uploadedBy = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "unknown";

            if (title.isEmpty() || description.isEmpty()
                    || subject.equals("Select Subject") || unit.equals("Select Unit")) {
                Toast.makeText(this, "Please fill all fields and select a subject/unit", Toast.LENGTH_SHORT).show();
                return;
            }

            int charCount = description.length();
            String estimated = getEstimatedReadingTime(description);
            String type = "text"; // <-- mark this note as text

            Note note = new Note(
                    title,
                    subject,
                    unit,
                    uploadedBy,
                    System.currentTimeMillis(),
                    description,
                    charCount,
                    estimated,
                    purpose,
                    type   // <-- pass type
            );

            DatabaseReference notesRef = database.child("classes").child(classId).child("notes").push();
            notesRef.setValue(note).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(UploadTextNotesActivity.this, "✅Note uploaded successfully!", Toast.LENGTH_LONG).show();

                    // Notifications will be handled by a Firebase Cloud Function.
                    // No need to call sendNotification() from here.

                    noteTitleEditText.setText("");
                    noteDescriptionEditText.setText("");
                    notePurposeEditText.setText("");
                    charCountTextView.setText("0/2000");
                    readingTimeTextView.setText("Estimated Reading Time: 1 min");
                } else {
                    Toast.makeText(UploadTextNotesActivity.this, "❌Upload failed. Try again!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Delete Subject Button
        deleteSubjectBtn.setOnClickListener(v -> {
            if (!isAdmin) {
                Toast.makeText(this, "Only admin can delete subjects", Toast.LENGTH_SHORT).show();
                return;
            }
            int selectedPosition = subjectSpinner.getSelectedItemPosition();
            if (selectedPosition > 0) { // Position 0 is "Select Subject"
                String selectedSubject = subjectList.get(selectedPosition);
                new android.app.AlertDialog.Builder(this)
                        .setTitle("Delete Subject")
                        .setMessage("Are you sure you want to delete '" + selectedSubject + "'? This cannot be undone.")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            // Find the subject in Firebase and remove it
                            database.child("classes").child(classId).child("subjects")
                                    .orderByValue().equalTo(selectedSubject)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChildren()) {
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    snapshot.getRef().removeValue();
                                                    Toast.makeText(UploadTextNotesActivity.this, "Subject deleted successfully.", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Toast.makeText(UploadTextNotesActivity.this, "Failed to delete: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                Toast.makeText(this, "Please select a subject to delete.", Toast.LENGTH_SHORT).show();
            }
        });

        // Delete Unit Button
        deleteUnitBtn.setOnClickListener(v -> {
            if (!isAdmin) {
                Toast.makeText(this, "Only admin can delete units", Toast.LENGTH_SHORT).show();
                return;
            }
            int selectedPosition = unitSpinner.getSelectedItemPosition();
            if (selectedPosition > 0) { // Position 0 is "Select Unit"
                String selectedUnit = unitList.get(selectedPosition);
                new android.app.AlertDialog.Builder(this)
                        .setTitle("Delete Unit")
                        .setMessage("Are you sure you want to delete '" + selectedUnit + "'? This cannot be undone.")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            // Find the unit in Firebase and remove it
                            database.child("classes").child(classId).child("units")
                                    .orderByValue().equalTo(selectedUnit)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChildren()) {
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    snapshot.getRef().removeValue();
                                                    Toast.makeText(UploadTextNotesActivity.this, "Unit deleted successfully.", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Toast.makeText(UploadTextNotesActivity.this, "Failed to delete: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                Toast.makeText(this, "Please select a unit to delete.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getEstimatedReadingTime(String text) {
        int length = text.trim().length();
        if (length <= 500) return "1 min";
        else if (length <= 1000) return "2 mins";
        else return "3 mins";
    }

    private void checkAdminStatusAndSetupListeners() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserEmail = mAuth.getCurrentUser().getEmail();

        database.child("classes").child(classId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String adminEmail = snapshot.child("adminEmail").getValue(String.class);
                isAdmin = currentUserEmail != null && currentUserEmail.equals(adminEmail);

                if (isAdmin) {
                    addSubjectBtn.setVisibility(View.VISIBLE);
                    addUnitBtn.setVisibility(View.VISIBLE);
                    deleteSubjectBtn.setVisibility(View.VISIBLE);
                    deleteUnitBtn.setVisibility(View.VISIBLE);
                } else {
                    addSubjectBtn.setVisibility(View.GONE);
                    addUnitBtn.setVisibility(View.GONE);
                    deleteSubjectBtn.setVisibility(View.GONE);
                    deleteUnitBtn.setVisibility(View.GONE);
                }

                setupSubjectListener();
                setupUnitListener();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UploadTextNotesActivity.this, "Failed to load class data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("UploadTextNotes", "Failed to load class data.", error.toException());
                addSubjectBtn.setVisibility(View.GONE);
                addUnitBtn.setVisibility(View.GONE);
                deleteSubjectBtn.setVisibility(View.GONE);
                deleteUnitBtn.setVisibility(View.GONE);
            }
        });
    }

    private void setupSubjectListener() {
        DatabaseReference subjectsRef = database.child("classes").child(classId).child("subjects");
        subjectsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                subjectList.clear();
                subjectList.add("Select Subject");
                for (DataSnapshot subjectSnapshot : snapshot.getChildren()) {
                    String subjectName = subjectSnapshot.getValue(String.class);
                    if (subjectName != null) {
                        subjectList.add(subjectName);
                    }
                }
                subjectAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UploadTextNotesActivity.this, "Failed to load subjects: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("UploadTextNotes", "Failed to load subjects.", error.toException());
            }
        });
    }

    private void setupUnitListener() {
        DatabaseReference unitsRef = database.child("classes").child(classId).child("units");
        unitsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                unitList.clear();
                unitList.add("Select Unit");
                for (DataSnapshot unitSnapshot : snapshot.getChildren()) {
                    String unitName = unitSnapshot.getValue(String.class);
                    if (unitName != null) {
                        unitList.add(unitName);
                    }
                }
                unitAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UploadTextNotesActivity.this, "Failed to load units: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("UploadTextNotes", "Failed to load units.", error.toException());
            }
        });
    }
}
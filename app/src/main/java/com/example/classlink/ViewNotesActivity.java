/*

package com.example.classlink;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import android.util.Log;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ViewNotesActivity extends AppCompatActivity {

    private RecyclerView notesRecyclerView;
    private NotesAdapter notesAdapter;
    private Button textNoteBtn, imageNoteBtn;
    private Spinner subjectSpinner, unitSpinner;
    private ProgressBar progressBar;

    private DatabaseReference classRef;
    private String classId;

    // This list holds ALL notes fetched from Firebase.
    private List<ClassNote> allNotesList = new ArrayList<>();

    // Keep track of the currently selected filters
    private String selectedSubject = "All";
    private String selectedUnit = "All";
    private String selectedNoteType = "text"; // Default filter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_notes);

        // Get classId from intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("classId")) {
            classId = intent.getStringExtra("classId");
        } else {
            Toast.makeText(this, "Class ID not found. Cannot view notes.", Toast.LENGTH_LONG).show();
            finish(); // Close the activity if no class ID is provided
            return;
        }

        // Initialize views
        textNoteBtn = findViewById(R.id.textNoteBtn);
        imageNoteBtn = findViewById(R.id.imageNoteBtn);
        subjectSpinner = findViewById(R.id.subject_spinner);
        unitSpinner = findViewById(R.id.unit_spinner);
        notesRecyclerView = findViewById(R.id.notes_recycler_view);
        progressBar = findViewById(R.id.progress_bar);

        // Setup RecyclerView
        notesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notesAdapter = new NotesAdapter(new ArrayList<>());
        //notesAdapter = new NotesAdapter(this, new ArrayList<>());
        notesRecyclerView.setAdapter(notesAdapter);

        // Initialize Firebase references
        classRef = FirebaseDatabase
                .getInstance("https://claink-49383-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("classes").child(classId);

        // Set up listeners for the filter spinners
        setupSubjectSpinner();
        setupUnitSpinner();

        // Set up click listeners for the type buttons
        textNoteBtn.setOnClickListener(v -> {
            selectedNoteType = "text";
            filterNotes();
        });
        imageNoteBtn.setOnClickListener(v -> {
            selectedNoteType = "image";
            filterNotes();
        });

        // Fetch all notes from Firebase on app start
        fetchAllNotes();
    }

    private void setupSubjectSpinner() {
        classRef.child("subjects").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> subjectList = new ArrayList<>();
                subjectList.add("All"); // Option to view all subjects

                for (DataSnapshot subjectSnapshot : snapshot.getChildren()) {
                    String subjectName = subjectSnapshot.getValue(String.class);
                    if (subjectName != null) {
                        subjectList.add(subjectName);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(ViewNotesActivity.this, android.R.layout.simple_spinner_dropdown_item, subjectList);
                subjectSpinner.setAdapter(adapter);

                // Set up the listener after the adapter is set
                subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedSubject = parent.getItemAtPosition(position).toString();
                        filterNotes(); // Re-filter notes when a subject is selected
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ViewNotesActivity", "Failed to load subjects: " + error.getMessage());
            }
        });
    }

    private void setupUnitSpinner() {
        classRef.child("units").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> unitList = new ArrayList<>();
                unitList.add("All"); // Option to view all units

                for (DataSnapshot unitSnapshot : snapshot.getChildren()) {
                    String unitName = unitSnapshot.getValue(String.class);
                    if (unitName != null) {
                        unitList.add(unitName);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(ViewNotesActivity.this, android.R.layout.simple_spinner_dropdown_item, unitList);
                unitSpinner.setAdapter(adapter);

                // Set up the listener after the adapter is set
                unitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedUnit = parent.getItemAtPosition(position).toString();
                        filterNotes(); // Re-filter notes when a unit is selected
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ViewNotesActivity", "Failed to load units: " + error.getMessage());
            }
        });
    }

    private void fetchAllNotes() {
        progressBar.setVisibility(View.VISIBLE);

        // Listen for both text and image notes and combine them
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allNotesList.clear(); // Clear the list before adding new data
                for (DataSnapshot textSnapshot : dataSnapshot.child("notes").getChildren()) {
                    ClassNote note = textSnapshot.getValue(ClassNote.class);
                    if (note != null) {
                        allNotesList.add(note);
                    }
                }
                for (DataSnapshot imageSnapshot : dataSnapshot.child("ImageNotes").getChildren()) {
                    ClassNote note = imageSnapshot.getValue(ClassNote.class);
                    if (note != null) {
                        allNotesList.add(note);
                    }
                }

                progressBar.setVisibility(View.GONE);
                Log.d("FilterDebug", allNotesList.size() + " notes fetched from Firebase.");

                // Sort by timestamp descending
                Collections.sort(allNotesList, (o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));

                // Initially display notes based on default filters
                filterNotes();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ViewNotesActivity.this, "Failed to load notes.", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        };

        // We attach one listener to the parent "classes/classId" node
        classRef.addValueEventListener(listener);
    }

    private void filterNotes() {
        Log.d("FilterDebug", "Filtering notes...");
        Log.d("FilterDebug", "Selected Filters: Type=" + selectedNoteType + ", Subject=" + selectedSubject + ", Unit=" + selectedUnit);

        List<ClassNote> filteredList = new ArrayList<>();

        // Iterate through the full list and apply all filters
        for (ClassNote note : allNotesList) {
            boolean matchesSubject = selectedSubject.equals("All") || (note.getSubject() != null && note.getSubject().equalsIgnoreCase(selectedSubject));
            boolean matchesUnit = selectedUnit.equals("All") || (note.getUnit() != null && note.getUnit().equalsIgnoreCase(selectedUnit));

            // Check for null type and default to "text" if needed
            String noteType = (note.getType() != null) ? note.getType() : "text";
            boolean matchesType = selectedNoteType.equals("All") || (noteType.equalsIgnoreCase(selectedNoteType));

            Log.d("FilterDebug", "Checking note: Title='" + note.getTitle() + "', Subject='" + note.getSubject() + "', Unit='" + note.getUnit() + "', Type='" + note.getType() + "'");
            Log.d("FilterDebug", "Matches: Subject=" + matchesSubject + ", Unit=" + matchesUnit + ", Type=" + matchesType);


            if (matchesSubject && matchesUnit && matchesType) {
                filteredList.add(note);
                Log.d("FilterDebug", "Note added to filtered list: " + note.getTitle());
            }
        }

        // Update button colors to show which filter is active
        if (selectedNoteType.equals("text")) {
            textNoteBtn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue));
            imageNoteBtn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.greyText));
        } else if (selectedNoteType.equals("image")) {
            textNoteBtn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.greyText));
            imageNoteBtn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue));
        } else {
            // "All" state (initial state)
            textNoteBtn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.greyText));
            imageNoteBtn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.greyText));
        }

        notesAdapter.setNotes(filteredList);
    }
}*/


//working
package com.example.classlink;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// ADDED: Import for Lottie
import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ViewNotesActivity extends AppCompatActivity {

    private RecyclerView notesRecyclerView;
    private NotesAdapter notesAdapter;
    private Button textNoteBtn, imageNoteBtn;
    private Spinner subjectSpinner, unitSpinner;
    private ProgressBar progressBar;
    // ADDED: Lottie animation view variable
    private LottieAnimationView lottieNoData;

    private DatabaseReference classRef;
    private String classId;

    // This list holds ALL notes fetched from Firebase.
    private List<ClassNote> allNotesList = new ArrayList<>();

    // Keep track of the currently selected filters
    private String selectedSubject = "All";
    private String selectedUnit = "All";
    private String selectedNoteType = "text"; // Default filter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_notes);

        // Get classId from intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("classId")) {
            classId = intent.getStringExtra("classId");
        } else {
            Toast.makeText(this, "Class ID not found. Cannot view notes.", Toast.LENGTH_LONG).show();
            finish(); // Close the activity if no class ID is provided
            return;
        }

        // Initialize views
        textNoteBtn = findViewById(R.id.textNoteBtn);
        imageNoteBtn = findViewById(R.id.imageNoteBtn);
        subjectSpinner = findViewById(R.id.subject_spinner);
        unitSpinner = findViewById(R.id.unit_spinner);
        notesRecyclerView = findViewById(R.id.notes_recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        // ADDED: Initialize the Lottie view
        lottieNoData = findViewById(R.id.lottieNoData);

        // Setup RecyclerView
        notesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notesAdapter = new NotesAdapter(new ArrayList<>());
        notesRecyclerView.setAdapter(notesAdapter);

        // Initialize Firebase references
        classRef = FirebaseDatabase
                .getInstance("https://claink-49383-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("classes").child(classId);

        // Set up listeners for the filter spinners
        setupSubjectSpinner();
        setupUnitSpinner();

        // Set up click listeners for the type buttons
        textNoteBtn.setOnClickListener(v -> {
            selectedNoteType = "text";
            filterNotes();
        });
        imageNoteBtn.setOnClickListener(v -> {
            selectedNoteType = "image";
            filterNotes();
        });

        // Fetch all notes from Firebase on app start
        fetchAllNotes();
    }

    private void setupSubjectSpinner() {
        classRef.child("subjects").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> subjectList = new ArrayList<>();
                subjectList.add("All"); // Option to view all subjects

                for (DataSnapshot subjectSnapshot : snapshot.getChildren()) {
                    String subjectName = subjectSnapshot.getValue(String.class);
                    if (subjectName != null) {
                        subjectList.add(subjectName);
                    }
                }
                /*ArrayAdapter<String> adapter = new ArrayAdapter<>(ViewNotesActivity.this, android.R.layout.simple_spinner_dropdown_item, subjectList);
                subjectSpinner.setAdapter(adapter);*/
                BlackTextAdapter adapter = new BlackTextAdapter(ViewNotesActivity.this, android.R.layout.simple_spinner_item, subjectList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                // --- END OF CHANGES ---
                subjectSpinner.setAdapter(adapter);

                // Set up the listener after the adapter is set
                subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedSubject = parent.getItemAtPosition(position).toString();
                        filterNotes(); // Re-filter notes when a subject is selected
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ViewNotesActivity", "Failed to load subjects: " + error.getMessage());
            }
        });
    }

    private void setupUnitSpinner() {
        classRef.child("units").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> unitList = new ArrayList<>();
                unitList.add("All"); // Option to view all units

                for (DataSnapshot unitSnapshot : snapshot.getChildren()) {
                    String unitName = unitSnapshot.getValue(String.class);
                    if (unitName != null) {
                        unitList.add(unitName);
                    }
                }
                /*ArrayAdapter<String> adapter = new ArrayAdapter<>(ViewNotesActivity.this, android.R.layout.simple_spinner_dropdown_item, unitList);
                unitSpinner.setAdapter(adapter);*/
                BlackTextAdapter adapter = new BlackTextAdapter(ViewNotesActivity.this, android.R.layout.simple_spinner_item, unitList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                // --- END OF CHANGES ---
                unitSpinner.setAdapter(adapter);

                // Set up the listener after the adapter is set
                unitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedUnit = parent.getItemAtPosition(position).toString();
                        filterNotes(); // Re-filter notes when a unit is selected
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ViewNotesActivity", "Failed to load units: " + error.getMessage());
            }
        });
    }

    private void fetchAllNotes() {
        progressBar.setVisibility(View.VISIBLE);

        // Listen for both text and image notes and combine them
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allNotesList.clear(); // Clear the list before adding new data
                for (DataSnapshot textSnapshot : dataSnapshot.child("notes").getChildren()) {
                    ClassNote note = textSnapshot.getValue(ClassNote.class);
                    if (note != null) {
                        allNotesList.add(note);
                    }
                }
                for (DataSnapshot imageSnapshot : dataSnapshot.child("ImageNotes").getChildren()) {
                    ClassNote note = imageSnapshot.getValue(ClassNote.class);
                    if (note != null) {
                        allNotesList.add(note);
                    }
                }

                progressBar.setVisibility(View.GONE);
                Log.d("FilterDebug", allNotesList.size() + " notes fetched from Firebase.");

                // Sort by timestamp descending
                Collections.sort(allNotesList, (o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));

                // Initially display notes based on default filters
                filterNotes();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ViewNotesActivity.this, "Failed to load notes.", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        };

        // We attach one listener to the parent "classes/classId" node
        classRef.addValueEventListener(listener);
    }

    private void filterNotes() {
        Log.d("FilterDebug", "Filtering notes...");
        Log.d("FilterDebug", "Selected Filters: Type=" + selectedNoteType + ", Subject=" + selectedSubject + ", Unit=" + selectedUnit);

        List<ClassNote> filteredList = new ArrayList<>();

        // Iterate through the full list and apply all filters
        for (ClassNote note : allNotesList) {
            boolean matchesSubject = selectedSubject.equals("All") || (note.getSubject() != null && note.getSubject().equalsIgnoreCase(selectedSubject));
            boolean matchesUnit = selectedUnit.equals("All") || (note.getUnit() != null && note.getUnit().equalsIgnoreCase(selectedUnit));

            // Check for null type and default to "text" if needed
            String noteType = (note.getType() != null) ? note.getType() : "text";
            boolean matchesType = selectedNoteType.equals("All") || (noteType.equalsIgnoreCase(selectedNoteType));

            Log.d("FilterDebug", "Checking note: Title='" + note.getTitle() + "', Subject='" + note.getSubject() + "', Unit='" + note.getUnit() + "', Type='" + note.getType() + "'");
            Log.d("FilterDebug", "Matches: Subject=" + matchesSubject + ", Unit=" + matchesUnit + ", Type=" + matchesType);


            if (matchesSubject && matchesUnit && matchesType) {
                filteredList.add(note);
                Log.d("FilterDebug", "Note added to filtered list: " + note.getTitle());
            }
        }

        // Update button colors to show which filter is active
        if (selectedNoteType.equals("text")) {
            textNoteBtn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue));
            imageNoteBtn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.greyText));
        } else if (selectedNoteType.equals("image")) {
            textNoteBtn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.greyText));
            imageNoteBtn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue));
        } else {
            // "All" state (initial state)
            textNoteBtn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.greyText));
            imageNoteBtn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.greyText));
        }

        notesAdapter.setNotes(filteredList);

        // ADDED: Logic to show or hide the Lottie animation
        if (filteredList.isEmpty()) {
            lottieNoData.setVisibility(View.VISIBLE);
            notesRecyclerView.setVisibility(View.GONE);
        } else {
            lottieNoData.setVisibility(View.GONE);
            notesRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}

package com.example.classlink;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ExamActivity extends AppCompatActivity {

    private EditText examNameEditText;
    private Button pickDateBtn, saveExamBtn;
    private RecyclerView examRecyclerView;

    private DatabaseReference database;
    private FirebaseAuth mAuth;
    private String classId;
    private String selectedDate = "";

    private List<ExamModel> examList;
    private List<String> examKeys; // new list to store Firebase keys
    private ExamAdapter examAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase
                .getInstance("https://claink-49383-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference();

        classId = getIntent().getStringExtra("classId");

        examNameEditText = findViewById(R.id.editTextExamName);
        pickDateBtn = findViewById(R.id.btnPickDate);
        saveExamBtn = findViewById(R.id.btnSaveExam);
        examRecyclerView = findViewById(R.id.examRecyclerView);

        examList = new ArrayList<>();
        examKeys = new ArrayList<>(); // initialize keys list
        examAdapter = new ExamAdapter(examList, examKeys, classId, this);
        examRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        examRecyclerView.setAdapter(examAdapter);

        pickDateBtn.setOnClickListener(v -> openDatePicker());
        saveExamBtn.setOnClickListener(v -> saveExamToFirebase());

        loadExamsFromFirebase();
    }

    private void openDatePicker() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(
                ExamActivity.this,
                (view, y, m, d) -> {
                    selectedDate = y + "-" + (m + 1) + "-" + d;
                    pickDateBtn.setText(selectedDate);
                },
                year, month, day
        );
        dpd.show();
    }

    private void saveExamToFirebase() {
        String examName = examNameEditText.getText().toString().trim();

        if (examName.isEmpty() || selectedDate.isEmpty()) {
            Toast.makeText(this, "Enter exam name and date", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference examsRef = database.child("classes").child(classId).child("exams");
        String examId = examsRef.push().getKey();

        ExamModel exam = new ExamModel(examName, selectedDate);
        examsRef.child(examId).setValue(exam).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(ExamActivity.this, "Exam saved!", Toast.LENGTH_SHORT).show();
                examNameEditText.setText("");
                pickDateBtn.setText("Pick Date");
                selectedDate = "";
            } else {
                Toast.makeText(ExamActivity.this, "Failed to save exam", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadExamsFromFirebase() {
        DatabaseReference examsRef = database.child("classes").child(classId).child("exams");
        examsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                examList.clear();
                examKeys.clear(); // clear keys as well
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ExamModel exam = ds.getValue(ExamModel.class);
                    if (exam != null) {
                        examList.add(exam);
                        examKeys.add(ds.getKey()); // store Firebase key
                    }
                }

                // Sort exams by date (ascending)
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d");
                Collections.sort(examList, new Comparator<ExamModel>() {
                    @Override
                    public int compare(ExamModel e1, ExamModel e2) {
                        try {
                            Date date1 = sdf.parse(e1.getDate());
                            Date date2 = sdf.parse(e2.getDate());
                            return date1.compareTo(date2);
                        } catch (ParseException ex) {
                            ex.printStackTrace();
                            return 0;
                        }
                    }
                });

                examAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ExamActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}




/*working
package com.example.classlink;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ExamActivity extends AppCompatActivity {

    private EditText examNameEditText;
    private Button pickDateBtn, saveExamBtn;
    private RecyclerView examRecyclerView;

    private DatabaseReference database;
    private FirebaseAuth mAuth;
    private String classId;
    private String selectedDate = "";

    private List<ExamModel> examList;
    private ExamAdapter examAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase
                .getInstance("https://claink-49383-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference();

        classId = getIntent().getStringExtra("classId");

        examNameEditText = findViewById(R.id.editTextExamName);
        pickDateBtn = findViewById(R.id.btnPickDate);
        saveExamBtn = findViewById(R.id.btnSaveExam);
        examRecyclerView = findViewById(R.id.examRecyclerView);

        examList = new ArrayList<>();
        examAdapter = new ExamAdapter(examList);
        examRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        examRecyclerView.setAdapter(examAdapter);

        pickDateBtn.setOnClickListener(v -> openDatePicker());
        saveExamBtn.setOnClickListener(v -> saveExamToFirebase());

        loadExamsFromFirebase();
    }

    private void openDatePicker() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(
                ExamActivity.this,
                (view, y, m, d) -> {
                    selectedDate = y + "-" + (m + 1) + "-" + d;
                    pickDateBtn.setText(selectedDate);
                },
                year, month, day
        );
        dpd.show();
    }

    private void saveExamToFirebase() {
        String examName = examNameEditText.getText().toString().trim();

        if (examName.isEmpty() || selectedDate.isEmpty()) {
            Toast.makeText(this, "Enter exam name and date", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference examsRef = database.child("classes").child(classId).child("exams");
        String examId = examsRef.push().getKey();

        ExamModel exam = new ExamModel(examName, selectedDate);
        examsRef.child(examId).setValue(exam).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(ExamActivity.this, "Exam saved!", Toast.LENGTH_SHORT).show();
                examNameEditText.setText("");
                pickDateBtn.setText("Pick Date");
                selectedDate = "";
            } else {
                Toast.makeText(ExamActivity.this, "Failed to save exam", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadExamsFromFirebase() {
        DatabaseReference examsRef = database.child("classes").child(classId).child("exams");
        examsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                examList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ExamModel exam = ds.getValue(ExamModel.class);
                    if (exam != null) examList.add(exam);
                }

                // Sort exams by date (ascending)
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d");
                Collections.sort(examList, new Comparator<ExamModel>() {
                    @Override
                    public int compare(ExamModel e1, ExamModel e2) {
                        try {
                            Date date1 = sdf.parse(e1.getDate());
                            Date date2 = sdf.parse(e2.getDate());
                            return date1.compareTo(date2); // ascending order
                        } catch (ParseException ex) {
                            ex.printStackTrace();
                            return 0;
                        }
                    }
                });

                examAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ExamActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}*/

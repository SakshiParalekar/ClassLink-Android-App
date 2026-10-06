//working
package com.example.classlink;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class DashboardActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryClickListener {

    private TextView welcomeTextView;
    private TextView viewAllCategories;
    private EditText searchBar;
    private RecyclerView categoriesRecyclerView;
    private CategoryAdapter categoryAdapter;
    private List<Category> allCategories;
    private List<Category> currentDisplayedCategories;
    private ImageView profileIcon;

    private final int INITIAL_DISPLAY_COUNT = 4;
    private boolean isExpanded = false;

    private TextView tvReminder;

    private FirebaseAuth mAuth;
    private String classId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mAuth = FirebaseAuth.getInstance();

        welcomeTextView = findViewById(R.id.welcomeTextView);
        viewAllCategories = findViewById(R.id.viewAllCategories);
        searchBar = findViewById(R.id.searchBar);
        categoriesRecyclerView = findViewById(R.id.categoriesRecyclerView);
        profileIcon = findViewById(R.id.profileIcon);


        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("classId")) {
            classId = intent.getStringExtra("classId");
        } else {
            Toast.makeText(this, "Error: Class ID not found. Please rejoin the class.", Toast.LENGTH_LONG).show();
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userNameToDisplay = "User";

        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            if (userEmail != null && !userEmail.isEmpty()) {
                userNameToDisplay = userEmail.split("@")[0];
            }
            welcomeTextView.setText("Hi, " + userNameToDisplay + "!");
        } else {
            welcomeTextView.setText("Welcome!");
        }

        // --- Exam reminder check ---
        if (classId != null && !classId.isEmpty()) {
            checkExamReminders();
        }

        allCategories = new ArrayList<>();
        allCategories.add(new Category("Upload Notes", R.drawable.ic_upload_notes));
        allCategories.add(new Category("View Notes", R.drawable.ic_view_notes));
        allCategories.add(new Category("Exam Tracker", R.drawable.ic_exam_tracker));
        allCategories.add(new Category("Chat Room", R.drawable.ic_chat_room));
        allCategories.add(new Category("Study Boost", R.drawable.booster));

        categoriesRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        categoryAdapter = new CategoryAdapter(this, allCategories, this);
        categoriesRecyclerView.setAdapter(categoryAdapter);

        updateCategoryDisplay(false);

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                categoryAdapter.filter(s.toString());

                if (s.length() > 0) {
                    viewAllCategories.setVisibility(View.GONE);
                    setRecyclerViewHeightToWrapContent();
                } else {
                    viewAllCategories.setVisibility(View.VISIBLE);
                    updateCategoryDisplay(isExpanded);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        viewAllCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isExpanded = !isExpanded;
                updateCategoryDisplay(isExpanded);
            }
        });

        profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProfileMenu(v);
            }
        });
    }



    private void checkExamReminders() {
        if (classId == null || classId.isEmpty()) return;

        DatabaseReference examsRef = FirebaseDatabase.getInstance().getReference("exams").child(classId);
        examsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    tvReminder.setText(""); // no exams saved
                    return;
                }

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Calendar todayCal = Calendar.getInstance();
                String todayDate = sdf.format(todayCal.getTime());

                StringBuilder reminderText = new StringBuilder();
                boolean foundExam = false;

                for (DataSnapshot examSnap : snapshot.getChildren()) {
                    String examName = examSnap.child("examName").getValue(String.class);
                    String examDateStr = examSnap.child("examDate").getValue(String.class);

                    if (examDateStr != null) {
                        try {
                            Calendar examCal = Calendar.getInstance();
                            examCal.setTime(sdf.parse(examDateStr.trim()));

                            // Subtract 1 day from exam date
                            examCal.add(Calendar.DAY_OF_YEAR, -1);
                            String dayBeforeExam = sdf.format(examCal.getTime());

                            // If today is the day before exam
                            if (dayBeforeExam.equals(todayDate)) {
                                reminderText.append("⏳Reminder: ").append(examName).append(" is tomorrow!\n");
                                foundExam = true;
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                if (foundExam) {
                    reminderText.append("✨ Stay confident, you got this!");
                    tvReminder.setText(reminderText.toString());
                } else {
                    tvReminder.setText(""); // no exam for tomorrow
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                tvReminder.setText(""); // error case
            }
        });
    }




    private void showProfileMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenuInflater().inflate(R.menu.menu_main, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_logout) {
                    try {
                        if (mAuth.getCurrentUser() != null) {
                            mAuth.signOut();
                            Toast.makeText(DashboardActivity.this, "✅ Logged out successfully, Thank you for being a part of our community.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(DashboardActivity.this, "No user is currently logged in.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } catch (Exception e) {
                        Toast.makeText(DashboardActivity.this, "❌ Failed to log out: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    return true;
                }
                return false;
            }
        });
        popup.show();
    }

    private void updateCategoryDisplay(boolean expand) {
        if (expand) {
            currentDisplayedCategories = new ArrayList<>(allCategories);
            viewAllCategories.setText("View Less");
            setRecyclerViewHeightToWrapContent();
        } else {
            currentDisplayedCategories = new ArrayList<>();
            for (int i = 0; i < Math.min(INITIAL_DISPLAY_COUNT, allCategories.size()); i++) {
                currentDisplayedCategories.add(allCategories.get(i));
            }
            viewAllCategories.setText("View All");
            setRecyclerViewHeightFixed(250);
        }
        categoryAdapter.updateDisplayedCategories(currentDisplayedCategories);
    }

    private void setRecyclerViewHeightToWrapContent() {
        ViewGroup.LayoutParams params = categoriesRecyclerView.getLayoutParams();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        categoriesRecyclerView.setLayoutParams(params);
        categoriesRecyclerView.requestLayout();
    }

    private void setRecyclerViewHeightFixed(int dp) {
        ViewGroup.LayoutParams params = categoriesRecyclerView.getLayoutParams();
        params.height = (int) (dp * getResources().getDisplayMetrics().density);
        categoriesRecyclerView.setLayoutParams(params);
        categoriesRecyclerView.requestLayout();
    }

    @Override
    public void onCategoryClick(Category category) {
        Toast.makeText(this, category.getName() + " clicked!", Toast.LENGTH_SHORT).show();

        if (category.getName().equals("Upload Notes")) {
            if (classId != null && !classId.isEmpty()) {
                Intent intent = new Intent(DashboardActivity.this, UploadTextNotesActivity.class);
                intent.putExtra("classId", classId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Class ID not found. Cannot upload notes.", Toast.LENGTH_SHORT).show();
            }
        } else if (category.getName().equals("View Notes")) {
            if (classId != null && !classId.isEmpty()) {
                Intent intent = new Intent(DashboardActivity.this, ViewNotesActivity.class);
                intent.putExtra("classId", classId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Class ID not found. Cannot view notes.", Toast.LENGTH_SHORT).show();
            }
        } else if (category.getName().equals("Chat Room")) {
            if (classId != null && !classId.isEmpty()) {
                Intent intent = new Intent(DashboardActivity.this, ChatroomActivity.class);
                intent.putExtra("classId", classId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Class ID not found. Cannot view notes.", Toast.LENGTH_SHORT).show();
            }
        } else if (category.getName().equals("Exam Tracker")) {
            if (classId != null && !classId.isEmpty()) {
                Intent intent = new Intent(DashboardActivity.this, ExamActivity.class);
                intent.putExtra("classId", classId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Class ID not found. Cannot open exam tracker.", Toast.LENGTH_SHORT).show();
            }
        } else if (category.getName().equals("Study Boost")) {
            if (classId != null && !classId.isEmpty()) {
                Intent intent = new Intent(DashboardActivity.this, MotivationActivity.class);
                intent.putExtra("classId", classId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Class ID not found. Cannot open exam tracker.", Toast.LENGTH_SHORT).show();
            }
        }

    }
}



/*

//with logout, chatroom done
package com.example.classlink;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;


public class DashboardActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryClickListener {

    private TextView welcomeTextView;
    private TextView viewAllCategories;
    private EditText searchBar;
    private RecyclerView categoriesRecyclerView;
    private CategoryAdapter categoryAdapter;
    private List<Category> allCategories;
    private List<Category> currentDisplayedCategories;
    private ImageView profileIcon;

    private final int INITIAL_DISPLAY_COUNT = 4;
    private boolean isExpanded = false;

    private FirebaseAuth mAuth;
    private String classId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mAuth = FirebaseAuth.getInstance();

        welcomeTextView = findViewById(R.id.welcomeTextView);
        viewAllCategories = findViewById(R.id.viewAllCategories);
        searchBar = findViewById(R.id.searchBar);
        categoriesRecyclerView = findViewById(R.id.categoriesRecyclerView);
        profileIcon = findViewById(R.id.profileIcon);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("classId")) {
            classId = intent.getStringExtra("classId");
        } else {
            Toast.makeText(this, "Error: Class ID not found. Please rejoin the class.", Toast.LENGTH_LONG).show();
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userNameToDisplay = "User";

        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            if (userEmail != null && !userEmail.isEmpty()) {
                userNameToDisplay = userEmail.split("@")[0];
            }
            welcomeTextView.setText("Hi, " + userNameToDisplay + "!");
        } else {
            welcomeTextView.setText("Welcome!");
        }

        allCategories = new ArrayList<>();
        allCategories.add(new Category("Upload Notes", R.drawable.ic_upload_notes));
        allCategories.add(new Category("View Notes", R.drawable.ic_view_notes));
        allCategories.add(new Category("Saved Notes", R.drawable.ic_saved_notes));
        allCategories.add(new Category("Chat Room", R.drawable.ic_chat_room));

        categoriesRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        categoryAdapter = new CategoryAdapter(this, allCategories, this);
        categoriesRecyclerView.setAdapter(categoryAdapter);

        updateCategoryDisplay(false);

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                categoryAdapter.filter(s.toString());

                if (s.length() > 0) {
                    viewAllCategories.setVisibility(View.GONE);
                    setRecyclerViewHeightToWrapContent();
                } else {
                    viewAllCategories.setVisibility(View.VISIBLE);
                    updateCategoryDisplay(isExpanded);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        viewAllCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isExpanded = !isExpanded;
                updateCategoryDisplay(isExpanded);
            }
        });

        profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProfileMenu(v);
            }
        });
    }

    private void showProfileMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenuInflater().inflate(R.menu.menu_main, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_logout) {
                    try {
                        if (mAuth.getCurrentUser() != null) {
                            mAuth.signOut();
                            Toast.makeText(DashboardActivity.this, "✅ Logged out successfully, Thank you for being a part of our community.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(DashboardActivity.this, "No user is currently logged in.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } catch (Exception e) {
                        Toast.makeText(DashboardActivity.this, "❌ Failed to log out: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    return true;
                }
                return false;
            }
        });
        popup.show();
    }

    private void updateCategoryDisplay(boolean expand) {
        if (expand) {
            currentDisplayedCategories = new ArrayList<>(allCategories);
            viewAllCategories.setText("View Less");
            setRecyclerViewHeightToWrapContent();
        } else {
            currentDisplayedCategories = new ArrayList<>();
            for (int i = 0; i < Math.min(INITIAL_DISPLAY_COUNT, allCategories.size()); i++) {
                currentDisplayedCategories.add(allCategories.get(i));
            }
            viewAllCategories.setText("View All");
            setRecyclerViewHeightFixed(250);
        }
        categoryAdapter.updateDisplayedCategories(currentDisplayedCategories);
    }

    private void setRecyclerViewHeightToWrapContent() {
        ViewGroup.LayoutParams params = categoriesRecyclerView.getLayoutParams();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        categoriesRecyclerView.setLayoutParams(params);
        categoriesRecyclerView.requestLayout();
    }

    private void setRecyclerViewHeightFixed(int dp) {
        ViewGroup.LayoutParams params = categoriesRecyclerView.getLayoutParams();
        params.height = (int) (dp * getResources().getDisplayMetrics().density);
        categoriesRecyclerView.setLayoutParams(params);
        categoriesRecyclerView.requestLayout();
    }

    @Override
    public void onCategoryClick(Category category) {
        Toast.makeText(this, category.getName() + " clicked!", Toast.LENGTH_SHORT).show();

        if (category.getName().equals("Upload Notes")) {
            if (classId != null && !classId.isEmpty()) {
                Intent intent = new Intent(DashboardActivity.this, UploadTextNotesActivity.class);
                intent.putExtra("classId", classId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Class ID not found. Cannot upload notes.", Toast.LENGTH_SHORT).show();
            }
        } else if (category.getName().equals("View Notes")) {
            if (classId != null && !classId.isEmpty()) {
                Intent intent = new Intent(DashboardActivity.this, ViewNotesActivity.class);
                intent.putExtra("classId", classId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Class ID not found. Cannot view notes.", Toast.LENGTH_SHORT).show();
            }
        } else if (category.getName().equals("Chat Room")) {
            if (classId != null && !classId.isEmpty()) {
                Intent intent = new Intent(DashboardActivity.this, ChatroomActivity.class);
                intent.putExtra("classId", classId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Class ID not found. Cannot view notes.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

*/
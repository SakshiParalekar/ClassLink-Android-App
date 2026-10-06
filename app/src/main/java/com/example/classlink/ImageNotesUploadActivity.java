package com.example.classlink;




import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import android.view.View;




import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;




import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;




import org.json.JSONException;
import org.json.JSONObject;




import java.util.*;




public class ImageNotesUploadActivity extends AppCompatActivity {




    private Spinner subjectSpinner, unitSpinner;
    private ImageView addSubjectBtn, addUnitBtn;
    private ImageView deleteSubjectBtn, deleteUnitBtn; // Added delete buttons
    private EditText imageNoteTitleEditText;
    private Button browseFileButton, uploadImageNoteBtn;
    private ImageView previewImage;

    private Uri selectedFileUri;
    private List<String> subjectList = new ArrayList<>();
    private ArrayAdapter<String> subjectAdapter;
    private List<String> unitList = new ArrayList<>();
    private ArrayAdapter<String> unitAdapter;

    private DatabaseReference dbRef;
    private StorageReference storageRef;
    private FirebaseAuth mAuth;
    private String classId;
    private boolean isAdmin = false;
    private boolean isUploading = false;




    private RequestQueue requestQueue;

    private final ActivityResultLauncher<Intent> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedFileUri = result.getData().getData();
                    String mimeType = getContentResolver().getType(selectedFileUri);
                    if (mimeType != null && mimeType.startsWith("image")) {
                        previewImage.setImageURI(selectedFileUri);
                    } else {
                        previewImage.setImageResource(R.drawable.ic_pdf_placeholder); // PDF icon
                    }
                    previewImage.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
                }
            });

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_notes_upload);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase
                .getInstance("https://claink-49383-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference();
        storageRef = FirebaseStorage.getInstance().getReference();
        requestQueue = Volley.newRequestQueue(this);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("classId")) {
            classId = intent.getStringExtra("classId");
        } else {
            Toast.makeText(this, "Class ID not found. Cannot upload image notes.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        imageNoteTitleEditText = findViewById(R.id.imageNoteTitleEditText);
        browseFileButton = findViewById(R.id.browseFileButton);
        uploadImageNoteBtn = findViewById(R.id.uploadImageNoteBtn);
        previewImage = findViewById(R.id.previewImage);
        subjectSpinner = findViewById(R.id.subjectSpinner);
        addSubjectBtn = findViewById(R.id.addSubjectBtn);
        unitSpinner = findViewById(R.id.unitSpinner);
        addUnitBtn = findViewById(R.id.addUnitBtn);
        deleteSubjectBtn = findViewById(R.id.deleteSubjectBtn);
        deleteUnitBtn = findViewById(R.id.deleteUnitBtn);

        subjectList = new ArrayList<>();
        subjectAdapter = new BlackTextAdapter(this, android.R.layout.simple_spinner_item, subjectList);
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjectSpinner.setAdapter(subjectAdapter);

        unitList = new ArrayList<>();
        unitAdapter = new BlackTextAdapter(this, android.R.layout.simple_spinner_item, unitList);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitSpinner.setAdapter(unitAdapter);



        checkAdminStatusAndSetupListeners();

        browseFileButton.setOnClickListener(v -> {
            Intent filePickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
            filePickerIntent.setType("*/*");
            String[] mimeTypes = {"image/*", "application/pdf"};
            filePickerIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            filePickerIntent.addCategory(Intent.CATEGORY_OPENABLE);
            filePickerLauncher.launch(filePickerIntent);
        });

        addSubjectBtn.setOnClickListener(v -> showAddSubjectDialog());
        addUnitBtn.setOnClickListener(v -> showAddUnitDialog());
        uploadImageNoteBtn.setOnClickListener(v -> uploadImageNote());

        // Add delete button listeners
        deleteSubjectBtn.setOnClickListener(v -> deleteSubject());
        deleteUnitBtn.setOnClickListener(v -> deleteUnit());
    }
    private void deleteSubject() {
        if (!isAdmin) {
            Toast.makeText(this, "Only admin can delete subjects", Toast.LENGTH_SHORT).show();
            return;
        }
        int selectedPosition = subjectSpinner.getSelectedItemPosition();
        if (selectedPosition > 0) { // Position 0 is "Select Subject"
            String selectedSubject = subjectList.get(selectedPosition);
            new AlertDialog.Builder(this)
                    .setTitle("Delete Subject")
                    .setMessage("Are you sure you want to delete '" + selectedSubject + "'? This cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        // Find the subject in Firebase and remove it
                        dbRef.child("classes").child(classId).child("subjects")
                                .orderByValue().equalTo(selectedSubject)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChildren()) {
                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                snapshot.getRef().removeValue();
                                                Toast.makeText(ImageNotesUploadActivity.this, "Subject deleted successfully.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Toast.makeText(ImageNotesUploadActivity.this, "Failed to delete: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            Toast.makeText(this, "Please select a subject to delete.", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteUnit() {
        if (!isAdmin) {
            Toast.makeText(this, "Only admin can delete units", Toast.LENGTH_SHORT).show();
            return;
        }
        int selectedPosition = unitSpinner.getSelectedItemPosition();
        if (selectedPosition > 0) { // Position 0 is "Select Unit"
            String selectedUnit = unitList.get(selectedPosition);
            new AlertDialog.Builder(this)
                    .setTitle("Delete Unit")
                    .setMessage("Are you sure you want to delete '" + selectedUnit + "'? This cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        // Find the unit in Firebase and remove it
                        dbRef.child("classes").child(classId).child("units")
                                .orderByValue().equalTo(selectedUnit)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChildren()) {
                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                snapshot.getRef().removeValue();
                                                Toast.makeText(ImageNotesUploadActivity.this, "Unit deleted successfully.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Toast.makeText(ImageNotesUploadActivity.this, "Failed to delete: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            Toast.makeText(this, "Please select a unit to delete.", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkAdminStatusAndSetupListeners() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserEmail = currentUser.getEmail();

        dbRef.child("classes").child(classId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String adminEmail = snapshot.child("adminEmail").getValue(String.class);
                isAdmin = currentUserEmail != null && currentUserEmail.equals(adminEmail);
                addSubjectBtn.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
                addUnitBtn.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
                deleteSubjectBtn.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
                deleteUnitBtn.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
                setupSubjectListener();
                setupUnitListener();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ImageNotesUploadActivity.this, "Failed to load class data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                addSubjectBtn.setVisibility(View.GONE);
                addUnitBtn.setVisibility(View.GONE);
                deleteSubjectBtn.setVisibility(View.GONE);
                deleteUnitBtn.setVisibility(View.GONE);
            }
        });
    }

    private void setupSubjectListener() {
        DatabaseReference subjectsRef = dbRef.child("classes").child(classId).child("subjects");
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
                Toast.makeText(ImageNotesUploadActivity.this, "Failed to load subjects", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupUnitListener() {
        DatabaseReference unitsRef = dbRef.child("classes").child(classId).child("units");
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
                Toast.makeText(ImageNotesUploadActivity.this, "Failed to load units", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddSubjectDialog() {
        final EditText input = new EditText(this);
        input.setHint("Enter new subject");

        new AlertDialog.Builder(this)
                .setTitle("Add Subject")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String newSubject = input.getText().toString().trim();
                    if (!newSubject.isEmpty()) {
                        dbRef.child("classes").child(classId).child("subjects").push().setValue(newSubject);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddUnitDialog() {
        final EditText input = new EditText(this);
        input.setHint("Enter new unit");

        new AlertDialog.Builder(this)
                .setTitle("Add Unit")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String newUnit = input.getText().toString().trim();
                    if (!newUnit.isEmpty()) {
                        dbRef.child("classes").child(classId).child("units").push().setValue(newUnit);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void uploadImageNote() {
        if (isUploading) {
            Toast.makeText(this, "Upload already in progress...", Toast.LENGTH_SHORT).show();
            return;
        }
        isUploading = true;
        uploadImageNoteBtn.setEnabled(false);

        String title = imageNoteTitleEditText.getText().toString().trim();
        String subject = subjectSpinner.getSelectedItem() != null ? subjectSpinner.getSelectedItem().toString() : "Select Subject";
        String unit = unitSpinner.getSelectedItem() != null ? unitSpinner.getSelectedItem().toString() : "Select Unit";

        if (title.isEmpty() || subject.equals("Select Subject") || unit.equals("Select Unit")) {
            Toast.makeText(this, "Please fill all fields and select a subject/unit", Toast.LENGTH_SHORT).show();
            resetUploadState();
            return;
        }

        if (selectedFileUri == null) {
            Toast.makeText(this, "Select a file to upload", Toast.LENGTH_SHORT).show();
            resetUploadState();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show();
            resetUploadState();
            return;
        }

        String uid = user.getUid();
        String email = user.getEmail();
        String noteId = dbRef.child("classes").child(classId).child("ImageNotes").push().getKey();
        if (noteId == null) {
            Toast.makeText(this, "Failed to generate note ID", Toast.LENGTH_SHORT).show();
            resetUploadState();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        final String UPLOAD_PRESET = "unsigned_classlink";
        final String uploadUrl = "https://api.cloudinary.com/v1_1/dbkorq12k/auto/upload";

        byte[] fileData = FileUtils.getFileDataFromUri(this, selectedFileUri);
        if (fileData.length == 0) {
            progressDialog.dismiss();
            resetUploadState();
            Toast.makeText(this, "Failed to read file.", Toast.LENGTH_SHORT).show();
            return;
        }

        String mime = getContentResolver().getType(selectedFileUri);
        if (mime == null) mime = "application/octet-stream";

        String fileName = System.currentTimeMillis() + "_" + title.replaceAll("\\s+", "_");

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(
                Request.Method.POST, uploadUrl,
                response -> {
                    progressDialog.dismiss();
                    resetUploadState();
                    try {
                        String res = new String(response.data);
                        JSONObject json = new JSONObject(res);
                        String cloudUrl = json.optString("secure_url", "");
                        if (cloudUrl.isEmpty()) {
                            Toast.makeText(ImageNotesUploadActivity.this, "Upload succeeded but no URL returned", Toast.LENGTH_LONG).show();
                            return;
                        }

                        NoteImageModel note = new NoteImageModel(title, subject, unit, cloudUrl, uid, email, "image", ServerValue.TIMESTAMP);
                        dbRef.child("classes").child(classId).child("ImageNotes").child(noteId)
                                .setValue(note)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(ImageNotesUploadActivity.this, "✅Note uploaded Successfully!", Toast.LENGTH_SHORT).show();
                                    imageNoteTitleEditText.setText("");
                                    previewImage.setVisibility(View.GONE);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(ImageNotesUploadActivity.this, "❌Failed to save note in DB.", Toast.LENGTH_SHORT).show();
                                });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(ImageNotesUploadActivity.this, "Response parse error", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    resetUploadState();
                    Toast.makeText(ImageNotesUploadActivity.this, "Upload failed: " + (error.getMessage() == null ? "Network error" : error.getMessage()), Toast.LENGTH_LONG).show();
                }
        );

        multipartRequest.setTag("UPLOAD_NOTE");
        requestQueue.cancelAll("UPLOAD_NOTE");
        Map<String, String> params = new HashMap<>();
        params.put("upload_preset", UPLOAD_PRESET);
        multipartRequest.setParams(params);

        Map<String, VolleyMultipartRequest.DataPart> data = new HashMap<>();
        data.put("file", new VolleyMultipartRequest.DataPart(fileName, fileData, mime));
        multipartRequest.setByteData(data);

        requestQueue.add(multipartRequest);
    }

    private void resetUploadState() {
        isUploading = false;
        uploadImageNoteBtn.setEnabled(true);
    }

    public static class NoteImageModel {
        public String title;
        public String subject;
        public String unit;
        public String downloadUrl;
        public String userId;
        public String uploadedBy;
        public String type;
        public Object timestamp;

        public NoteImageModel() {
        }

        public NoteImageModel(String title, String subject, String unit, String downloadUrl, String userId, String uploadedBy, String type, Object timestamp) {
            this.title = title;
            this.subject = subject;
            this.unit = unit;
            this.downloadUrl = downloadUrl;
            this.userId = userId;
            this.uploadedBy = uploadedBy;
            this.type = type;
            this.timestamp = timestamp;
        }
    }
}


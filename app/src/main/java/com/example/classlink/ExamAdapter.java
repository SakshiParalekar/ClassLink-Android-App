//delete button
package com.example.classlink;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class ExamAdapter extends RecyclerView.Adapter<ExamAdapter.ExamViewHolder> {

    private List<ExamModel> examList;
    private List<String> examKeys; // keys for deletion
    private String classId;
    private Context context;

    public ExamAdapter(List<ExamModel> examList, List<String> examKeys, String classId, Context context) {
        this.examList = examList;
        this.examKeys = examKeys;
        this.classId = classId;
        this.context = context;
    }

    @NonNull
    @Override
    public ExamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exam, parent, false);
        return new ExamViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ExamViewHolder holder, int position) {
        ExamModel exam = examList.get(position);
        holder.examName.setText(exam.getExamName());
        holder.examDate.setText("\uD83D\uDDD3\uFE0F " + exam.getExamDate());

        // Delete button click listener
        holder.btnDeleteExam.setOnClickListener(v -> {
            if (position < examKeys.size()) {
                String examKey = examKeys.get(position);
                DatabaseReference examsRef = FirebaseDatabase.getInstance(
                                "https://claink-49383-default-rtdb.asia-southeast1.firebasedatabase.app")
                        .getReference()
                        .child("classes")
                        .child(classId)
                        .child("exams");

                examsRef.child(examKey).removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Exam deleted!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Failed to delete exam", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return examList.size();
    }

    public static class ExamViewHolder extends RecyclerView.ViewHolder {
        TextView examName, examDate;
        Button btnDeleteExam;

        public ExamViewHolder(@NonNull View itemView) {
            super(itemView);
            examName = itemView.findViewById(R.id.textExamName);
            examDate = itemView.findViewById(R.id.textExamDate);
            btnDeleteExam = itemView.findViewById(R.id.btnDeleteExam); // the new button
        }
    }
}



/*
package com.example.classlink;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ExamAdapter extends RecyclerView.Adapter<ExamAdapter.ExamViewHolder> {

    private List<ExamModel> examList;

    public ExamAdapter(List<ExamModel> examList) {
        this.examList = examList;
    }

    @NonNull
    @Override
    public ExamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exam, parent, false);
        return new ExamViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ExamViewHolder holder, int position) {
        ExamModel exam = examList.get(position);
        holder.examName.setText(exam.getExamName());
        holder.examDate.setText("\uD83D\uDDD3\uFE0F " + exam.getExamDate());
    }

    @Override
    public int getItemCount() {
        return examList.size();
    }

    public static class ExamViewHolder extends RecyclerView.ViewHolder {
        TextView examName, examDate;
        public ExamViewHolder(@NonNull View itemView) {
            super(itemView);
            examName = itemView.findViewById(R.id.textExamName);
            examDate = itemView.findViewById(R.id.textExamDate);
        }
    }
}
*/

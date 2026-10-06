package com.example.classlink;

public class ExamModel {
    private String examName;
    private String examDate;

    public ExamModel() {}

    public ExamModel(String examName, String examDate) {
        this.examName = examName;
        this.examDate = examDate;
    }

    public String getExamName() { return examName; }
    public String getExamDate() { return examDate; }


    public String getDate() {
        return examDate;
    }
}

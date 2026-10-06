
package com.example.classlink;
public class NoteModel {
    public String type; // "text" or "image"
    public String title;
    public String subject;
    public String unit;
    public String description;
    public String purpose;
    public String estimatedReadTime;
    public int charCount;
    public String downloadUrl;
    public String uploadedBy;
    public String userId;
    public String adminEmail;
    public String classCode;
    public long timestamp;

    public NoteModel() {} // Firebase requires empty constructor
}

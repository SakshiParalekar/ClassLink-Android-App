
package com.example.classlink;

public class NoteTextModel {
    private String noteId, title, description, purpose, subject, userId;

    public NoteTextModel() {
    }

    public NoteTextModel(String noteId, String title, String description, String purpose, String subject, String userId) {
        this.noteId = noteId;
        this.title = title;
        this.description = description;
        this.purpose = purpose;
        this.subject = subject;
        this.userId = userId;
    }

    // Getters (and setters if needed)
    public String getNoteId() { return noteId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getPurpose() { return purpose; }
    public String getSubject() { return subject; }
    public String getUserId() { return userId; }
}



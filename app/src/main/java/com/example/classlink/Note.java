/*
package com.example.classlink;


public class Note {
    private String subject;
    private String unit;
    private String date;
    private String content;

    public Note() { }

    public Note(String subject, String unit, String date, String content) {
        this.subject = subject;
        this.unit = unit;
        this.date = date;
        this.content = content;
    }

    public String getSubject() { return subject; }
    public String getUnit() { return unit; }
    public String getDate() { return date; }
    public String getContent() { return content; }
}

*/


/*package com.example.classlink;

public class Note {
    public String noteId, title, description, purpose, uploadedBy, estimatedTime;
    public int charCount;
    public long timestamp;

    public Note(String title, String subject, String unit, String uploadedBy, long l, String cloudUrl) {} // Needed for Firebase

    public Note(String noteId, String title, String description, String purpose,
                String uploadedBy, int charCount, String estimatedTime, long timestamp) {
        this.noteId = noteId;
        this.title = title;
        this.description = description;
        this.purpose = purpose;
        this.uploadedBy = uploadedBy;
        this.charCount = charCount;
        this.estimatedTime = estimatedTime;
        this.timestamp = timestamp;
    }
}*/


package com.example.classlink;

public class Note {
    public String title;
    public String subject;
    public String unit;
    public String uploadedBy;
    public long timestamp;
    public String description;

    // Extra fields
    public int charCount;
    public String estimatedReadTime;
    public String purpose;
    public String type;   // <-- NEW FIELD

    // Empty constructor (needed by Firebase)
    public Note(String title, String subject, String unit, String uploadedBy, long l, String description, String estimated, String purpose, String type) {}

    public Note(String title,
                String subject,
                String unit,
                String uploadedBy,
                long timestamp,
                String description,
                int charCount,
                String estimatedReadTime,
                String purpose,
                String type) {          // <-- include in constructor
        this.title = title;
        this.subject = subject;
        this.unit = unit;
        this.uploadedBy = uploadedBy;
        this.timestamp = timestamp;
        this.description = description;
        this.charCount = charCount;
        this.estimatedReadTime = estimatedReadTime;
        this.purpose = purpose;
        this.type = type;
    }
}

package com.educore.academic.model;

import com.educore.user.util.Validator;

public final class Notification {
    private final String id;
    private final String studentId;
    private final String gradeId;
    private final String message;
    private final long createdAt;

    public Notification(String id, String studentId,
                        String gradeId, String message, long createdAt) {
        Validator.requireNonBlank(id, "Id");
        Validator.requireNonBlank(studentId, "StudentId");
        Validator.requireNonBlank(gradeId, "GradeId");
        Validator.requireNonBlank(message, "Message");

        if (createdAt <= 0) {
            throw new IllegalArgumentException("Invalid creation time");
        }

        this.id = id;
        this.studentId = studentId;
        this.gradeId = gradeId;
        this.message = message;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getGradeId() {
        return gradeId;
    }

    public String getMessage() {
        return message;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}

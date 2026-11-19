package com.educore.academic.model;

import com.educore.user.util.Validator;

public class Grade {
    private final String id;
    private final String studentId;
    private final String teacherId;
    private final String subject;
    private final GradeType type;
    private final int score;

    public Grade(String id, String studentId, String teacherId, String subject, GradeType type, int score) {
        Validator.requireNonBlank(id, "Id");
        Validator.requireNonBlank(studentId, "StudentId");
        Validator.requireNonBlank(teacherId, "TeacherId");
        Validator.requireNonBlank(subject, "Subject");
        if(type == null){
            throw new IllegalArgumentException("Grade type cannot be null.");
        }
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("Score is invalid");
        }
        this.id = id;
        this.studentId = studentId;
        this.teacherId = teacherId;
        this.subject = subject;
        this.type = type;
        this.score = score;
    }

    public String getId() {
        return id;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public String getSubject() {
        return subject;
    }

    public GradeType getType() {
        return type;
    }

    public int getScore() {
        return score;
    }
}

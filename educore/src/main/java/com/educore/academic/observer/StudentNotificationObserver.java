package com.educore.academic.observer;

import com.educore.academic.model.Grade;
import com.educore.academic.model.Notification;
import com.educore.academic.repository.NotificationRepository;
import com.educore.user.util.Validator;
import java.util.UUID;

public class StudentNotificationObserver implements GradeObserver {
    private final String studentId;
    private final NotificationRepository notificationRepository;

    public StudentNotificationObserver(String studentId,
                                       NotificationRepository notificationRepository) {
        Validator.requireNonBlank(studentId, "StudentId");

        if (notificationRepository == null) {
            throw new IllegalArgumentException("Notification repository cannot be null");
        }

        this.studentId = studentId;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void onGradeAdded(Grade grade) {
        if (!studentId.equals(grade.getStudentId())) {
            return;
        }

        String message = "New grade in " + grade.getSubject()
                + " (" + grade.getType() + "): " + grade.getScore();

        Notification notification = new Notification(
                UUID.randomUUID().toString(),
                studentId,
                grade.getId(),
                message,
                System.currentTimeMillis()
        );

        notificationRepository.save(notification);
    }
}

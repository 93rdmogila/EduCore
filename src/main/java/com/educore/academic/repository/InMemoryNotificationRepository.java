package com.educore.academic.repository;

import com.educore.academic.model.Notification;
import java.util.ArrayList;
import java.util.List;

public class InMemoryNotificationRepository implements NotificationRepository {
    private final List<Notification> notifications = new ArrayList<>();

    @Override
    public void save(Notification notification) {
        if (notification == null) {
            throw new IllegalArgumentException("Notification cannot be null");
        }

        for (Notification existing : notifications) {
            if (existing.getId().equals(notification.getId())) {
                throw new IllegalArgumentException("Notification ID already exists");
            }
        }

        notifications.add(notification);
    }

    @Override
    public List<Notification> findByStudentId(String studentId) {
        List<Notification> result = new ArrayList<>();

        for (Notification notification : notifications) {
            if (notification.getStudentId().equals(studentId)) {
                result.add(notification);
            }
        }

        return result;
    }
}

package com.educore.academic.repository;

import com.educore.academic.model.Notification;
import java.util.List;

public interface NotificationRepository {
    void save(Notification notification);

    List<Notification> findByStudentId(String studentId);
}

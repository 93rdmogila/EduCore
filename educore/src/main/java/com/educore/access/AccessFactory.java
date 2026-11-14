package com.educore.access;

import com.educore.user.auth.Session;
import com.educore.user.model.UserRole;

public class AccessFactory {
    public Access createFor(Session session) {
        if (session == null || !session.isActive()) {
            throw new SecurityException("Active session required.");
        }

        Access basicAccess = new BasicAccess();

        return switch (session.getRole()) {
            case STUDENT -> new StudentAccessDecorator(basicAccess);
            case TEACHER -> new TeacherAccessDecorator(basicAccess);
            case ADMINISTRATOR -> new AdministratorAccessDecorator(basicAccess);
        };
    }
}

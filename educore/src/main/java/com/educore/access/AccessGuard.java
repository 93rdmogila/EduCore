package com.educore.access;

import com.educore.user.model.UserRole;
import com.educore.user.auth.Session;
import com.educore.user.util.Validator;

public class AccessGuard {
    private final AccessFactory accessFactory;

    public AccessGuard() {
        this(new AccessFactory());
    }

    public AccessGuard(AccessFactory accessFactory) {
        if (accessFactory == null) {
            throw new IllegalArgumentException("Access factory can not be null.");
        }

        this.accessFactory = accessFactory;
    }

    public void requirePermission(Session session, Permission permission) {
        if (permission == null) {
            throw new IllegalArgumentException("Permission can not be null.");
        }

        Access access = accessFactory.createFor(session);

        if (!access.hasPermission(permission)) {
            throw new SecurityException("Access denied " + permission);
        }
    }

    public void requireGradeAccess(Session session, String studentId) {
        Access access = accessFactory.createFor(session);

        Validator.requireNonBlank(studentId, "Student id");

        Permission requirePermission = session.getRole() == UserRole.STUDENT
                ? Permission.VIEW_OWN_GRADES
                : Permission.VIEW_ANY_GRADES;

        if (!access.hasPermission(requirePermission)) {
            throw new SecurityException("Access Denied.");
        }

        if (session.getRole() == UserRole.STUDENT
        && !session.getUserId().equals(studentId)) {
            throw new SecurityException("Students can view only their own grades.");
        }
     }
}

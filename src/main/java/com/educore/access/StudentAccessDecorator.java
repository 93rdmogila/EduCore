package com.educore.access;

public class StudentAccessDecorator extends AccessDecorator{
    public StudentAccessDecorator(Access wrappedAccess) {
        super(wrappedAccess);
    }

    @Override
    public boolean hasPermission(Permission permission) {
        return permission == Permission.VIEW_OWN_GRADES
                || permission == Permission.VIEW_NOTIFICATIONS
                || super.hasPermission(permission);
    }
}

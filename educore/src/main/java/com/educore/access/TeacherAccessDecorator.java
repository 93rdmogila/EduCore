package com.educore.access;

public class TeacherAccessDecorator extends AccessDecorator{
    public TeacherAccessDecorator(Access wrappedAccess) {
        super(wrappedAccess);
    }

    @Override
    public boolean hasPermission(Permission permission) {
        return permission == Permission.ADD_GRADE
                || permission == Permission.VIEW_ANY_GRADES
                || super.hasPermission(permission)
				|| permission == Permission.UPDATE_GRADE;
    }
}

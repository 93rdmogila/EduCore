package com.educore.access;

public class AdministratorAccessDecorator extends AccessDecorator{
    public AdministratorAccessDecorator(Access wrappedAccess) {
        super(wrappedAccess);
    }

    @Override
    public boolean hasPermission(Permission permission) {
        return permission == Permission.REGISTER_USERS
				|| permission == Permission.MANAGE_CLASSES
                || permission == Permission.VIEW_ANY_GRADES
                || super.hasPermission(permission);
    }
}

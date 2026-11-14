package com.educore.access;

public abstract class AccessDecorator implements Access{
    private final Access wrappedAccess;

    protected AccessDecorator(Access wrappedAccess) {
        if (wrappedAccess == null) {
            throw new IllegalArgumentException("Wrapped access can not be null.");
        }
        this.wrappedAccess = wrappedAccess;
    }

    @Override
    public boolean hasPermission(Permission permission) {
        return wrappedAccess.hasPermission(permission);
    }
}

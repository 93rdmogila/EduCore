package com.educore.access;

public class BasicAccess implements Access{

    @Override
    public boolean hasPermission(Permission permission) {
        return false;
    }
}

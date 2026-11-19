package com.educore.user.auth;

public interface PasswordHasher {
	String hash(String rawPassword);
	boolean verify(String rawPassword, String storedHash);
}

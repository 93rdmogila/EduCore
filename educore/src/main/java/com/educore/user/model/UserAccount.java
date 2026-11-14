package com.educore.user.model;

import com.educore.user.util.Validator;
import java.util.regex.Pattern;

public class UserAccount {
	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

	private final String userId;
	private final String email;
	private final String passwordHash;
	private final UserRole role;

	public UserAccount(String userId, String email, String passwordHash, String role){
		Validator.requireNonBlank(userId, "User id");
		Validator.requireEmail(email);
		Validator.requireNonBlank(passwordHash, "Password hash");
		if(role == null){
			throw new IllegalArgumentException("User role cannot be null.");
		}

		this.userId = userId;
		this.email = email;
		this.passwordHash = passwordHash;
		this.role = role;
	}
	
	public String getUserId(){
		return userId;
	}
	public String getEmail(){
		return email;
	}
	public String getPasswordHash(){
		return passwordHash;
	}
	public String getRole(){
		return role;
	}
}

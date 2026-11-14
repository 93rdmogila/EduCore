package com.educore.user.model;

import com.educore.user.util.Validator;

public class UserAccount {
	private final String userId;
	private final String email;
	private final String passwordHash;
	private final UserRole role;

	public UserAccount(String userId, String email, String passwordHash, UserRole role){
		Validator.requireNonBlank(userId, "User id");
		Validator.requireEmail(email);
		Validator.requireNonBlank(passwordHash, "Password hash");
		if(role == null){
			throw new IllegalArgumentException("User role cannot be null.");
		}

		this.userId = userId;
		this.email = email;
		this.passwordHash = passwordHash;
		this.role = role
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
	public UserRole getRole(){
		return role;
	}
}

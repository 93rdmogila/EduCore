package com.educore.user.auth;

import com.educore.user.model.UserRole;
import com.educore.user.util.Validator;

import java.time.Instant;

// Who is logged in right now. Other modules take userId and role from here.
public class Session {
	private final String userId;
	private final UserRole role;
	private final Instant loginTime;
	private boolean active;

	public Session(String userId, UserRole role){
		Validator.requireNonBlank(userId, "User id");
		if(role == null){
			throw new IllegalArgumentException("User role cannot be null.");
		}
		this.userId = userId;
		this.role = role;
		this.loginTime = Instant.now();
		this.active = true;
	}

	public String getUserId(){
		return userId;
	}
	public UserRole getRole(){
		return role;
	}
	public Instant getLoginTime(){
		return loginTime;
	}
	public boolean isActive(){
		return active;
	}
	public void logout(){
		this.active = false;
	}
}
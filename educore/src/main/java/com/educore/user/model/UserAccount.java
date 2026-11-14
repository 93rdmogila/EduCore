package com.educore.user.model;

public class UserAccount {
	private String userId;
	private String email;
	private String passwordHash;
	private String role;

	public UserAccount(String userId, String email, String passwordHash, String role){
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

	public String setUserId(String userId){
		this.userId = userId;
	}
	public String setEmail(String email){
		this.email = email;
	}
	public String setPasswordHash(String passwordHash){
		this.passwordHash = passwordHash;
	}
	public String setRole(String role){
		this.role = role;
	}
}

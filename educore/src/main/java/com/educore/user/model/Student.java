package com.educore.user.model;

public class Student implements User{
	private String id;
	private String name;
	private StudentProfile profile;

	public Student(String id, String name, StudentProfile profile){
		this.id = id;
		this.name = name;
		this.profile = profile;
	}

	@Override 
	public String getId(){
		return id;
	}
	@Override 
	public String gettName(){
		return name;
	}
	public StudentProfile getstudentProfile(){
		return profile;
	}
}

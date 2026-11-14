package com.educore.user.model;

public class Student implements User{
	private final String id;
	private final String name;
	private final StudentProfile profile;

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
	public String getName(){
		return name;
	}
	public StudentProfile getStudentProfile(){
		return profile;
	}
}

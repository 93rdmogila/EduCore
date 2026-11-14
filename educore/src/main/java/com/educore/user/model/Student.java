package com.educore.user.model;

import com.educore.user.util.Validator;

public class Student implements User{
	private final String id;
	private final String name;
	private final StudentProfile profile;

	public Student(String id, String name, StudentProfile profile){
		Validator.requireNonBlank(id, "Id");
		Validator.requireNonBlank(name, "Name");
		if(profile == null){
			throw new IllegalArgumentException("Student profile cannot be null");
		}
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

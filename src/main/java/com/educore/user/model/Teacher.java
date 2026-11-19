package com.educore.user.model;

import com.educore.user.util.Validator;

public class Teacher implements User{
	private final String id;
	private final String name;
	private final TeacherSpecialization specialization;

	public Teacher(String id, String name, TeacherSpecialization specialization){
		Validator.requireNonBlank(id, "Id");
		Validator.requireNonBlank(name, "Name");
		if(specialization == null){
			throw new IllegalArgumentException("Teacher specialization cannot be null.");
		}
		this.id = id;
		this.name = name;
		this.specialization = specialization;
	}

	@Override 
	public String getId(){
		return id;
	}
	@Override 
	public String getName(){
		return name;
	}
	public TeacherSpecialization getSpecialization(){
		return specialization;
	}
}

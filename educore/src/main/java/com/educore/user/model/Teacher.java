package com.educore.user.model;

public class Teacher implements User{
	private final String id;
	private final String name;
	private final String specialization;

	public Teacher(String id, String name, String specialization){
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
	public String getSpecialization(){
		return specialization;
	}
}

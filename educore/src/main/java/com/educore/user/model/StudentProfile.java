package com.educore.user.model;

import java.util.List;

public class StudentProfile {
	private final String className;
	private final String parentContact;
	private final List<String> subjects;
	private final String address;
	private final String dateOfBirth;

	public StudentProfile(String className, String parentContact, List<String> subjects, String address, String dateOfBirth){
		this.className = className;
		this.parentContact = parentContact;
		this.subjects = subjects;
		this.address = address;
		this.dateOfBirth = dateOfBirth;
	}

	public String getClassName(){
		return className;
	}
	public String getParentContact(){
		return parentContact;
	}
	public List<String> getSubjects(){
		return subjects;
	}
	public String getAddress(){
		return address;
	}
	public String getDateOfBirth(){
		return dateOfBirth;
	}
}

package com.educore.user.model;

import java.util.List;

public class StudentProfile {
	private String className;
	private String parentContact;
	private List<String> subjects;
	private String address;
	private String dateOfBirth;

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
	public List<String> getSubjects{
		return subjects;
	}
	public String getAddress(){
		return address;
	}
	public String getDateOfBirth(){
		return dateOfBirth;
	}

	public String setClassName(String className){
		this.className = className;
	}
	public String setParentContact(String parentContact){
		this.parentContact = parentContact;
	}
	public List<String> setSubjects(List<String> subjects){
		this.subjects = subjects;
	}
	public String setAddress(String address){
		this.address = address;
	}
	public String setDateOfBirth(String dateOfBirth){
		this.dateOfBirth = dateOfBirth;
	}
}

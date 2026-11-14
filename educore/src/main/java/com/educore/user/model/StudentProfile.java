package com.educore.user.model;

import java.util.List;
import java.util.Collections;
import com.educore.user.util.Validator;

public class StudentProfile {
	private final String className;
	private final String parentContact;
	private final List<String> subjects;
	private final String address;
	private final String dateOfBirth;

	//constructor is package-private
	StudentProfile(String className, String parentContact, List<String> subjects, String address, String dateOfBirth){
		Validator.requireNonBlank(className, "Class name");
		Validator.requirePhone(parentContact);
		if(address != null && address.isBlank()){
			throw new IllegalArgumentException("Address cannot be blank.");
		}
		if(dateOfBirth != null && dateOfBirth.isBlank()){
			throw new IllegalArgumentException("Date of birth cannot be blank.");
		}
		this.className = className;
		this.parentContact = parentContact;
		this.subjects = subjects != null ? List.copyOf(subjects) : Collections.emptyList();
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

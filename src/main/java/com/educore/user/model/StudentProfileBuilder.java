package com.educore.user.model;

import com.educore.user.util.Validator;

import java.util.List;
import java.util.ArrayList;

public class StudentProfileBuilder {
	private String className;
	private String parentContact;
	private List<String> subjects = new ArrayList<>();
	private String address;
	private String dateOfBirth;

	public StudentProfileBuilder className(String className){
		this.className = className;
		return this;
	}
	public StudentProfileBuilder parentContact(String parentContact){
		this.parentContact = parentContact;
		return this;
	}
	public StudentProfileBuilder subjects(List<String> subjects){
		this.subjects = subjects != null ? new ArrayList<>(subjects) : new ArrayList<>();
		return this;
	}
	public StudentProfileBuilder addSubject(String subject){
		Validator.requireNonBlank(subject, "Subject");
		this.subjects.add(subject);
		return this;
	}
	public StudentProfileBuilder address(String address){
		this.address = address;
		return this;
	}
	public StudentProfileBuilder dateOfBirth(String dateOfBirth){
		this.dateOfBirth = dateOfBirth;
		return this;
	}

	public StudentProfile build(){
		Validator.requireNonBlank(className, "Class name");
		Validator.requirePhone(parentContact);

		return new StudentProfile(className, parentContact, subjects, address, dateOfBirth);
	}
}

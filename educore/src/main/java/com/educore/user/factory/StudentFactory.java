package com.educore.user.factory;

import java.util.List;

import com.educore.user.model.*;
import com.educore.user.util.Validator;

public class StudentFactory implements UserFactory{
	public Student createStudent(String id, String name, String className, String parentContact){
		Validator.requireNonBlank(id, "Id");
		Validator.requireNonBlank(name, "Name");
		StudentProfile profile = new StudentProfileBuilder().className(className)
															.parentContact(parentContact)
															.build();
		return new Student(id, name, profile);
	}

	public Student createStudent(String id, String name, String className, String parentContact, List<String> subjects,
								String address, String dateOfBirth){
		Validator.requireNonBlank(id, "Id");
		Validator.requireNonBlank(name, "Name");
		StudentProfile profile = new StudentProfileBuilder().className(className)
															.parentContact(parentContact)
															.subjects(subjects)
															.address(address)
															.dateOfBirth(dateOfBirth)
															.build();
		return new Student(id, name, profile);
	}

	@Override 
	public UserAccount createAccount(String userId, String email, String passwordHash){
		Validator.requireNonBlank(userId, "User id");
		Validator.requireEmail(email);
		Validator.requireNonBlank(passwordHash, "Password hash");
		return new UserAccount(userId, email, passwordHash, UserRole.STUDENT);
	}
}

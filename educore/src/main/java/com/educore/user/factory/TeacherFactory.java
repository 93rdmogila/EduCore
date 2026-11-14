package com.educore.user.factory;

import com.educore.user.model.*;
import com.educore.user.util.Validator;


public class TeacherFactory implements UserFactory{
	public Teacher createTeacher(String id, String name, TeacherSpecialization specialization){
		Validator.requireNonBlank(id, "Id");
		Validator.requireNonBlank(name, "Name");
		return new Teacher(id, name, specialization);
	}

	@Override 
	public UserAccount createAccount(String userId, String email, String passwordHash){
		Validator.requireNonBlank(userId, "User id");
		Validator.requireEmail(email);
		Validator.requireNonBlank(passwordHash, "Password hash");
		return new UserAccount(userId, email, passwordHash, UserRole.TEACHER);
	}
}

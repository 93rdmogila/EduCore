package com.educore.user.factory;

import com.educore.user.model.*;
import com.educore.user.util.Validator;

public class AdministratorFactory implements UserFactory{
	public Administrator createAdministrator(String id, String name){
		Validator.requireNonBlank(id, "Id");
		Validator.requireNonBlank(name, "Name");
		return new Administrator(id, name);
	}

	@Override 
	public UserAccount createAccount(String userId, String email, String passwordHash){
		Validator.requireNonBlank(userId, "User id");
		Validator.requireEmail(email);
		Validator.requireNonBlank(passwordHash, "Password hash");
		return new UserAccount(userId, email, passwordHash, UserRole.ADMINISTRATOR);
	}
}

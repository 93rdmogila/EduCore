package com.educore.user.auth;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

import com.educore.user.factory.*;
import com.educore.user.repository.UserRepository;
import com.educore.user.util.Validator;
import com.educore.user.model.*;

public class RegistrationService {
	private static final int MIN_PASSWORD_LENGTH = 8;

	private final UserRepository repository;
	private final PasswordHasher hasher;
	private final StudentFactory studentFactory = new StudentFactory();
	private final TeacherFactory teacherFactory = new TeacherFactory();
	private final AdministratorFactory administratorFactory = new AdministratorFactory();

	public RegistrationService(UserRepository repository, PasswordHasher hasher){
		this.repository = repository;
		this.hasher = hasher;
	}

	public Student registerStudent(String name, String email, String password, String className, String parentContact){
		String normalizedEmail = checkCredentials(email, password);
		String id = UUID.randomUUID().toString();
		Student student = studentFactory.createStudent(id, name, className, parentContact);
		saveUserWithAccount(student, studentFactory, normalizedEmail, password);
		return student;
	}

	public Teacher registerTeacher(String name, String email, String password, TeacherSpecialization specialization){
		String normalizedEmail = checkCredentials(email, password);
		String id = UUID.randomUUID().toString();
		Teacher teacher = teacherFactory.createTeacher(id, name, specialization);
		saveUserWithAccount(teacher, teacherFactory, normalizedEmail, password);
		return teacher;
	}

	public Administrator registerAdministrator(Session creator, String name, String email, String password){
		requireAdministrator(creator);
		return createAdministrator(name, email, password);
	}
	public Administrator registerFirstAdministrator(String name, String email, String password){
		if(hasAdministrator()){
			throw new IllegalStateException("An administrator already exists.");
		}
		return createAdministrator(name, email, password);
	}
	private Administrator createAdministrator(String name, String email, String password){
		String normalizedEmail = checkCredentials(email, password);
		String id = UUID.randomUUID().toString();
		Administrator administrator = administratorFactory.createAdministrator(id, name);
		saveUserWithAccount(administrator, administratorFactory, normalizedEmail, password);
		return administrator;
	}

	private String checkCredentials(String email, String password){
		if(email == null){
			throw new IllegalArgumentException("Email cannot be null.");
		}
		String normalized = email.trim().toLowerCase();
		Validator.requireEmail(normalized);
		if(password == null || password.length() < MIN_PASSWORD_LENGTH){
			throw new IllegalArgumentException("Password must be at least 8 characters long.");
		}
		if(repository.existsAccountByEmail(normalized)){
			throw new IllegalArgumentException("Email is already registered.");
		}
		return normalized;
	}
	private void saveUserWithAccount(User user, UserFactory factory, String email, String password){
		String hash = hasher.hash(password);
		UserAccount account = factory.createAccount(user.getId(), email, hash);
		repository.saveAccount(account);
		repository.saveUser(user);
	}
	private void requireAdministrator(Session session){
		if(session == null || !session.isActive() || session.getRole() != UserRole.ADMINISTRATOR){
			throw new SecurityException("Only an administrator can create administrator accounts.");
		}
		Optional<User> optionalUser = repository.findUserById(session.getUserId());
		if (optionalUser.isEmpty()) {
			throw new SecurityException("User not found");
		}
		User user = optionalUser.get();
		if (!(user instanceof Administrator)) {
			throw new SecurityException("Only administrator can perform this operation.");
		}
	}
	private boolean hasAdministrator(){
		List<User> users = repository.findAllUsers();
		for(User user : users){
			if(user instanceof Administrator){
				return true;
			}
		}
		return false;
	}
}

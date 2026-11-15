package com.educore;

import com.educore.user.auth.AuthService;
import com.educore.user.auth.AuthenticationException;
import com.educore.user.auth.PasswordHasher;
import com.educore.user.auth.Pbkdf2PasswordHasher;
import com.educore.user.auth.RegistrationService;
import com.educore.user.auth.Session;
import com.educore.user.model.Administrator;
import com.educore.user.model.Student;
import com.educore.user.model.UserRole;
import com.educore.user.repository.InMemoryUserRepository;
import com.educore.user.repository.UserRepository;

import java.util.List;

public class Main {

    public static void main(String[] args) {
		UserRepository repo = new InMemoryUserRepository();
		PasswordHasher hasher = new Pbkdf2PasswordHasher();
		RegistrationService reg = new RegistrationService(repo, hasher);
		AuthService auth = new AuthService(repo, hasher);

		reg.registerStudent("Alex", "alex@mail.com", "secret123", "SE-2526", "+77001234567");

		Session s = auth.login("ALEX@mail.com", "secret123");        // регистр не важен
		System.out.println(s.getRole() + " " + s.isActive());        // STUDENT true

		try { auth.login("alex@mail.com", "wrong-pass"); }
		catch (AuthenticationException e) { System.out.println("1: " + e.getMessage()); }
		try { auth.login("nobody@mail.com", "secret123"); }
		catch (AuthenticationException e) { System.out.println("2: " + e.getMessage()); }
		// 1 и 2 должны вывести один и тот же текст

		auth.logout(s);
		System.out.println(s.isActive());                            // false
	}
}
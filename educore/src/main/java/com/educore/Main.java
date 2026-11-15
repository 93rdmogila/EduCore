package com.educore;

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
		RegistrationService reg = new RegistrationService(repo, new Pbkdf2PasswordHasher());
		Administrator admin = reg.registerFirstAdministrator("Root", "root@school.kz", "admin12345");

		// 1. второй «первый» админ должен упасть
		try { reg.registerFirstAdministrator("Evil", "evil@school.kz", "admin12345"); }
		catch (IllegalStateException e) { System.out.println("ok: " + e.getMessage()); }
		
		// 2. настоящий админ создаёт второго
		Session real = new Session(admin.getId(), UserRole.ADMINISTRATOR);
		reg.registerAdministrator(real, "Second", "second@school.kz", "admin12345");
		System.out.println("admins: " + repo.findAllUsers().size());   // нужно иметь repo в переменной

		// 3. подделка и студент должны упасть
		try { reg.registerAdministrator(new Session("fake-id", UserRole.ADMINISTRATOR),
		        "X", "x@school.kz", "admin12345"); }
		catch (SecurityException e) { System.out.println("ok: forged session rejected"); }
		try { reg.registerAdministrator(new Session("anyone", UserRole.STUDENT),
		        "X", "x@school.kz", "admin12345"); }
		catch (SecurityException e) { System.out.println("ok: student rejected"); }
	}
}
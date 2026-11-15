package com.educore;

import com.educore.academic.model.Grade;
import com.educore.academic.model.GradeType;
import com.educore.academic.model.Notification;
import com.educore.academic.repository.JsonGradeRepository;
import com.educore.academic.repository.JsonNotificationRepository;
import com.educore.academic.service.GradeService;
import com.educore.academic.strategy.GradeCalculationStrategy;
import com.educore.academic.strategy.HighestGradeStrategy;
import com.educore.academic.strategy.SimpleAverageStrategy;
import com.educore.academic.strategy.WeightedAverageStrategy;
import com.educore.access.AccessGuard;
import com.educore.facade.SchoolFacade;
import com.educore.user.auth.AuthService;
import com.educore.user.auth.AuthenticationException;
import com.educore.user.auth.PasswordHasher;
import com.educore.user.auth.Pbkdf2PasswordHasher;
import com.educore.user.auth.RegistrationService;
import com.educore.user.auth.Session;
import com.educore.user.model.Administrator;
import com.educore.user.model.Student;
import com.educore.user.model.Teacher;
import com.educore.user.model.TeacherSpecialization;
import com.educore.user.model.User;
import com.educore.user.repository.JsonUserRepository;
import com.educore.user.repository.UserRepository;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class Main {
	private static final Scanner in = new Scanner(System.in);
	private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private static UserRepository users;
	private static SchoolFacade facade;
	private static Session session;   // null = nobody is logged in

	public static void main(String[] args) {
		try {
			start();
		} catch (IllegalStateException e) {
			// e.g. a corrupted JSON file: show a clear message instead of a stack trace
			System.out.println("Cannot start: " + e.getMessage());
			return;
		}

		boolean running = true;
		while (running) {
			try {
				running = (session == null) ? guestMenu() : roleMenu();
			} catch (IllegalArgumentException | IllegalStateException
					| SecurityException | AuthenticationException e) {
				System.out.println("Error: " + e.getMessage());
			}
		}
		System.out.println("Goodbye!");
	}

	// ---------- setup ----------

	private static void start() {
		Path dataDir = Paths.get("data");
		users = new JsonUserRepository(dataDir);
		PasswordHasher hasher = new Pbkdf2PasswordHasher();
		GradeService gradeService = new GradeService(
				users, new JsonGradeRepository(dataDir), new JsonNotificationRepository(dataDir));
		facade = new SchoolFacade(users, gradeService, new AccessGuard(),
				new RegistrationService(users, hasher), new AuthService(users, hasher));

		System.out.println("================================");
		System.out.println("           EDUCORE");
		System.out.println("     School Management System");
		System.out.println("================================");

		if (!hasAdministrator()) {
			System.out.println("\nNo administrator exists yet. Create the first administrator.");
			createFirstAdministrator();
		}
	}

	private static boolean hasAdministrator() {
		for (User user : users.findAllUsers()) {
			if (user instanceof Administrator) {
				return true;
			}
		}
		return false;
	}

	private static void createFirstAdministrator() {
		while (true) {
			try {
				String name = ask("Name");
				String email = ask("Email");
				String password = ask("Password (min 8 characters)");
				facade.registerFirstAdministrator(name, email, password);
				System.out.println("Administrator created. You can log in now.");
				return;
			} catch (IllegalArgumentException e) {
				System.out.println("Error: " + e.getMessage() + " Try again.");
			}
		}
	}

	// ---------- menus ----------

	private static boolean guestMenu() {
		System.out.println("\n--- Main menu ---");
		System.out.println("1. Register as student");
		System.out.println("2. Login");
		System.out.println("0. Exit");
		switch (askInt("Choose")) {
			case 1: registerStudent(); return true;
			case 2: login(); return true;
			case 0: return false;
			default: System.out.println("Unknown option."); return true;
		}
	}

	private static boolean roleMenu() {
		switch (session.getRole()) {
			case STUDENT: return studentMenu();
			case TEACHER: return teacherMenu();
			case ADMINISTRATOR: return administratorMenu();
			default: throw new IllegalStateException("Unknown role");
		}
	}

	private static boolean studentMenu() {
		System.out.println("\n--- Student menu ---");
		System.out.println("1. My grades");
		System.out.println("2. My notifications");
		System.out.println("3. My final grade");
		System.out.println("0. Logout");
		switch (askInt("Choose")) {
			case 1: showGrades(session.getUserId()); break;
			case 2: showNotifications(); break;
			case 3: showFinalGrade(session.getUserId()); break;
			case 0: logout(); break;
			default: System.out.println("Unknown option.");
		}
		return true;
	}

	private static boolean teacherMenu() {
		System.out.println("\n--- Teacher menu ---");
		System.out.println("1. List students");
		System.out.println("2. Add grade");
		System.out.println("3. Update grade");
		System.out.println("4. View student grades");
		System.out.println("5. Calculate final grade");
		System.out.println("0. Logout");
		switch (askInt("Choose")) {
			case 1: listStudents(); break;
			case 2: addGrade(); break;
			case 3: updateGrade(); break;
			case 4: { Student s = pickStudent(); if (s != null) showGrades(s.getId()); break; }
			case 5: { Student s = pickStudent(); if (s != null) showFinalGrade(s.getId()); break; }
			case 0: logout(); break;
			default: System.out.println("Unknown option.");
		}
		return true;
	}

	private static boolean administratorMenu() {
		System.out.println("\n--- Administrator menu ---");
		System.out.println("1. Register teacher");
		System.out.println("2. Register administrator");
		System.out.println("3. List students");
		System.out.println("4. List teachers");
		System.out.println("5. View student grades");
		System.out.println("6. Calculate final grade");
		System.out.println("0. Logout");
		switch (askInt("Choose")) {
			case 1: registerTeacher(); break;
			case 2: registerAdministrator(); break;
			case 3: listStudents(); break;
			case 4: listTeachers(); break;
			case 5: { Student s = pickStudent(); if (s != null) showGrades(s.getId()); break; }
			case 6: { Student s = pickStudent(); if (s != null) showFinalGrade(s.getId()); break; }
			case 0: logout(); break;
			default: System.out.println("Unknown option.");
		}
		return true;
	}

	// ---------- actions ----------

	private static void registerStudent() {
		String name = ask("Name");
		String email = ask("Email");
		String password = ask("Password (min 8 characters)");
		String className = ask("Class (e.g. SE-2526)");
		String parent = ask("Parent phone (e.g. +77001234567)");
		Student s = facade.registerStudent(name, email, password, className, parent);
		System.out.println("Registered student " + s.getName() + ". You can log in now.");
	}

	private static void registerTeacher() {
		String name = ask("Name");
		String email = ask("Email");
		String password = ask("Password (min 8 characters)");
		TeacherSpecialization spec = pickSpecialization();
		Teacher t = facade.registerTeacher(session, name, email, password, spec);
		System.out.println("Registered teacher " + t.getName() + " (" + spec + ").");
	}

	private static void registerAdministrator() {
		String name = ask("Name");
		String email = ask("Email");
		String password = ask("Password (min 8 characters)");
		Administrator a = facade.registerAdministrator(session, name, email, password);
		System.out.println("Registered administrator " + a.getName() + ".");
	}

	private static void login() {
		String email = ask("Email");
		String password = ask("Password");
		session = facade.login(email, password);
		System.out.println("Logged in as " + session.getRole() + ".");
	}

	private static void logout() {
		facade.logout(session);
		session = null;
		System.out.println("Logged out.");
	}

	private static void listStudents() {
		List<Student> students = facade.getStudents(session);
		if (students.isEmpty()) {
			System.out.println("No students yet.");
			return;
		}
		for (int i = 0; i < students.size(); i++) {
			Student s = students.get(i);
			System.out.println((i + 1) + ". " + s.getName() + " (" + s.getStudentProfile().getClassName() + ")");
		}
	}

	private static void listTeachers() {
		List<Teacher> teachers = facade.getTeachers(session);
		if (teachers.isEmpty()) {
			System.out.println("No teachers yet.");
			return;
		}
		for (int i = 0; i < teachers.size(); i++) {
			Teacher t = teachers.get(i);
			System.out.println((i + 1) + ". " + t.getName() + " (" + t.getSpecialization() + ")");
		}
	}

	private static void addGrade() {
		Student student = pickStudent();
		if (student == null) {
			return;
		}
		String subject = facade.getTeacherSubject(session);
		GradeType type = pickGradeType();
		int score = askInt("Score (0-100)");
		Grade grade = new Grade(UUID.randomUUID().toString(), student.getId(),
				session.getUserId(), subject, type, score);
		facade.addGrade(session, grade);
		System.out.println("Grade added: " + subject + " " + type + " " + score + " for " + student.getName() + ".");
	}

	private static void updateGrade() {
		Student student = pickStudent();
		if (student == null) {
			return;
		}
		List<Grade> mine = new ArrayList<>();
		for (Grade g : facade.getGrades(session, student.getId())) {
			if (g.getTeacherId().equals(session.getUserId())) {
				mine.add(g);
			}
		}
		if (mine.isEmpty()) {
			System.out.println("You have not given any grades to this student.");
			return;
		}
		for (int i = 0; i < mine.size(); i++) {
			Grade g = mine.get(i);
			System.out.println((i + 1) + ". " + g.getSubject() + " " + g.getType() + ": " + g.getScore());
		}
		int index = askInt("Grade number") - 1;
		if (index < 0 || index >= mine.size()) {
			System.out.println("No such grade.");
			return;
		}
		int newScore = askInt("New score (0-100)");
		facade.updateGrade(session, mine.get(index).getId(), newScore);
		System.out.println("Grade updated.");
	}

	private static void showGrades(String studentId) {
		List<Grade> grades = facade.getGrades(session, studentId);
		if (grades.isEmpty()) {
			System.out.println("No grades yet.");
			return;
		}
		for (Grade g : grades) {
			System.out.println(g.getSubject() + " | " + g.getType() + " | " + g.getScore());
		}
	}

	private static void showNotifications() {
		List<Notification> list = facade.getNotifications(session);
		if (list.isEmpty()) {
			System.out.println("No notifications.");
			return;
		}
		for (Notification n : list) {
			String time = LocalDateTime.ofInstant(Instant.ofEpochMilli(n.getCreatedAt()),
					ZoneId.systemDefault()).format(TIME);
			System.out.println("[" + time + "] " + n.getMessage());
		}
	}

	private static void showFinalGrade(String studentId) {
		TeacherSpecialization subject = pickSpecialization();
		boolean hasGrades = false;
		for (Grade g : facade.getGrades(session, studentId)) {
			if (g.getSubject().equals(subject.name())) {
				hasGrades = true;
			}
		}
		if (!hasGrades) {
			System.out.println("No grades in " + subject + " yet.");
			return;
		}
		GradeCalculationStrategy strategy = pickStrategy();
		double result = facade.calculateFinalGrade(session, studentId, subject.name(), strategy);
		System.out.printf("Final grade in %s: %.2f%n", subject, result);
	}


	private static Student pickStudent() {
		List<Student> students = facade.getStudents(session);
		if (students.isEmpty()) {
			System.out.println("No students yet.");
			return null;
		}
		listStudents();
		int index = askInt("Student number") - 1;
		if (index < 0 || index >= students.size()) {
			System.out.println("No such student.");
			return null;
		}
		return students.get(index);
	}

	private static TeacherSpecialization pickSpecialization() {
		TeacherSpecialization[] all = TeacherSpecialization.values();
		for (int i = 0; i < all.length; i++) {
			System.out.println((i + 1) + ". " + all[i]);
		}
		int index = askInt("Subject number") - 1;
		if (index < 0 || index >= all.length) {
			throw new IllegalArgumentException("No such subject.");
		}
		return all[index];
	}

	private static GradeType pickGradeType() {
		GradeType[] all = GradeType.values();
		for (int i = 0; i < all.length; i++) {
			System.out.println((i + 1) + ". " + all[i]);
		}
		int index = askInt("Grade type number") - 1;
		if (index < 0 || index >= all.length) {
			throw new IllegalArgumentException("No such grade type.");
		}
		return all[index];
	}

	private static GradeCalculationStrategy pickStrategy() {
		System.out.println("1. Simple average");
		System.out.println("2. Weighted average (homework 20%, quiz 30%, exam 50%)");
		System.out.println("3. Highest grade");
		switch (askInt("Strategy")) {
			case 1: return new SimpleAverageStrategy();
			case 2: return new WeightedAverageStrategy();
			case 3: return new HighestGradeStrategy();
			default: throw new IllegalArgumentException("No such strategy.");
		}
	}

	private static String ask(String prompt) {
		System.out.print(prompt + ": ");
		if (!in.hasNextLine()) {
			System.out.println();
			System.out.println("Goodbye!");
			System.exit(0);
		}
		return in.nextLine().trim();
	}

	private static int askInt(String prompt) {
		String text = ask(prompt);
		try {
			return Integer.parseInt(text);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Please enter a number.");
		}
	}
}
package com.educore.user.repository;

import com.educore.user.model.Student;
import com.educore.user.model.StudentProfile;
import com.educore.user.model.Teacher;
import com.educore.user.model.User;
import com.educore.user.model.UserAccount;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
	void saveUser(User user);
	void saveAccount(UserAccount account);

	Optional<User> findUserById(String id);
	Optional<UserAccount> findAccountByEmail(String email);
	Optional<UserAccount> findAccountByUserId(String userId);

	boolean existsAccountByEmail(String email);

	// lists for other modules (e.g. Observer subscriptions, "View all students")
	List<User> findAllUsers();
	List<Student> findAllStudents();
	List<Teacher> findAllTeachers();

	// the profile is immutable, so the Student is replaced by a new one with the same id
	void updateStudentProfile(String userId, StudentProfile newProfile);
}
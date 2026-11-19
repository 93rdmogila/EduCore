package com.educore.user.repository;

import com.educore.user.model.Student;
import com.educore.user.model.StudentProfile;
import com.educore.user.model.Teacher;
import com.educore.user.model.User;
import com.educore.user.model.UserAccount;
import com.educore.user.util.Validator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Temporary storage in memory. Will be replaced by a JSON-based implementation.
public class InMemoryUserRepository implements UserRepository {
	private final Map<String, User> users = new LinkedHashMap<>();
	// key = userId
	private final Map<String, UserAccount> accounts = new LinkedHashMap<>();

	private static String normalizeEmail(String email){
		return email == null ? null : email.trim().toLowerCase();
	}

	@Override
	public void saveUser(User user){
		if(user == null){
			throw new IllegalArgumentException("User cannot be null.");
		}
		users.put(user.getId(), user);
	}

	@Override
	public void saveAccount(UserAccount account){
		if(account == null){
			throw new IllegalArgumentException("Account cannot be null.");
		}
		Optional<UserAccount> sameEmail = findAccountByEmail(account.getEmail());
		if(sameEmail.isPresent() && !sameEmail.get().getUserId().equals(account.getUserId())){
			throw new IllegalArgumentException("Email is already registered: " + account.getEmail());
		}
		accounts.put(account.getUserId(), account);
	}

	@Override
	public Optional<User> findUserById(String id){
		return Optional.ofNullable(users.get(id));
	}

	@Override
	public Optional<UserAccount> findAccountByEmail(String email){
		String wanted = normalizeEmail(email);
		if(wanted == null){
			return Optional.empty();
		}
		for(UserAccount account : accounts.values()){
			if(normalizeEmail(account.getEmail()).equals(wanted)){
				return Optional.of(account);
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<UserAccount> findAccountByUserId(String userId){
		return Optional.ofNullable(accounts.get(userId));
	}

	@Override
	public boolean existsAccountByEmail(String email){
		return findAccountByEmail(email).isPresent();
	}

	@Override
	public List<User> findAllUsers(){
		return new ArrayList<>(users.values());
	}

	@Override
	public List<Student> findAllStudents(){
		List<Student> result = new ArrayList<>();
		for(User user : users.values()){
			if(user instanceof Student){
				result.add((Student) user);
			}
		}
		return result;
	}

	@Override
	public List<Teacher> findAllTeachers(){
		List<Teacher> result = new ArrayList<>();
		for(User user : users.values()){
			if(user instanceof Teacher){
				result.add((Teacher) user);
			}
		}
		return result;
	}

	@Override
	public void updateStudentProfile(String userId, StudentProfile newProfile){
		Validator.requireNonBlank(userId, "User id");
		if(newProfile == null){
			throw new IllegalArgumentException("Student profile cannot be null.");
		}
		User existing = users.get(userId);
		if(!(existing instanceof Student)){
			throw new IllegalArgumentException("Student not found: " + userId);
		}
		// immutable model: replace the object, keep the same id
		users.put(userId, new Student(existing.getId(), existing.getName(), newProfile));
	}
}
package com.educore.user.repository;

import com.educore.storage.JsonFile;
import com.educore.user.factory.AdministratorFactory;
import com.educore.user.factory.StudentFactory;
import com.educore.user.factory.TeacherFactory;
import com.educore.user.model.Administrator;
import com.educore.user.model.Student;
import com.educore.user.model.StudentProfile;
import com.educore.user.model.Teacher;
import com.educore.user.model.TeacherSpecialization;
import com.educore.user.model.User;
import com.educore.user.model.UserAccount;
import com.educore.user.model.UserRole;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Stores users in users.json and accounts in accounts.json.
// All data is loaded into memory on start; every change is written back to the files.
public class JsonUserRepository implements UserRepository {
	private final InMemoryUserRepository memory = new InMemoryUserRepository();
	private final JsonFile usersFile;
	private final JsonFile accountsFile;
	// all accounts in saving order (kept to write accounts.json without losing any)
	private final Map<String, UserAccount> accounts = new LinkedHashMap<>();

	private final StudentFactory studentFactory = new StudentFactory();
	private final TeacherFactory teacherFactory = new TeacherFactory();
	private final AdministratorFactory administratorFactory = new AdministratorFactory();

	public JsonUserRepository(Path directory){
		this.usersFile = new JsonFile(directory.resolve("users.json"));
		this.accountsFile = new JsonFile(directory.resolve("accounts.json"));
		load();
	}

	private void load(){
		for(Map<String, Object> record : accountsFile.readAll()){
			UserAccount account = new UserAccount(
					JsonFile.requireString(record, "userId"),
					JsonFile.requireString(record, "email"),
					JsonFile.requireString(record, "passwordHash"),
					UserRole.valueOf(JsonFile.requireString(record, "role")));
			memory.saveAccount(account);
			accounts.put(account.getUserId(), account);
		}
		for(Map<String, Object> record : usersFile.readAll()){
			memory.saveUser(toUser(record));
		}
	}

	@SuppressWarnings("unchecked")
	private User toUser(Map<String, Object> r){
		String id = JsonFile.requireString(r, "id");
		String name = JsonFile.requireString(r, "name");
		String type = JsonFile.requireString(r, "type");
		switch(type){
			case "STUDENT":
				List<String> subjects = new ArrayList<>();
				Object raw = r.get("subjects");
				if(raw instanceof List){
					for(Object s : (List<Object>) raw){
						subjects.add(String.valueOf(s));
					}
				}
				return studentFactory.createStudent(id, name,
						JsonFile.requireString(r, "className"),
						JsonFile.requireString(r, "parentContact"),
						subjects,
						JsonFile.optionalString(r, "address"),
						JsonFile.optionalString(r, "dateOfBirth"));
			case "TEACHER":
				return teacherFactory.createTeacher(id, name,
						TeacherSpecialization.valueOf(JsonFile.requireString(r, "specialization")));
			case "ADMINISTRATOR":
				return administratorFactory.createAdministrator(id, name);
			default:
				throw new IllegalStateException("Unknown user type in users.json: " + type);
		}
	}

	private Map<String, Object> toRecord(User user){
		Map<String, Object> r = new LinkedHashMap<>();
		r.put("id", user.getId());
		r.put("name", user.getName());
		if(user instanceof Student){
			StudentProfile p = ((Student) user).getStudentProfile();
			r.put("type", "STUDENT");
			r.put("className", p.getClassName());
			r.put("parentContact", p.getParentContact());
			r.put("address", p.getAddress());
			r.put("dateOfBirth", p.getDateOfBirth());
			r.put("subjects", new ArrayList<Object>(p.getSubjects()));
		}else if(user instanceof Teacher){
			r.put("type", "TEACHER");
			r.put("specialization", ((Teacher) user).getSpecialization().name());
		}else if(user instanceof Administrator){
			r.put("type", "ADMINISTRATOR");
		}else{
			throw new IllegalStateException("Unknown user class: " + user.getClass().getName());
		}
		return r;
	}

	private void persistUsers(){
		List<Map<String, Object>> records = new ArrayList<>();
		for(User user : memory.findAllUsers()){
			records.add(toRecord(user));
		}
		usersFile.writeAll(records);
	}

	private void persistAccounts(){
		List<Map<String, Object>> records = new ArrayList<>();
		for(UserAccount account : accounts.values()){
			records.add(accountRecord(account));
		}
		accountsFile.writeAll(records);
	}

	private Map<String, Object> accountRecord(UserAccount a){
		Map<String, Object> r = new LinkedHashMap<>();
		r.put("userId", a.getUserId());
		r.put("email", a.getEmail());
		r.put("passwordHash", a.getPasswordHash());
		r.put("role", a.getRole().name());
		return r;
	}

	@Override
	public void saveUser(User user){
		memory.saveUser(user);
		persistUsers();
	}

	@Override
	public void saveAccount(UserAccount account){
		memory.saveAccount(account);   // rejects a duplicate email
		accounts.put(account.getUserId(), account);
		persistAccounts();
	}

	@Override
	public Optional<User> findUserById(String id){
		return memory.findUserById(id);
	}

	@Override
	public Optional<UserAccount> findAccountByEmail(String email){
		return memory.findAccountByEmail(email);
	}

	@Override
	public Optional<UserAccount> findAccountByUserId(String userId){
		return memory.findAccountByUserId(userId);
	}

	@Override
	public boolean existsAccountByEmail(String email){
		return memory.existsAccountByEmail(email);
	}

	@Override
	public List<User> findAllUsers(){
		return memory.findAllUsers();
	}

	@Override
	public List<Student> findAllStudents(){
		return memory.findAllStudents();
	}

	@Override
	public List<Teacher> findAllTeachers(){
		return memory.findAllTeachers();
	}

	@Override
	public void updateStudentProfile(String userId, StudentProfile newProfile){
		memory.updateStudentProfile(userId, newProfile);
		persistUsers();
	}
}
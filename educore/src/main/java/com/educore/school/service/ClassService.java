package com.educore.school.service;

import com.educore.school.model.SchoolClass;
import com.educore.school.repository.ClassRepository;
import com.educore.user.model.Student;
import com.educore.user.model.Teacher;
import com.educore.user.model.User;
import com.educore.user.repository.UserRepository;
import com.educore.user.util.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ClassService {
	private final ClassRepository classes;
	private final UserRepository users;

	public ClassService(ClassRepository classes, UserRepository users){
		if(classes == null || users == null){
			throw new IllegalArgumentException("Class service dependencies cannot be null.");
		}
		this.classes = classes;
		this.users = users;
	}
	
	public SchoolClass createClass(String name){
		Validator.requireNonBlank(name, "Name");
		SchoolClass created = new SchoolClass(UUID.randomUUID().toString(), name, null, List.of());
		classes.save(created);
		return created;
	}

	public SchoolClass assignTeacher(String classId, String teacherId){
		SchoolClass target = requireClass(classId);
		User user = users.findUserById(teacherId)
					.orElseThrow(() -> new IllegalArgumentException("Teacher not found."));
		if(!(user instanceof Teacher)){
			throw new IllegalArgumentException("User is not a teacher.");
		}
		SchoolClass updated = target.withTeacher(teacherId);
		classes.save(updated);
		return updated;
	}

	public List<SchoolClass> findAll(){
		return classes.findAll();
	}

	public List<SchoolClass> findByTeacher(String teacherId){
		return classes.findByTeacherId(teacherId);
	}

	public List<String> studentIdsOfTeacher(String teacherId){
		List<String> result = new ArrayList<>();
		for(SchoolClass schoolClass : findByTeacher(teacherId)){
			result.addAll(schoolClass.getStudentIds());
		}
		return result;
	}

	public boolean teacherTeachesStudent(String teacherId, String studentId){
		return studentIdsOfTeacher(teacherId).contains(studentId);
	}

	private SchoolClass requireClass(String classId){
		return classes.findById(classId)
			.orElseThrow(() -> new IllegalArgumentException("Class not found."));
	}
}


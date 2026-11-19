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
import java.util.UUID;

// Business logic for classes. WHO may call these methods is decided by SchoolFacade.
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
		Validator.requireNonBlank(name, "Class name");
		SchoolClass created = new SchoolClass(UUID.randomUUID().toString(), name, null, List.of());
		classes.save(created);   // rejects a duplicate name
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

	// A class is a teaching group with one teacher (for example "10A-MATH").
	// A student may belong to several classes, so that every subject teacher can grade him or her.
	public SchoolClass assignStudent(String classId, String studentId){
		SchoolClass target = requireClass(classId);
		User user = users.findUserById(studentId)
				.orElseThrow(() -> new IllegalArgumentException("Student not found."));
		if(!(user instanceof Student)){
			throw new IllegalArgumentException("User is not a student.");
		}
		if(target.hasStudent(studentId)){
			return target;   // already there
		}
		SchoolClass updated = target.withStudent(studentId);
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
		for(SchoolClass c : classes.findByTeacherId(teacherId)){
			result.addAll(c.getStudentIds());
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

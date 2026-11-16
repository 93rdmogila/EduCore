package com.educore.school.model;

import com.educore.user.util.Validator;

import java.util.ArrayList;
import java.util.List;

// A school class: one name, at most one teacher, a list of student ids. Immutable.
public final class SchoolClass {
	private final String id;
	private final String name;
	private final String teacherId;          // null = no teacher assigned yet
	private final List<String> studentIds;

	public SchoolClass(String id, String name, String teacherId, List<String> studentIds){
		Validator.requireNonBlank(id, "Class id");
		Validator.requireNonBlank(name, "Class name");
		if(teacherId != null && teacherId.isBlank()){
			throw new IllegalArgumentException("Teacher id cannot be blank.");
		}
		this.id = id;
		this.name = name.trim();
		this.teacherId = teacherId;
		this.studentIds = studentIds == null ? List.of() : List.copyOf(studentIds);
	}

	public String getId(){
		return id;
	}
	public String getName(){
		return name;
	}
	public String getTeacherId(){
		return teacherId;
	}
	public List<String> getStudentIds(){
		return studentIds;
	}

	public boolean hasStudent(String studentId){
		return studentIds.contains(studentId);
	}

	public SchoolClass withTeacher(String newTeacherId){
		return new SchoolClass(id, name, newTeacherId, studentIds);
	}

	public SchoolClass withStudent(String studentId){
		Validator.requireNonBlank(studentId, "Student id");
		if(hasStudent(studentId)){
			return this;
		}
		List<String> copy = new ArrayList<>(studentIds);
		copy.add(studentId);
		return new SchoolClass(id, name, teacherId, copy);
	}

	public SchoolClass withoutStudent(String studentId){
		Validator.requireNonBlank(studentId, "Student id");
		if(hasStudent(studentId)){
			return this;
		}
		List<String> copy = new ArrayList<>(studentIds);
		copy.remove(studentId);
		return new SchoolClass(id, name, teacherId, copy);
	}
}

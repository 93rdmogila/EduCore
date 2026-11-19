package com.educore.school.repository;

import com.educore.school.model.SchoolClass;
import com.educore.user.util.Validator;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryClassRepository implements ClassRepository{
	private final Map<String, SchoolClass> classes = new LinkedHashMap<>();

	@Override 
	public void save(SchoolClass schoolClass){
		if(schoolClass == null){
			throw new IllegalArgumentException("Class cannot be null.");
		}
		Optional<SchoolClass> saveName = findByName(schoolClass.getName());
		if(saveName.isPresent() && !saveName.get().getId().equals(schoolClass.getId())){
			throw new IllegalArgumentException("Class name already exists: " + schoolClass.getName());
		}
		classes.put(schoolClass.getId(), schoolClass);
	}

	@Override 
	public Optional<SchoolClass> findById(String id){
		return Optional.ofNullable(classes.get(id));
	}

	@Override 
	public Optional<SchoolClass> findByName(String name){
		if(name == null){
			return Optional.empty();
		}
		for(SchoolClass schoolClass : classes.values()){
			if(schoolClass.getName().equalsIgnoreCase(name.trim())){
				return Optional.of(schoolClass);
			}
		}
		return Optional.empty();
	}

	@Override 
	public Optional<SchoolClass> findByStudentId(String studentId){
		Validator.requireNonBlank(studentId, "Student id");
		for(SchoolClass schoolClass : classes.values()){
			if(schoolClass.hasStudent(studentId)){
				return Optional.of(schoolClass);
			}
		}
		return Optional.empty();
	}

	@Override 
	public List<SchoolClass> findByTeacherId(String teacherId){
		List<SchoolClass> result = new ArrayList<>();
		for(SchoolClass schoolClass : classes.values()){
			if(schoolClass.getTeacherId() != null && schoolClass.getTeacherId().equals(teacherId)){
				result.add(schoolClass);
			}
		}
		return result;
	}

	@Override 
	public List<SchoolClass> findAll(){
		return new ArrayList<>(classes.values());
	}
}

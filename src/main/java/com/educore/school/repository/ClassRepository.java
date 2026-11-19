package com.educore.school.repository;

import com.educore.school.model.SchoolClass;

import java.util.Optional;
import java.util.List;
public interface ClassRepository {
	void save(SchoolClass schoolClass);

	Optional<SchoolClass> findById(String id);
	Optional<SchoolClass> findByName(String name);
	Optional<SchoolClass> findByStudentId(String studentId);
	List<SchoolClass> findByTeacherId(String teacherId);
	List<SchoolClass> findAll();
}

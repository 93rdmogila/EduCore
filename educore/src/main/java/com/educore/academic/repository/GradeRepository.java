package com.educore.academic.repository;

import com.educore.academic.model.Grade;

import java.util.List;
import java.util.Optional;

public interface GradeRepository {
    void save(Grade grade);

    List<Grade> findByStudentId(String studentId);

    List<Grade> findByStudentIdAndSubject(String studentId, String subject);
	Optional<Grade> findById(String id);
	void update(Grade grade);
}

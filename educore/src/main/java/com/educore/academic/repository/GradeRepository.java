package com.educore.academic.repository;

import com.educore.academic.model.Grade;

import java.util.List;

public interface GradeRepository {
    void save(Grade grade);

    List<Grade> findByStudentId(String studentId);

    List<Grade> findByStudentIdAndSubject(String studentId, String subject);
}

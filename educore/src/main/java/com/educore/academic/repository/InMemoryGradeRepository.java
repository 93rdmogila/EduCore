
package com.educore.academic.repository;

import com.educore.academic.model.Grade;
import java.util.ArrayList;
import java.util.List;

public class InMemoryGradeRepository implements GradeRepository {
    private final List<Grade> grades = new ArrayList<>();

    @Override
    public void save(Grade grade) {
        if (grade == null) {
            throw new IllegalArgumentException("Grade cannot be null");
        }

        for (Grade existing : grades) {
            if (existing.getId().equals(grade.getId())) {
                throw new IllegalArgumentException("Grade ID already exists");
            }
        }

        grades.add(grade);
    }

    @Override
    public List<Grade> findByStudentId(String studentId) {
        List<Grade> result = new ArrayList<>();

        for (Grade grade : grades) {
            if (grade.getStudentId().equals(studentId)) {
                result.add(grade);
            }
        }

        return result;
    }

    @Override
    public List<Grade> findByStudentIdAndSubject(String studentId, String subject) {
        List<Grade> result = new ArrayList<>();

        for (Grade grade : grades) {
            if (grade.getStudentId().equals(studentId)
                    && grade.getSubject().equals(subject)) {
                result.add(grade);
            }
        }

        return result;
    }
}

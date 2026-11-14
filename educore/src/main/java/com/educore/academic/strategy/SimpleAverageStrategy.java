package com.educore.academic.strategy;

import com.educore.academic.model.Grade;
import java.util.List;

public class SimpleAverageStrategy implements GradeCalculationStrategy {

    @Override
    public double calculate(List<Grade> grades) {
        if (grades == null || grades.isEmpty()) {
            throw new IllegalArgumentException("Grades cannot be empty");
        }

        int sum = 0;

        for (Grade grade : grades) {
            if (grade == null) {
                throw new IllegalArgumentException("Grade cannot be null");
            }

            sum += grade.getScore();
        }

        return (double) sum / grades.size();
    }
}

package com.educore.academic.strategy;

import com.educore.academic.model.Grade;
import java.util.List;

public class GradeCalculator {
    private GradeCalculationStrategy strategy;

    public GradeCalculator(GradeCalculationStrategy strategy) {
        setStrategy(strategy);
    }

    public void setStrategy(GradeCalculationStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy cannot be null");
        }

        this.strategy = strategy;
    }

    public double calculateFinalGrade(List<Grade> grades) {
        return strategy.calculate(grades);
    }
}

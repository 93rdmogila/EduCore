package com.educore.academic.strategy;

import com.educore.academic.model.Grade;
import java.util.List;

public interface GradeCalculationStrategy {
    double calculate(List<Grade> grades);
}

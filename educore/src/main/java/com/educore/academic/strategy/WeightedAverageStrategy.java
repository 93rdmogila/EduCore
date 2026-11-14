package com.educore.academic.strategy;

import com.educore.academic.model.Grade;
import java.util.List;

public class WeightedAverageStrategy implements GradeCalculationStrategy {

    @Override
    public double calculate(List<Grade> grades) {
        if (grades == null || grades.isEmpty()) {
            throw new IllegalArgumentException("Grades cannot be empty");
        }

        int homeworkSum = 0;
        int quizSum = 0;
        int examSum = 0;

        int homeworkCount = 0;
        int quizCount = 0;
        int examCount = 0;

        for (Grade grade : grades) {
            if (grade == null) {
                throw new IllegalArgumentException("Grade cannot be null");
            }

            switch (grade.getType()) {
                case HOMEWORK:
                    homeworkSum += grade.getScore();
                    homeworkCount++;
                    break;
                case QUIZ:
                    quizSum += grade.getScore();
                    quizCount++;
                    break;
                case EXAM:
                    examSum += grade.getScore();
                    examCount++;
                    break;
            }
        }

        double result = 0;
        double totalWeight = 0;

        if (homeworkCount > 0) {
            result += ((double) homeworkSum / homeworkCount) * 0.2;
            totalWeight += 0.2;
        }

        if (quizCount > 0) {
            result += ((double) quizSum / quizCount) * 0.3;
            totalWeight += 0.3;
        }

        if (examCount > 0) {
            result += ((double) examSum / examCount) * 0.5;
            totalWeight += 0.5;
        }

        return result / totalWeight;
    }
}

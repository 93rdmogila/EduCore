package com.educore.academic.strategy;

import com.educore.academic.model.Grade;
import java.util.List;
public class HighestGradeStrategy implements GradeCalculationStrategy{
	@Override 
	public double calculate(List<Grade> grades){
		if(grades == null || grades.isEmpty()){
			throw new IllegalArgumentException("Grades cannot be empty.");
		}
		int highest = Integer.MIN_VALUE;
		for(Grade grade : grades){
			if(grade == null){
				throw new IllegalArgumentException("Grade cannot be null.");
			}
			if(grade.getScore() > highest){
				highest = grade.getScore();
			}
		}
		return highest;
	}
}

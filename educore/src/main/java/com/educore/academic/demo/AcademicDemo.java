
package com.educore.academic.demo;

import com.educore.academic.model.*;
import com.educore.academic.repository.*;
import com.educore.academic.service.GradeService;
import com.educore.academic.strategy.*;
import com.educore.user.auth.Session;
import com.educore.user.factory.StudentFactory;
import com.educore.user.factory.TeacherFactory;
import com.educore.user.model.TeacherSpecialization;
import com.educore.user.model.UserRole;
import com.educore.user.repository.InMemoryUserRepository;
import com.educore.user.repository.UserRepository;

public class AcademicDemo {
    public static void main(String[] args) {
        UserRepository users = new InMemoryUserRepository();

        users.saveUser(new StudentFactory().createStudent(
                "s1", "Alex", "SE-2526", "+77001234567"
        ));

        users.saveUser(new TeacherFactory().createTeacher(
                "t1", "Kim", TeacherSpecialization.MATH
        ));

        GradeService service = new GradeService(
                users,
                new InMemoryGradeRepository(),
                new InMemoryNotificationRepository()
        );

        Session teacher = new Session("t1", UserRole.TEACHER);
        Session student = new Session("s1", UserRole.STUDENT);

        System.out.println("=== EduCore Academic Demo ===");

        service.addGrade(teacher, new Grade(
                "g1", "s1", "t1", "MATH", GradeType.HOMEWORK, 80
        ));

        service.addGrade(teacher, new Grade(
                "g2", "s1", "t1", "MATH", GradeType.QUIZ, 90
        ));

        service.addGrade(teacher, new Grade(
                "g3", "s1", "t1", "MATH", GradeType.EXAM, 100
        ));

        System.out.println("Teacher Kim added 3 grades.");

        System.out.println("\nObserver notifications:");
        for (Notification notification : service.getNotifications(student)) {
            System.out.println(notification.getMessage());
        }

        GradeCalculator calculator =
                new GradeCalculator(new SimpleAverageStrategy());

        double simple = service.calculateFinalGrade(
                student, "s1", "MATH", calculator
        );

        calculator.setStrategy(new WeightedAverageStrategy());

        double weighted = service.calculateFinalGrade(
                student, "s1", "MATH", calculator
        );

        System.out.println("\nStrategy calculations:");
        System.out.println("Simple average: " + simple);
        System.out.println("Weighted average: " + weighted);
    }
}

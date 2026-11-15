
package com.educore.academic.demo;

import com.educore.academic.model.*;
import com.educore.academic.repository.*;
import com.educore.academic.service.GradeService;
import com.educore.academic.strategy.*;
import com.educore.user.auth.AuthService;
import com.educore.user.auth.PasswordHasher;
import com.educore.user.auth.Pbkdf2PasswordHasher;
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

		PasswordHasher hasher = new Pbkdf2PasswordHasher();
		String hash = hasher.hash("secret123");
		StudentFactory studentFactory = new StudentFactory();
		TeacherFactory teacherFactory = new TeacherFactory();

       	users.saveUser(studentFactory.createStudent(
                "s1", "Alex", "SE-2526", "+77001234567"
        ));
        users.saveAccount(studentFactory.createAccount(
                "s1", "alex@mail.com", hash
        ));

        users.saveUser(teacherFactory.createTeacher(
                "t1", "Kim", TeacherSpecialization.MATH
        ));
        users.saveAccount(teacherFactory.createAccount(
                "t1", "kim@mail.com", hash
        ));

        GradeService service = new GradeService(
                users,
                new InMemoryGradeRepository(),
                new InMemoryNotificationRepository()
        );

        AuthService auth = new AuthService(users, hasher);
		Session teacher = auth.login("kim@mail.com", "secret123");
		Session student = auth.login("alex@mail.com", "secret123");

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

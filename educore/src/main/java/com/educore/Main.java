package com.educore;

import com.educore.access.AccessGuard;
import com.educore.academic.model.Grade;
import com.educore.academic.model.GradeType;
import com.educore.academic.model.Notification;
import com.educore.academic.repository.GradeRepository;
import com.educore.academic.repository.InMemoryGradeRepository;
import com.educore.academic.repository.InMemoryNotificationRepository;
import com.educore.academic.repository.NotificationRepository;
import com.educore.academic.service.GradeService;
import com.educore.academic.strategy.SimpleAverageStrategy;
import com.educore.academic.strategy.WeightedAverageStrategy;
import com.educore.facade.SchoolFacade;
import com.educore.user.auth.Session;
import com.educore.user.factory.AdministratorFactory;
import com.educore.user.model.Administrator;
import com.educore.user.model.Student;
import com.educore.user.model.Teacher;
import com.educore.user.model.TeacherSpecialization;
import com.educore.user.model.UserRole;
import com.educore.user.repository.InMemoryUserRepository;
import com.educore.user.repository.UserRepository;

import java.util.List;

public class Main {
    private static final String DEMO_PASSWORD_HASH = "demo-only-placeholder";

    public static void main(String[] args) {
        UserRepository userRepository = new InMemoryUserRepository();
        GradeRepository gradeRepository = new InMemoryGradeRepository();
        NotificationRepository notificationRepository =
                new InMemoryNotificationRepository();

        GradeService gradeService = new GradeService(
                userRepository,
                gradeRepository,
                notificationRepository
        );

        SchoolFacade schoolFacade = new SchoolFacade(
                userRepository,
                gradeService,
                new AccessGuard()
        );

        Administrator administrator = createDemoAdministrator(userRepository);
        Session administratorSession = new Session(
                administrator.getId(),
                UserRole.ADMINISTRATOR
        );

        Teacher teacher = schoolFacade.registerTeacher(
                administratorSession,
                "teacher-1",
                "Zhaksylyk",
                "zhaksylyk@astanait.school.kz",
                DEMO_PASSWORD_HASH,
                TeacherSpecialization.MATH
        );

        Student student = schoolFacade.registerStudent(
                administratorSession,
                "student-1",
                "Bekarys",
                "bekarys@astanait.school.kz",
                DEMO_PASSWORD_HASH,
                "10A",
                "+77476767667"
        );

        Student anotherStudent = schoolFacade.registerStudent(
                administratorSession,
                "student-2",
                "Cristiano",
                "cristiano@astanait.school.kz",
                DEMO_PASSWORD_HASH,
                "10A",
                "+770552525252"
        );

        Session teacherSession = new Session(
                teacher.getId(),
                UserRole.TEACHER
        );
        Session studentSession = new Session(
                student.getId(),
                UserRole.STUDENT
        );

        String subject = teacher.getSpecialization().name();

        schoolFacade.addGrade(
                teacherSession,
                new Grade("grade-1", student.getId(), teacher.getId(),
                        subject, GradeType.HOMEWORK, 80)
        );
        schoolFacade.addGrade(
                teacherSession,
                new Grade("grade-2", student.getId(), teacher.getId(),
                        subject, GradeType.QUIZ, 90)
        );
        schoolFacade.addGrade(
                teacherSession,
                new Grade("grade-3", student.getId(), teacher.getId(),
                        subject, GradeType.EXAM, 100)
        );

        double simpleAverage = schoolFacade.calculateFinalGrade(
                studentSession,
                student.getId(),
                subject,
                new SimpleAverageStrategy()
        );

        double weightedAverage = schoolFacade.calculateFinalGrade(
                studentSession,
                student.getId(),
                subject,
                new WeightedAverageStrategy()
        );

        System.out.println("=== EduCore integrated demo ===");
        System.out.println("Registered student: " + student.getName());
        System.out.println("Subject: " + subject);
        System.out.printf("Simple average: %.1f%n", simpleAverage);
        System.out.printf("Weighted average: %.1f%n", weightedAverage);

        System.out.println("\nNotifications:");
        List<Notification> notifications =
                schoolFacade.getNotifications(studentSession);

        for (Notification notification : notifications) {
            System.out.println("- " + notification.getMessage());
        }

        System.out.println("\nAccess control:");
        try {
            schoolFacade.getGrades(studentSession, anotherStudent.getId());
            System.out.println("Unexpected: access was allowed.");
        } catch (SecurityException exception) {
            System.out.println("Student cannot view another student's grades.");
        }
    }

    private static Administrator createDemoAdministrator(
            UserRepository userRepository
    ) {
        AdministratorFactory administratorFactory =
                new AdministratorFactory();

        Administrator administrator =
                administratorFactory.createAdministrator(
                        "admin-1",
                        "School Administrator"
                );

        userRepository.saveUser(administrator);
        userRepository.saveAccount(
                administratorFactory.createAccount(
                        administrator.getId(),
                        "admin@astanait.school.kz",
                        DEMO_PASSWORD_HASH
                )
        );

        return administrator;
    }
}
package com.educore.academic;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AcademicModuleTest {
    private GradeService service;
    private Session teacherSession;
    private Session alexSession;
    private Session miaSession;

    @BeforeEach
    void setUp() {
        UserRepository users = new InMemoryUserRepository();

        users.saveUser(new StudentFactory().createStudent(
                "s1", "Alex", "SE-2526", "+77001234567"
        ));

        users.saveUser(new StudentFactory().createStudent(
                "s2", "Mia", "SE-2526", "+77007654321"
        ));

        users.saveUser(new TeacherFactory().createTeacher(
                "t1", "Kim", TeacherSpecialization.MATH
        ));

        service = new GradeService(
                users,
                new InMemoryGradeRepository(),
                new InMemoryNotificationRepository()
        );

        teacherSession = new Session("t1", UserRole.TEACHER);
        alexSession = new Session("s1", UserRole.STUDENT);
        miaSession = new Session("s2", UserRole.STUDENT);
    }

    private Grade grade(String id, String studentId,
                        GradeType type, int score) {
        return new Grade(id, studentId, "t1", "MATH", type, score);
    }

    @Test
    void testGradeIsSaved() {
        service.addGrade(teacherSession,
                grade("g1", "s1", GradeType.HOMEWORK, 90));

        assertEquals(1, service.getGrades(alexSession, "s1").size());
        assertEquals(90, service.getGrades(alexSession, "s1")
                .get(0).getScore());
    }

    @Test
    void testObserverCreatesNotification() {
        service.addGrade(teacherSession,
                grade("g1", "s1", GradeType.EXAM, 95));

        assertEquals(1, service.getNotifications(alexSession).size());
        assertEquals("g1", service.getNotifications(alexSession)
                .get(0).getGradeId());
    }

    @Test
    void testOtherStudentDoesNotReceiveNotification() {
        service.addGrade(teacherSession,
                grade("g1", "s1", GradeType.EXAM, 95));

        assertTrue(service.getNotifications(miaSession).isEmpty());
    }

    @Test
    void testMultipleNotifications() {
        service.addGrade(teacherSession,
                grade("g1", "s1", GradeType.HOMEWORK, 80));
        service.addGrade(teacherSession,
                grade("g2", "s1", GradeType.QUIZ, 90));

        assertEquals(2, service.getNotifications(alexSession).size());
    }

    @Test
    void testSimpleAverageStrategy() {
        service.addGrade(teacherSession,
                grade("g1", "s1", GradeType.HOMEWORK, 80));
        service.addGrade(teacherSession,
                grade("g2", "s1", GradeType.QUIZ, 90));
        service.addGrade(teacherSession,
                grade("g3", "s1", GradeType.EXAM, 100));

        GradeCalculator calculator =
                new GradeCalculator(new SimpleAverageStrategy());

        double result = service.calculateFinalGrade(
                alexSession, "s1", "MATH", calculator
        );

        assertEquals(90.0, result, 0.001);
    }

    @Test
    void testWeightedAverageStrategy() {
        service.addGrade(teacherSession,
                grade("g1", "s1", GradeType.HOMEWORK, 80));
        service.addGrade(teacherSession,
                grade("g2", "s1", GradeType.QUIZ, 90));
        service.addGrade(teacherSession,
                grade("g3", "s1", GradeType.EXAM, 100));

        GradeCalculator calculator =
                new GradeCalculator(new WeightedAverageStrategy());

        double result = service.calculateFinalGrade(
                alexSession, "s1", "MATH", calculator
        );

        assertEquals(93.0, result, 0.001);
    }

    @Test
    void testStrategySwitching() {
        service.addGrade(teacherSession,
                grade("g1", "s1", GradeType.HOMEWORK, 80));
        service.addGrade(teacherSession,
                grade("g2", "s1", GradeType.EXAM, 100));

        GradeCalculator calculator =
                new GradeCalculator(new SimpleAverageStrategy());

        double simple = service.calculateFinalGrade(
                alexSession, "s1", "MATH", calculator
        );

        calculator.setStrategy(new WeightedAverageStrategy());

        double weighted = service.calculateFinalGrade(
                alexSession, "s1", "MATH", calculator
        );

        assertEquals(90.0, simple, 0.001);
        assertEquals(94.2857, weighted, 0.001);
    }

    @Test
    void testStudentCannotAddGrade() {
        assertThrows(SecurityException.class, () -> {
            service.addGrade(alexSession,
                    grade("g1", "s1", GradeType.EXAM, 90));
        });
    }

    @Test
    void testStudentCannotReadOtherGrades() {
        assertThrows(SecurityException.class, () -> {
            service.getGrades(alexSession, "s2");
        });
    }

    @Test
    void testInvalidTeacherSubject() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.addGrade(teacherSession,
                    new Grade("g1", "s1", "t1",
                            "PHYSICS", GradeType.EXAM, 90));
        });
    }

    @Test
    void testDuplicateGradeId() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.addGrade(teacherSession,
                    grade("g1", "s1", GradeType.HOMEWORK, 80));
            service.addGrade(teacherSession,
                    grade("g1", "s1", GradeType.EXAM, 90));
        });
    }

    @Test
    void testInactiveSession() {
        assertThrows(SecurityException.class, () -> {
            alexSession.logout();
            service.getNotifications(alexSession);
        });
    }

    @Test
    void testUnknownStudent() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.addGrade(teacherSession,
                    grade("g1", "unknown", GradeType.EXAM, 90));
        });
    }
}
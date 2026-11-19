package com.educore.facade;

import com.educore.academic.model.Grade;
import com.educore.academic.model.GradeType;
import com.educore.academic.model.Notification;
import com.educore.academic.repository.InMemoryGradeRepository;
import com.educore.academic.repository.InMemoryNotificationRepository;
import com.educore.academic.service.GradeService;
import com.educore.academic.strategy.HighestGradeStrategy;
import com.educore.academic.strategy.SimpleAverageStrategy;
import com.educore.access.AccessGuard;
import com.educore.school.model.SchoolClass;
import com.educore.school.repository.InMemoryClassRepository;
import com.educore.school.service.ClassService;
import com.educore.user.auth.AuthService;
import com.educore.user.auth.PasswordHasher;
import com.educore.user.auth.Pbkdf2PasswordHasher;
import com.educore.user.auth.RegistrationService;
import com.educore.user.auth.Session;
import com.educore.user.model.Student;
import com.educore.user.model.Teacher;
import com.educore.user.model.TeacherSpecialization;
import com.educore.user.repository.InMemoryUserRepository;
import com.educore.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SchoolFacadeIntegrationTest {
    private SchoolFacade facade;
    private Session admin;
    private Session teacher;
    private Session alex;
    private Student alexUser;
    private Student miaUser;
    private Teacher teacherUser;

    @BeforeEach
    void setUp() {
        UserRepository users = new InMemoryUserRepository();
        PasswordHasher hasher = new Pbkdf2PasswordHasher();
        AuthService auth = new AuthService(users, hasher);
        RegistrationService registration = new RegistrationService(users, hasher);
        facade = new SchoolFacade(users,
                new GradeService(users, new InMemoryGradeRepository(), new InMemoryNotificationRepository()),
                new AccessGuard(), registration, auth,
                new ClassService(new InMemoryClassRepository(), users));

        facade.registerFirstAdministrator("Root", "root@mail.com", "password1");
        admin = facade.login("root@mail.com", "password1");
        teacherUser = facade.registerTeacher(admin, "Kim", "kim@mail.com", "password1", TeacherSpecialization.MATH);
        alexUser = facade.registerStudent("Alex", "alex@mail.com", "password1", "10A", "+77001234567");
        miaUser = facade.registerStudent("Mia", "mia@mail.com", "password1", "10A", "+77007654321");
        teacher = facade.login("kim@mail.com", "password1");
        alex = facade.login("alex@mail.com", "password1");
    }

    private void putAlexInTeachersClass() {
        SchoolClass math = facade.createClass(admin, "10A-MATH");
        facade.assignTeacherToClass(admin, math.getId(), teacherUser.getId());
        facade.assignStudentToClass(admin, math.getId(), alexUser.getId());
    }

    private Grade grade(String id, String studentId, GradeType type, int score) {
        return new Grade(id, studentId, teacherUser.getId(), "MATH", type, score);
    }

    @Test
    void secondFirstAdministratorIsRejected() {
        assertThrows(IllegalStateException.class,
                () -> facade.registerFirstAdministrator("Other", "other@mail.com", "password1"));
    }

    @Test
    void teacherCannotRegisterTeachersAndStudentsCannotManageClasses() {
        assertThrows(SecurityException.class, () -> facade.registerTeacher(
                teacher, "X", "x@mail.com", "password1", TeacherSpecialization.PHYSICS));
        assertThrows(SecurityException.class, () -> facade.createClass(alex, "10A-MATH"));
        assertThrows(SecurityException.class, () -> facade.createClass(teacher, "10A-MATH"));
    }

    @Test
    void teacherWithoutClassesSeesNoStudentsAndCannotGrade() {
        assertTrue(facade.getStudents(teacher).isEmpty());
        assertThrows(SecurityException.class,
                () -> facade.addGrade(teacher, grade("g1", alexUser.getId(), GradeType.HOMEWORK, 80)));
    }

    @Test
    void teacherSeesAndGradesOnlyStudentsOfOwnClasses() {
        putAlexInTeachersClass();

        List<Student> visible = facade.getStudents(teacher);
        assertEquals(1, visible.size());
        assertEquals(alexUser.getId(), visible.get(0).getId());
        assertEquals(2, facade.getStudents(admin).size());

        assertDoesNotThrow(() -> facade.addGrade(teacher, grade("g1", alexUser.getId(), GradeType.HOMEWORK, 80)));
        assertThrows(SecurityException.class,
                () -> facade.addGrade(teacher, grade("g2", miaUser.getId(), GradeType.HOMEWORK, 80)));
    }

    @Test
    void studentCannotAddGrades() {
        putAlexInTeachersClass();
        assertThrows(SecurityException.class,
                () -> facade.addGrade(alex, grade("g1", alexUser.getId(), GradeType.HOMEWORK, 80)));
    }

    @Test
    void addingAndUpdatingAGradeCreatesNotificationsForTheStudent() {
        putAlexInTeachersClass();
        facade.addGrade(teacher, grade("g1", alexUser.getId(), GradeType.HOMEWORK, 80));
        facade.updateGrade(teacher, "g1", 90);

        List<Notification> notifications = facade.getNotifications(alex);
        assertEquals(2, notifications.size());
        assertEquals(90, facade.getGrades(alex, alexUser.getId()).get(0).getScore());
    }

    @Test
    void studentSeesOnlyOwnGrades() {
        putAlexInTeachersClass();
        assertThrows(SecurityException.class, () -> facade.getGrades(alex, miaUser.getId()));
    }

    @Test
    void finalGradeUsesTheSelectedStrategy() {
        putAlexInTeachersClass();
        facade.addGrade(teacher, grade("g1", alexUser.getId(), GradeType.HOMEWORK, 60));
        facade.addGrade(teacher, grade("g2", alexUser.getId(), GradeType.EXAM, 100));

        assertEquals(80.0, facade.calculateFinalGrade(teacher, alexUser.getId(), "MATH", new SimpleAverageStrategy()), 0.001);
        assertEquals(100.0, facade.calculateFinalGrade(teacher, alexUser.getId(), "MATH", new HighestGradeStrategy()), 0.001);
    }
}

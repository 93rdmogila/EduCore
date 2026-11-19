package com.educore.access;

import com.educore.user.auth.AuthService;
import com.educore.user.auth.PasswordHasher;
import com.educore.user.auth.Pbkdf2PasswordHasher;
import com.educore.user.auth.Session;
import com.educore.user.factory.AdministratorFactory;
import com.educore.user.factory.StudentFactory;
import com.educore.user.factory.TeacherFactory;
import com.educore.user.model.TeacherSpecialization;
import com.educore.user.repository.InMemoryUserRepository;
import com.educore.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccessDecoratorTest {
    private AccessFactory factory;
    private AccessGuard guard;
    private Session student;
    private Session teacher;
    private Session admin;

    @BeforeEach
    void setUp() {
        UserRepository users = new InMemoryUserRepository();
        PasswordHasher hasher = new Pbkdf2PasswordHasher();
        String hash = hasher.hash("secret123");
        StudentFactory sf = new StudentFactory();
        TeacherFactory tf = new TeacherFactory();
        AdministratorFactory af = new AdministratorFactory();
        users.saveUser(sf.createStudent("s1", "Alex", "10A", "+77001234567"));
        users.saveAccount(sf.createAccount("s1", "alex@mail.com", hash));
        users.saveUser(tf.createTeacher("t1", "Kim", TeacherSpecialization.MATH));
        users.saveAccount(tf.createAccount("t1", "kim@mail.com", hash));
        users.saveUser(af.createAdministrator("a1", "Root"));
        users.saveAccount(af.createAccount("a1", "root@mail.com", hash));
        AuthService auth = new AuthService(users, hasher);
        student = auth.login("alex@mail.com", "secret123");
        teacher = auth.login("kim@mail.com", "secret123");
        admin = auth.login("root@mail.com", "secret123");
        factory = new AccessFactory();
        guard = new AccessGuard(factory);
    }

    @Test
    void basicAccessGrantsNothing() {
        for (Permission p : Permission.values()) {
            assertFalse(new BasicAccess().hasPermission(p));
        }
    }

    @Test
    void studentCanOnlyViewOwnGradesAndNotifications() {
        Access access = factory.createFor(student);
        assertTrue(access.hasPermission(Permission.VIEW_OWN_GRADES));
        assertTrue(access.hasPermission(Permission.VIEW_NOTIFICATIONS));
        assertFalse(access.hasPermission(Permission.ADD_GRADE));
        assertFalse(access.hasPermission(Permission.MANAGE_CLASSES));
        assertFalse(access.hasPermission(Permission.REGISTER_USERS));
    }

    @Test
    void teacherCanGradeButNotManageClassesOrRegisterUsers() {
        Access access = factory.createFor(teacher);
        assertTrue(access.hasPermission(Permission.ADD_GRADE));
        assertTrue(access.hasPermission(Permission.UPDATE_GRADE));
        assertTrue(access.hasPermission(Permission.VIEW_ANY_GRADES));
        assertFalse(access.hasPermission(Permission.MANAGE_CLASSES));
        assertFalse(access.hasPermission(Permission.REGISTER_USERS));
    }

    @Test
    void administratorCanManageClassesAndRegisterUsersButNotGrade() {
        Access access = factory.createFor(admin);
        assertTrue(access.hasPermission(Permission.MANAGE_CLASSES));
        assertTrue(access.hasPermission(Permission.REGISTER_USERS));
        assertFalse(access.hasPermission(Permission.ADD_GRADE));
    }

    @Test
    void guardRejectsMissingPermission() {
        assertThrows(SecurityException.class, () -> guard.requirePermission(student, Permission.ADD_GRADE));
        assertDoesNotThrow(() -> guard.requirePermission(teacher, Permission.ADD_GRADE));
    }

    @Test
    void studentCannotReadAnotherStudentsGrades() {
        assertDoesNotThrow(() -> guard.requireGradeAccess(student, "s1"));
        assertThrows(SecurityException.class, () -> guard.requireGradeAccess(student, "s2"));
        assertDoesNotThrow(() -> guard.requireGradeAccess(teacher, "s2"));
    }
}

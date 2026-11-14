package com.educore.facade;

import com.educore.access.AccessGuard;
import com.educore.access.Permission;
import com.educore.academic.model.Grade;
import com.educore.academic.model.Notification;
import com.educore.academic.service.GradeService;
import com.educore.academic.strategy.GradeCalculationStrategy;
import com.educore.academic.strategy.GradeCalculator;
import com.educore.user.auth.Session;
import com.educore.user.factory.AdministratorFactory;
import com.educore.user.factory.StudentFactory;
import com.educore.user.factory.TeacherFactory;
import com.educore.user.model.Administrator;
import com.educore.user.model.Student;
import com.educore.user.model.Teacher;
import com.educore.user.model.TeacherSpecialization;
import com.educore.user.model.User;
import com.educore.user.model.UserAccount;
import com.educore.user.model.UserRole;
import com.educore.user.repository.UserRepository;
import com.educore.user.util.Validator;

import java.util.List;

public class SchoolFacade {
    private final UserRepository userRepository;
    private final GradeService gradeService;
    private final AccessGuard accessGuard;

    private final StudentFactory studentFactory = new StudentFactory();
    private final TeacherFactory teacherFactory = new TeacherFactory();
    private final AdministratorFactory administratorFactory =
            new AdministratorFactory();

    public SchoolFacade(
            UserRepository userRepository,
            GradeService gradeService,
            AccessGuard accessGuard
    ) {
        if (userRepository == null || gradeService == null || accessGuard == null) {
            throw new IllegalArgumentException(
                    "Facade dependencies cannot be null"
            );
        }

        this.userRepository = userRepository;
        this.gradeService = gradeService;
        this.accessGuard = accessGuard;
    }

    public Student registerStudent(
            Session session,
            String id,
            String name,
            String email,
            String passwordHash,
            String className,
            String parentContact
    ) {
        requireAdministrator(session);
        validateNewAccount(id, email);

        Student student = studentFactory.createStudent(
                id, name, className, parentContact
        );
        UserAccount account = studentFactory.createAccount(
                id, email, passwordHash
        );

        saveUserAndAccount(student, account);
        return student;
    }

    public Teacher registerTeacher(
            Session session,
            String id,
            String name,
            String email,
            String passwordHash,
            TeacherSpecialization specialization
    ) {
        requireAdministrator(session);
        validateNewAccount(id, email);

        Teacher teacher = teacherFactory.createTeacher(
                id, name, specialization
        );
        UserAccount account = teacherFactory.createAccount(
                id, email, passwordHash
        );

        saveUserAndAccount(teacher, account);
        return teacher;
    }

    public Administrator registerAdministrator(
            Session session,
            String id,
            String name,
            String email,
            String passwordHash
    ) {
        requireAdministrator(session);
        validateNewAccount(id, email);

        Administrator administrator =
                administratorFactory.createAdministrator(id, name);
        UserAccount account = administratorFactory.createAccount(
                id, email, passwordHash
        );

        saveUserAndAccount(administrator, account);
        return administrator;
    }

    public void addGrade(Session session, Grade grade) {
        accessGuard.requirePermission(session, Permission.ADD_GRADE);
        gradeService.addGrade(session, grade);
    }

    public List<Grade> getGrades(Session session, String studentId) {
        accessGuard.requireGradeAccess(session, studentId);
        return gradeService.getGrades(session, studentId);
    }

    public List<Notification> getNotifications(Session session) {
        accessGuard.requirePermission(
                session, Permission.VIEW_NOTIFICATIONS
        );
        return gradeService.getNotifications(session);
    }

    public double calculateFinalGrade(
            Session session,
            String studentId,
            String subject,
            GradeCalculationStrategy strategy
    ) {
        accessGuard.requireGradeAccess(session, studentId);

        GradeCalculator calculator = new GradeCalculator(strategy);

        return gradeService.calculateFinalGrade(
                session, studentId, subject, calculator
        );
    }

    private void requireAdministrator(Session session) {
        accessGuard.requirePermission(session, Permission.REGISTER_USERS);

        User currentUser = userRepository.findUserById(session.getUserId())
                .orElseThrow(() -> new SecurityException("User not found"));

        if (session.getRole() != UserRole.ADMINISTRATOR
                || !(currentUser instanceof Administrator)) {
            throw new SecurityException("Administrator session required");
        }
    }

    private void validateNewAccount(String id, String email) {
        Validator.requireNonBlank(id, "User id");
        Validator.requireEmail(email);

        if (userRepository.findUserById(id).isPresent()
                || userRepository.findAccountByUserId(id).isPresent()) {
            throw new IllegalArgumentException("User id already exists: " + id);
        }

        if (userRepository.existsAccountByEmail(email)) {
            throw new IllegalArgumentException(
                    "Email is already registered: " + email
            );
        }
    }

    private void saveUserAndAccount(User user, UserAccount account) {
        userRepository.saveUser(user);
        userRepository.saveAccount(account);
    }
}
package com.educore.facade;

import com.educore.access.AccessGuard;
import com.educore.access.Permission;
import com.educore.academic.model.Grade;
import com.educore.academic.model.Notification;
import com.educore.academic.service.GradeService;
import com.educore.academic.strategy.GradeCalculationStrategy;
import com.educore.academic.strategy.GradeCalculator;
import com.educore.user.auth.AuthService;
import com.educore.user.auth.Session;
import com.educore.user.model.Administrator;
import com.educore.user.model.Student;
import com.educore.user.model.Teacher;
import com.educore.user.model.TeacherSpecialization;
import com.educore.user.model.User;
import com.educore.user.model.UserRole;
import com.educore.user.repository.UserRepository;
import com.educore.user.auth.RegistrationService;
import java.util.List;

public class SchoolFacade {
    private final UserRepository userRepository;
    private final GradeService gradeService;
    private final AccessGuard accessGuard;
	private final RegistrationService registrationService;
	private final AuthService authService;

    public SchoolFacade(
            UserRepository userRepository,
            GradeService gradeService,
            AccessGuard accessGuard,
			RegistrationService registrationService,
			AuthService authService
    ) {
        if (userRepository == null || gradeService == null || accessGuard == null || registrationService == null || authService == null) {
            throw new IllegalArgumentException(
                    "Facade dependencies cannot be null"
            );
        }

        this.userRepository = userRepository;
        this.gradeService = gradeService;
        this.accessGuard = accessGuard;
		this.registrationService = registrationService;
		this.authService = authService;
    }

    public Student registerStudent(
            String name,
            String email,
            String password,
            String className,
            String parentContact
    ) {
		return registrationService.registerStudent(name, email, password, className, parentContact);
    }

    public Teacher registerTeacher(
            Session session,
            String name,
            String email,
            String password,
            TeacherSpecialization specialization
    ) {
        requireAdministrator(session);
		return registrationService.registerTeacher(name, email, password, specialization);
    }

    public Administrator registerAdministrator(
            Session session,
            String name,
            String email,
            String password
    ) {
        accessGuard.requirePermission(session, Permission.REGISTER_USERS);
		return registrationService.registerAdministrator(session, name, email, password);
    }
	public Administrator registerFirstAdministrator(String name, String email, String password){
		return registrationService.registerFirstAdministrator(name, email, password);
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

	public Session login(String email, String password){
		return authService.login(email, password);
	}
	public void logout(Session session){
		authService.logout(session);
	}
}
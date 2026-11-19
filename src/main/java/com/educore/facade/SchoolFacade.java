package com.educore.facade;

import com.educore.access.AccessGuard;
import com.educore.access.Permission;
import com.educore.academic.model.Grade;
import com.educore.academic.model.Notification;
import com.educore.academic.service.GradeService;
import com.educore.academic.strategy.GradeCalculationStrategy;
import com.educore.academic.strategy.GradeCalculator;
import com.educore.school.model.SchoolClass;
import com.educore.school.service.ClassService;
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
import java.util.ArrayList;
import java.util.List;

public class SchoolFacade {
    private final UserRepository userRepository;
    private final GradeService gradeService;
    private final AccessGuard accessGuard;
	private final RegistrationService registrationService;
	private final AuthService authService;
	private final ClassService classService;

    public SchoolFacade(
            UserRepository userRepository,
            GradeService gradeService,
            AccessGuard accessGuard,
			RegistrationService registrationService,
			AuthService authService,
			ClassService classService
    ) {
        if (userRepository == null || gradeService == null || accessGuard == null || registrationService == null || authService == null
                || classService == null) {
            throw new IllegalArgumentException(
                    "Facade dependencies cannot be null"
            );
        }

        this.userRepository = userRepository;
        this.gradeService = gradeService;
        this.accessGuard = accessGuard;
		this.registrationService = registrationService;
		this.authService = authService;
		this.classService = classService;
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
        requireTeacherTeachesStudent(session, grade.getStudentId());
        gradeService.addGrade(session, grade);
    }

	public void updateGrade(Session session, String gradeId, int newScore){
		accessGuard.requirePermission(session, Permission.UPDATE_GRADE);
		gradeService.updateGrade(session, gradeId, newScore);
	}
    public List<Grade> getGrades(Session session, String studentId) {
        accessGuard.requireGradeAccess(session, studentId);
        requireTeacherTeachesStudent(session, studentId);
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
        requireTeacherTeachesStudent(session, studentId);

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

	public List<Student> getStudents(Session session) {
		accessGuard.requirePermission(session, Permission.VIEW_ANY_GRADES);
		List<Student> all = userRepository.findAllStudents();
		if (session.getRole() != UserRole.TEACHER) {
			return all;   // administrators see everybody
		}
		// a teacher sees only the students of his/her own classes
		List<String> allowed = classService.studentIdsOfTeacher(session.getUserId());
		List<Student> mine = new ArrayList<>();
		for (Student student : all) {
			if (allowed.contains(student.getId())) {
				mine.add(student);
			}
		}
		return mine;
	}

	public List<Teacher> getTeachers(Session session) {
		accessGuard.requirePermission(session, Permission.REGISTER_USERS);
		return userRepository.findAllTeachers();
	}

	public String getTeacherSubject(Session session) {
		accessGuard.requirePermission(session, Permission.ADD_GRADE);
		User user = userRepository.findUserById(session.getUserId())
				.orElseThrow(() -> new SecurityException("User not found"));
		if (!(user instanceof Teacher)) {
			throw new SecurityException("Teacher session required");
		}
		return ((Teacher) user).getSpecialization().name();
	}

	public SchoolClass createClass(Session session, String name) {
		accessGuard.requirePermission(session, Permission.MANAGE_CLASSES);
		return classService.createClass(name);
	}

	public SchoolClass assignTeacherToClass(Session session, String classId, String teacherId) {
		accessGuard.requirePermission(session, Permission.MANAGE_CLASSES);
		return classService.assignTeacher(classId, teacherId);
	}

	public SchoolClass assignStudentToClass(Session session, String classId, String studentId) {
		accessGuard.requirePermission(session, Permission.MANAGE_CLASSES);
		return classService.assignStudent(classId, studentId);
	}

	// administrator: all classes; teacher: only his/her own classes
	public List<SchoolClass> getClasses(Session session) {
		accessGuard.requirePermission(session, Permission.VIEW_ANY_GRADES);
		if (session.getRole() == UserRole.TEACHER) {
			return classService.findByTeacher(session.getUserId());
		}
		return classService.findAll();
	}

	private void requireTeacherTeachesStudent(Session session, String studentId) {
		if (session.getRole() == UserRole.TEACHER
				&& !classService.teacherTeachesStudent(session.getUserId(), studentId)) {
			throw new SecurityException("This student is not in one of your classes.");
		}
	}

	public Session login(String email, String password){
		return authService.login(email, password);
	}
	public void logout(Session session){
		authService.logout(session);
	}
}
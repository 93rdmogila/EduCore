package com.educore.academic.service;

import com.educore.academic.model.Grade;
import com.educore.academic.model.Notification;
import com.educore.academic.observer.GradePublisher;
import com.educore.academic.observer.StudentNotificationObserver;
import com.educore.academic.repository.GradeRepository;
import com.educore.academic.repository.NotificationRepository;
import com.educore.academic.strategy.GradeCalculator;
import com.educore.user.auth.Session;
import com.educore.user.model.*;
import com.educore.user.repository.UserRepository;
import com.educore.user.util.Validator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GradeService {
    private final UserRepository userRepository;
    private final GradeRepository gradeRepository;
    private final NotificationRepository notificationRepository;
    private final GradePublisher gradePublisher = new GradePublisher();
    private final Set<String> subscribedStudents = new HashSet<>();

    public GradeService(UserRepository userRepository,
                        GradeRepository gradeRepository,
                        NotificationRepository notificationRepository) {
        if (userRepository == null || gradeRepository == null
                || notificationRepository == null) {
            throw new IllegalArgumentException("Repositories cannot be null");
        }

        this.userRepository = userRepository;
        this.gradeRepository = gradeRepository;
        this.notificationRepository = notificationRepository;
    }

    public void addGrade(Session session, Grade grade) {
        User user = getCurrentUser(session);

        if (session.getRole() != UserRole.TEACHER
                || !(user instanceof Teacher)) {
            throw new SecurityException("Only teachers can add grades");
        }

        if (grade == null) {
            throw new IllegalArgumentException("Grade cannot be null");
        }

        if (!session.getUserId().equals(grade.getTeacherId())) {
            throw new SecurityException("Teacher ID does not match session");
        }

        Teacher teacher = (Teacher) user;

        if (!teacher.getSpecialization().name().equals(grade.getSubject())) {
            throw new IllegalArgumentException("Invalid teacher subject");
        }

        User student = userRepository.findUserById(grade.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        if (!(student instanceof Student)) {
            throw new IllegalArgumentException("User is not a student");
        }

        gradeRepository.save(grade);

        ensureSubscribed(grade.getStudentId());

        gradePublisher.notifyGradeAdded(grade);
    }

    public List<Grade> getGrades(Session session, String studentId) {
        checkReadAccess(session, studentId);
        return gradeRepository.findByStudentId(studentId);
    }

    public List<Notification> getNotifications(Session session) {
        User user = getCurrentUser(session);

        if (session.getRole() != UserRole.STUDENT
                || !(user instanceof Student)) {
            throw new SecurityException("Only students can view their notifications");
        }

        return notificationRepository.findByStudentId(session.getUserId());
    }

    public double calculateFinalGrade(Session session,
                                      String studentId,
                                      String subject,
                                      GradeCalculator calculator) {
        checkReadAccess(session, studentId);
        Validator.requireNonBlank(subject, "Subject");

        if (calculator == null) {
            throw new IllegalArgumentException("Calculator cannot be null");
        }

        List<Grade> grades = gradeRepository
                .findByStudentIdAndSubject(studentId, subject);

        return calculator.calculateFinalGrade(grades);
    }

    private void checkReadAccess(Session session, String studentId) {
        Validator.requireNonBlank(studentId, "StudentId");
        User user = getCurrentUser(session);

        if (session.getRole() == UserRole.STUDENT) {
            if (!(user instanceof Student)
                    || !session.getUserId().equals(studentId)) {
                throw new SecurityException("Access denied");
            }
        } else if (session.getRole() == UserRole.TEACHER) {
            if (!(user instanceof Teacher)) {
                throw new SecurityException("Access denied");
            }
        } else if (session.getRole() == UserRole.ADMINISTRATOR) {
            if (!(user instanceof Administrator)) {
                throw new SecurityException("Access denied");
            }
        } else {
            throw new SecurityException("Access denied");
        }
    }

    private User getCurrentUser(Session session) {
        if (session == null || !session.isActive()) {
            throw new SecurityException("Active session required");
        }

        return userRepository.findUserById(session.getUserId())
                .orElseThrow(() -> new SecurityException("User not found"));
    }

	public void updateGrade(Session session, String gradeId, int newScore){
		User user = getCurrentUser(session);
		if(session.getRole() != UserRole.TEACHER || !(user instanceof Teacher)){
			throw new SecurityException("Only teachers can update grades.");
		}
		Validator.requireNonBlank(gradeId, "Grade id");
		Grade old = gradeRepository.findById(gradeId).orElseThrow(() -> new IllegalArgumentException("Grade not found."));
		if(!session.getUserId().equals(old.getTeacherId())){
			throw new SecurityException("Only the teacher who set the grade can change it.");
		}
		if(newScore == old.getScore()){
			return;
		}
		Grade updated = new Grade(old.getId(), old.getStudentId(), old.getTeacherId(), old.getSubject(), old.getType(), newScore);
		gradeRepository.update(updated);
		ensureSubscribed(updated.getStudentId());
		gradePublisher.notifyGradeUpdated(old, updated);
	}
	private void ensureSubscribed(String studentId){
		if(subscribedStudents.add(studentId)){
			gradePublisher.subscribe(new StudentNotificationObserver(studentId, notificationRepository));
		}
	}
}

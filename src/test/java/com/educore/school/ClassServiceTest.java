package com.educore.school;

import com.educore.school.model.SchoolClass;
import com.educore.school.repository.ClassRepository;
import com.educore.school.repository.InMemoryClassRepository;
import com.educore.school.repository.JsonClassRepository;
import com.educore.school.service.ClassService;
import com.educore.user.factory.StudentFactory;
import com.educore.user.factory.TeacherFactory;
import com.educore.user.model.TeacherSpecialization;
import com.educore.user.repository.InMemoryUserRepository;
import com.educore.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClassServiceTest {
    private ClassService service;

    @BeforeEach
    void setUp() {
        UserRepository users = new InMemoryUserRepository();
        users.saveUser(new StudentFactory().createStudent("s1", "Alex", "10A", "+77001234567"));
        users.saveUser(new StudentFactory().createStudent("s2", "Mia", "10A", "+77007654321"));
        users.saveUser(new TeacherFactory().createTeacher("t1", "Kim", TeacherSpecialization.MATH));
        users.saveUser(new TeacherFactory().createTeacher("t2", "Lee", TeacherSpecialization.PHYSICS));
        service = new ClassService(new InMemoryClassRepository(), users);
    }

    @Test
    void teacherTeachesOnlyStudentsOfOwnClasses() {
        SchoolClass math = service.createClass("10A-MATH");
        service.assignTeacher(math.getId(), "t1");
        service.assignStudent(math.getId(), "s1");

        assertTrue(service.teacherTeachesStudent("t1", "s1"));
        assertFalse(service.teacherTeachesStudent("t1", "s2"));
        assertFalse(service.teacherTeachesStudent("t2", "s1"));
    }

    @Test
    void studentCanBelongToSeveralClassesSoEverySubjectTeacherCanGradeThem() {
        SchoolClass math = service.createClass("10A-MATH");
        SchoolClass physics = service.createClass("10A-PHYSICS");
        service.assignTeacher(math.getId(), "t1");
        service.assignTeacher(physics.getId(), "t2");
        service.assignStudent(math.getId(), "s1");
        service.assignStudent(physics.getId(), "s1");

        assertTrue(service.teacherTeachesStudent("t1", "s1"));
        assertTrue(service.teacherTeachesStudent("t2", "s1"));
    }

    @Test
    void assigningTheSameStudentTwiceDoesNotDuplicateHim() {
        SchoolClass math = service.createClass("10A-MATH");
        service.assignStudent(math.getId(), "s1");
        SchoolClass again = service.assignStudent(math.getId(), "s1");

        assertEquals(1, again.getStudentIds().size());
    }

    @Test
    void withoutStudentRemovesOnlyThatStudent() {
        SchoolClass base = new SchoolClass("c1", "10A", null, List.of("s1", "s2"));
        SchoolClass changed = base.withoutStudent("s1");

        assertFalse(changed.hasStudent("s1"));
        assertTrue(changed.hasStudent("s2"));
        assertTrue(base.hasStudent("s1"), "the original class must stay unchanged");
        assertSame(base, base.withoutStudent("missing"));
    }

    @Test
    void duplicateClassNameIsRejected() {
        service.createClass("10A-MATH");
        assertThrows(IllegalArgumentException.class, () -> service.createClass("10a-math"));
    }

    @Test
    void onlyTeachersCanBeAssignedAsTeacherAndOnlyStudentsAsStudents() {
        SchoolClass math = service.createClass("10A-MATH");
        assertThrows(IllegalArgumentException.class, () -> service.assignTeacher(math.getId(), "s1"));
        assertThrows(IllegalArgumentException.class, () -> service.assignStudent(math.getId(), "t1"));
        assertThrows(IllegalArgumentException.class, () -> service.assignStudent("missing", "s1"));
    }

    @Test
    void jsonRepositoryKeepsClassesAfterRestart() throws IOException {
        Path dir = Files.createTempDirectory("educore-classes");
        ClassRepository first = new JsonClassRepository(dir);
        first.save(new SchoolClass("c1", "10A-MATH", "t1", List.of("s1", "s2")));
        first.save(new SchoolClass("c2", "10A-PHYSICS", null, List.of()));

        ClassRepository second = new JsonClassRepository(dir);
        assertEquals(2, second.findAll().size());
        SchoolClass loaded = second.findById("c1").orElseThrow();
        assertEquals("t1", loaded.getTeacherId());
        assertEquals(List.of("s1", "s2"), loaded.getStudentIds());
        assertNull(second.findById("c2").orElseThrow().getTeacherId());
    }
}

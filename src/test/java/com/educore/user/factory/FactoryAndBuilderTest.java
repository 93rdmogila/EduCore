package com.educore.user.factory;

import com.educore.user.model.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FactoryAndBuilderTest {

    @Test
    void everyFactoryCreatesAnAccountWithTheMatchingRole() {
        assertEquals(UserRole.STUDENT, new StudentFactory().createAccount("1", "a@mail.com", "hash").getRole());
        assertEquals(UserRole.TEACHER, new TeacherFactory().createAccount("2", "b@mail.com", "hash").getRole());
        assertEquals(UserRole.ADMINISTRATOR, new AdministratorFactory().createAccount("3", "c@mail.com", "hash").getRole());
    }

    @Test
    void factoriesCreateTheirOwnUserTypes() {
        assertTrue(new StudentFactory().createStudent("1", "Alex", "10A", "+77001234567") instanceof Student);
        assertTrue(new TeacherFactory().createTeacher("2", "Kim", TeacherSpecialization.MATH) instanceof Teacher);
        assertTrue(new AdministratorFactory().createAdministrator("3", "Root") instanceof Administrator);
    }

    @Test
    void studentFactoryBuildsTheProfileThroughTheBuilder() {
        Student student = new StudentFactory().createStudent("1", "Alex", "10A", "+77001234567");
        assertEquals("10A", student.getStudentProfile().getClassName());
        assertEquals("+77001234567", student.getStudentProfile().getParentContact());
    }

    @Test
    void factoriesRejectInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> new StudentFactory().createAccount("1", "not-an-email", "hash"));
        assertThrows(IllegalArgumentException.class, () -> new TeacherFactory().createTeacher(" ", "Kim", TeacherSpecialization.MATH));
    }

    @Test
    void builderRequiresClassNameAndParentContact() {
        assertThrows(IllegalArgumentException.class, () -> new StudentProfileBuilder().parentContact("+77001234567").build());
        assertThrows(IllegalArgumentException.class, () -> new StudentProfileBuilder().className("10A").build());
        assertThrows(IllegalArgumentException.class,
                () -> new StudentProfileBuilder().className("10A").parentContact("12345").build());
    }

    @Test
    void builderOptionalFieldsAreOptional() {
        StudentProfile profile = new StudentProfileBuilder().className("10A").parentContact("+77001234567").build();
        assertTrue(profile.getSubjects().isEmpty());
    }

    @Test
    void profileIsImmutableAfterBuild() {
        List<String> subjects = new ArrayList<>(List.of("MATH"));
        StudentProfile profile = new StudentProfileBuilder().className("10A").parentContact("+77001234567")
                .subjects(subjects).address("Astana").dateOfBirth("2010-01-01").build();
        subjects.add("PHYSICS");

        assertEquals(List.of("MATH"), profile.getSubjects());
        assertEquals("Astana", profile.getAddress());
        assertEquals("2010-01-01", profile.getDateOfBirth());
    }
}

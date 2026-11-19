package com.educore.storage;

import com.educore.academic.model.Grade;
import com.educore.academic.model.GradeType;
import com.educore.academic.repository.GradeRepository;
import com.educore.academic.repository.JsonGradeRepository;
import com.educore.user.auth.PasswordHasher;
import com.educore.user.auth.Pbkdf2PasswordHasher;
import com.educore.user.auth.RegistrationService;
import com.educore.user.model.Student;
import com.educore.user.model.TeacherSpecialization;
import com.educore.user.model.User;
import com.educore.user.repository.JsonUserRepository;
import com.educore.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonStorageTest {
    private Path dir;

    @BeforeEach
    void setUp() throws Exception {
        dir = Files.createTempDirectory("educore-test");
    }

    @Test
    void testParserReadsAllValueTypes() {
        Object parsed = Json.parse("{\"a\":[1,2.5,true,null,\"x\"],\"b\":{}}");
        Map<?, ?> map = (Map<?, ?>) parsed;
        List<?> list = (List<?>) map.get("a");
        assertEquals(1L, list.get(0));
        assertEquals(2.5, list.get(1));
        assertEquals(Boolean.TRUE, list.get(2));
        assertNull(list.get(3));
        assertEquals("x", list.get(4));
    }

    @Test
    void testWriterAndParserRoundTripWithSpecialCharacters() {
        Map<String, Object> record = new java.util.LinkedHashMap<>();
        record.put("name", "Alex \"The\" Great\nline2 \\ tab\t");
        Object back = Json.parse(Json.write(List.of(record)));
        Map<?, ?> first = (Map<?, ?>) ((List<?>) back).get(0);
        assertEquals(record.get("name"), first.get("name"));
    }

    @Test
    void testBrokenJsonIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> Json.parse("[ {broken"));
        assertThrows(IllegalArgumentException.class, () -> Json.parse("[1,2] extra"));
    }

    @Test
    void testFileIsCreatedOnFirstRun() {
        new JsonUserRepository(dir);
        assertTrue(Files.exists(dir.resolve("users.json")));
        assertTrue(Files.exists(dir.resolve("accounts.json")));
    }

    @Test
    void testUsersSurviveRestart() {
        PasswordHasher hasher = new Pbkdf2PasswordHasher();
        UserRepository first = new JsonUserRepository(dir);
        RegistrationService reg = new RegistrationService(first, hasher);
        Student alex = reg.registerStudent("Alex", "alex@mail.com", "secret123", "SE-2526", "+77001234567");
        reg.registerTeacher("Kim", "kim@mail.com", "secret123", TeacherSpecialization.MATH);

        UserRepository second = new JsonUserRepository(dir);   // simulates restart
        User loaded = second.findUserById(alex.getId()).orElseThrow();
        assertEquals("Alex", loaded.getName());
        assertEquals("SE-2526", ((Student) loaded).getStudentProfile().getClassName());
        assertEquals(1, second.findAllStudents().size());
        assertEquals(1, second.findAllTeachers().size());
        assertTrue(second.existsAccountByEmail("ALEX@mail.com"));
    }

    @Test
    void testPasswordHashSurvivesRestartAndStillVerifies() {
        PasswordHasher hasher = new Pbkdf2PasswordHasher();
        new RegistrationService(new JsonUserRepository(dir), hasher)
                .registerStudent("Alex", "alex@mail.com", "secret123", "SE-2526", "+77001234567");

        String hash = new JsonUserRepository(dir).findAccountByEmail("alex@mail.com")
                .orElseThrow().getPasswordHash();
        assertTrue(hasher.verify("secret123", hash));
        assertFalse(hasher.verify("wrong", hash));
    }

    @Test
    void testDuplicateEmailIsNotWrittenToFile() {
        PasswordHasher hasher = new Pbkdf2PasswordHasher();
        RegistrationService reg = new RegistrationService(new JsonUserRepository(dir), hasher);
        reg.registerStudent("Alex", "alex@mail.com", "secret123", "SE-2526", "+77001234567");
        assertThrows(IllegalArgumentException.class, () ->
                reg.registerStudent("Bob", "ALEX@mail.com", "secret123", "SE-2526", "+77001234567"));

        assertEquals(1, new JsonUserRepository(dir).findAllStudents().size());
    }

    @Test
    void testGradesSurviveRestartAndUpdateIsSaved() {
        GradeRepository first = new JsonGradeRepository(dir);
        first.save(new Grade("g1", "s1", "t1", "MATH", GradeType.EXAM, 80));
        first.update(new Grade("g1", "s1", "t1", "MATH", GradeType.EXAM, 95));

        GradeRepository second = new JsonGradeRepository(dir);
        assertEquals(1, second.findByStudentId("s1").size());
        assertEquals(95, second.findById("g1").orElseThrow().getScore());
    }

    @Test
    void testCorruptedFileGivesClearError() throws Exception {
        Files.write(dir.resolve("grades.json"), "[ {broken".getBytes(StandardCharsets.UTF_8));
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> new JsonGradeRepository(dir));
        assertTrue(e.getMessage().contains("grades.json"));
    }

    @Test
    void testBackupIsCreatedOnSecondWrite() {
        GradeRepository repo = new JsonGradeRepository(dir);
        repo.save(new Grade("g1", "s1", "t1", "MATH", GradeType.EXAM, 80));
        repo.save(new Grade("g2", "s1", "t1", "MATH", GradeType.QUIZ, 90));
        assertTrue(Files.exists(dir.resolve("grades.json.bak")));
    }
}
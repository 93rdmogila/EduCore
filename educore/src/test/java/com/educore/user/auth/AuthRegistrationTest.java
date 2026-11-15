package com.educore.user.auth;

import com.educore.user.model.UserRole;
import com.educore.user.repository.InMemoryUserRepository;
import com.educore.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthRegistrationTest {

    private UserRepository repo;
    private RegistrationService reg;
    private AuthService auth;
    private PasswordHasher hasher;

    @BeforeEach
    void setUp() {
        repo = new InMemoryUserRepository();
        hasher = new Pbkdf2PasswordHasher();
        reg = new RegistrationService(repo, hasher);
        auth = new AuthService(repo, hasher);
    }

    @Test
    void testDuplicateEmailDifferentCase() {
        reg.registerStudent("Alex", "alex@mail.com", "secret123", "SE-2526", "+77001234567");
        
        assertThrows(IllegalArgumentException.class, () -> {
            reg.registerStudent("Alex2", "ALEX@mail.com", "secret456", "SE-2526", "+77001234567");
        });
    }
    @Test
    void testPasswordTooShort() {
        assertThrows(IllegalArgumentException.class, () -> {
            reg.registerStudent("Alex", "alex@mail.com", "short", "SE-2526", "+77001234567");
        });
    }

    @Test
    void testInvalidEmailFormat() {
        assertThrows(IllegalArgumentException.class, () -> {
            reg.registerStudent("Alex", "not-an-email", "secret123", "SE-2526", "+77001234567");
        });
    }
    @Test
    void testPasswordNotStoredInPlainText() {
        String rawPassword = "secret123";
        reg.registerStudent("Alex", "alex@mail.com", rawPassword, "SE-2526", "+77001234567");
        
        String storedHash = repo.findAccountByEmail("alex@mail.com")
                .orElseThrow().getPasswordHash();
        
        assertNotEquals(rawPassword, storedHash);
        assertFalse(storedHash.contains(rawPassword));
    }

    @Test
    void testSamePasswordGivesDifferentHashes() {
        String rawPassword = "secret123";
        String hash1 = hasher.hash(rawPassword);
        String hash2 = hasher.hash(rawPassword);
        
        assertNotEquals(hash1, hash2);
    }
    @Test
    void testUnknownEmailAndWrongPasswordSameError() {
        reg.registerStudent("Alex", "alex@mail.com", "secret123", "SE-2526", "+77001234567");
        AuthenticationException wrongPassEx = assertThrows(AuthenticationException.class, () -> {
            auth.login("alex@mail.com", "wrong-pass");
        });
        AuthenticationException unknownEmailEx = assertThrows(AuthenticationException.class, () -> {
            auth.login("nobody@mail.com", "secret123");
        });
        assertEquals(wrongPassEx.getMessage(), unknownEmailEx.getMessage());
    }
    @Test
    void testLoginAfterLogout() {
        reg.registerStudent("Alex", "alex@mail.com", "secret123", "SE-2526", "+77001234567");
        Session session = auth.login("alex@mail.com", "secret123");
        assertTrue(session.isActive());
        auth.logout(session);
        assertFalse(session.isActive());
    }
    @Test
    void testSecondFirstAdminThrowsException() {
        reg.registerFirstAdministrator("SuperAdmin", "admin@mail.com", "adminPassword123");
        assertThrows(IllegalStateException.class, () -> {
            reg.registerFirstAdministrator("Hacker", "hacker@mail.com", "hackerPassword123");
        });
    }

    @Test
    void testStudentSessionCannotCreateAdmin() {
        reg.registerStudent("Alex", "alex@mail.com", "secret123", "SE-2526", "+77001234567");
        Session studentSession = auth.login("alex@mail.com", "secret123");
        assertThrows(SecurityException.class, () -> {
            reg.registerAdministrator(studentSession, "NewAdmin", "new@mail.com", "password123");
        });
        Session fakeAdminSession = new Session(studentSession.getUserId(), UserRole.ADMINISTRATOR);
        assertThrows(SecurityException.class, () -> {
            reg.registerAdministrator(fakeAdminSession, "NewAdmin2", "new2@mail.com", "password123");
        });
    }
    @Test
    void testGarbageHashInVerifyReturnsFalse() {
        assertFalse(hasher.verify("secret123", "garbage-string"));
        assertFalse(hasher.verify("secret123", "pbkdf2$120000$invalid_base64$invalid_base64"));
        assertFalse(hasher.verify("secret123", null));
    }
}
package com.educore.user.repository;

import com.educore.user.model.StudentProfile;
import com.educore.user.model.User;
import com.educore.user.model.UserAccount;

import java.util.Optional;

public interface UserRepository {
    void saveUser(User user);
    void saveAccount(UserAccount account);

    Optional<User> findUserById(String id);
    Optional<UserAccount> findAccountByEmail(String email);
    Optional<UserAccount> findAccountByUserId(String userId);
    
    boolean existsAccountByEmail(String email);
    
    void updateStudentProfile(String userId, StudentProfile newProfile);
}
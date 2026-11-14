package com.educore.user.factory;

import com.educore.user.model.User;
import com.educore.user.model.UserAccount;

public interface UserFactory {
	UserAccount createAccount(String userId, String email, String passwordHash);
}

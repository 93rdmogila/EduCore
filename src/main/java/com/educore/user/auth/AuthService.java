package com.educore.user.auth;

import java.util.Optional;

import com.educore.user.model.UserAccount;
import com.educore.user.repository.UserRepository;

public class AuthService {
	private static final String INVALID = "Invalid email or password.";

	private final UserRepository repository;
	private final PasswordHasher hasher;
	private final String dummyHash;

	public AuthService(UserRepository repository, PasswordHasher hasher){
		this.repository = repository;
		this.hasher = hasher;
		this.dummyHash = hasher.hash("dummy-password-for-timing");
	}

	public Session login(String email, String password){
		if(email == null || password == null){
			throw new AuthenticationException(INVALID);
		}
		Optional<UserAccount> found = repository.findAccountByEmail(email.trim());
		String hashToCheck = found.isPresent() ? found.get().getPasswordHash() : dummyHash;
		boolean passwordOk = hasher.verify(password, hashToCheck);
		if(!found.isPresent() || !passwordOk){
			throw new AuthenticationException(INVALID);
		}
		UserAccount account = found.get();
		if(repository.findUserById(account.getUserId()).isEmpty()){
			throw new AuthenticationException(INVALID);
		}
		return new Session(account.getUserId(), account.getRole());
	}

	public void logout(Session session){
		if(session != null){
			session.logout();
		}
	}
}

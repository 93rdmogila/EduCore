package com.educore.user.auth;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class Pbkdf2PasswordHasher implements PasswordHasher{
	private static final int ITERATIONS = 120_000;
	private static final int SALT_BYTES = 16;
	private static final int KEY_BITS = 256;
	private final SecureRandom random = new SecureRandom();

	private byte[] derive(String password, byte[] salt, int iterations){
		try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, KEY_BITS);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Password hashing failed", e);
        }
	}

	private String base64(byte[] b){
		return Base64.getEncoder().encodeToString(b);
	}
	@Override 
	public String hash(String rawPassword){
		if(rawPassword == null){
			throw new IllegalArgumentException("Password cannot be null");
		}
		byte[] salt = new byte[SALT_BYTES];
		random.nextBytes(salt);
		byte[] hash = derive(rawPassword, salt, ITERATIONS);
		return "pbkdf2$" + ITERATIONS + "$" + base64(salt) + "$" + base64(hash);


	}
	@Override 
	public boolean verify(String rawPassword, String storedHash){
		if(rawPassword == null || storedHash == null){
			return false;
		}
		String[] parts = storedHash.split("\\$");
		if(parts.length != 4){
			return false;
		}
		if(!"pbkdf2".equals(parts[0])){
			return false;
		}
		int iterations;
		try{
			iterations = Integer.parseInt(parts[1]);
		}catch(NumberFormatException e){
			return false;
		}
		try{
			byte[] salt = Base64.getDecoder().decode(parts[2]);
			byte[] expected = Base64.getDecoder().decode(parts[3]);
			byte[] actual = derive(rawPassword, salt, iterations);
			return MessageDigest.isEqual(expected, actual);
		}catch(IllegalArgumentException e){
			return false;
		}
		
	}

}

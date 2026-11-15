package com.educore;

import com.educore.user.auth.PasswordHasher;
import com.educore.user.auth.Pbkdf2PasswordHasher;


import java.util.List;

public class Main {

    public static void main(String[] args) {
		PasswordHasher h = new Pbkdf2PasswordHasher();
		String a = h.hash("secret123"), b = h.hash("secret123");
		System.out.println(a);
		System.out.println(a.equals(b));                 // ожидаем false (разная соль)
		System.out.println(h.verify("secret123", a));    // true
		System.out.println(h.verify("wrong", a));        // false
		System.out.println(h.verify("secret123", "мусор")); // false, без исключения
	}
}
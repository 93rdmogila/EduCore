package com.educore.user.model;

import com.educore.user.util.Validator;

public class Administrator implements User{
	private final String id;
	private final String name;

	public Administrator(String id, String name){
		Validator.requireNonBlank(id, "Id");
		Validator.requireNonBlank(name, "Name");
		this.id = id;
		this.name = name;
	}

	@Override 
	public String getId(){
		return id;
	}
	@Override 
	public String getName(){
		return name;
	}
}

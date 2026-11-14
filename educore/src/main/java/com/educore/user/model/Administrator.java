package com.educore.user.model;

public class Administrator implements User{
	private String id;
	private String name;

	public Administrator(String id, String name){
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

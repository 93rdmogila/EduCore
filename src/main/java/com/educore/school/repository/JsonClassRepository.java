package com.educore.school.repository;

import com.educore.school.model.SchoolClass;
import com.educore.storage.JsonFile;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JsonClassRepository implements ClassRepository{
	private final InMemoryClassRepository memory = new InMemoryClassRepository();
	private final JsonFile file;

	@SuppressWarnings("unchecked")
	public JsonClassRepository(Path directory){
		this.file = new JsonFile(directory.resolve("classes.json"));
		for(Map<String, Object> r : file.readAll()){
			List<String> studentIds = new ArrayList<>();
			Object raw = r.get("studentIds");
			if(raw instanceof List){
				for(Object s : (List<Object>) raw){
					studentIds.add(String.valueOf(s));
				}
			}
			memory.save(new SchoolClass(JsonFile.requireString(r, "id"), 
				JsonFile.requireString(r, "name"), 
				JsonFile.optionalString(r, "teacherId"),
				studentIds));
		}
	}
	
	private void persist(){
		List<Map<String, Object>> records = new ArrayList<>();
		for(SchoolClass schoolClass : memory.findAll()){
			Map<String, Object> r = new LinkedHashMap<>();
			r.put("id", schoolClass.getId());
			r.put("name", schoolClass.getName());
			r.put("teacherId", schoolClass.getTeacherId());
			r.put("studentIds", schoolClass.getStudentIds());
			records.add(r);
		}
		file.writeAll(records);
	}

	@Override 
	public void save(SchoolClass schoolClass){
		memory.save(schoolClass);
		persist();
	}

	@Override 
	public Optional<SchoolClass> findById(String id){
		return memory.findById(id);
	}
	@Override 
	public Optional<SchoolClass> findByName(String name){
		return memory.findByName(name);
	}
	@Override 
	public Optional<SchoolClass> findByStudentId(String studentId){
		return memory.findByStudentId(studentId);
	}
	@Override 
	public List<SchoolClass> findByTeacherId(String teacherId){
		return memory.findByTeacherId(teacherId);
	}
	@Override 
	public List<SchoolClass> findAll(){
		return memory.findAll();
	}
}

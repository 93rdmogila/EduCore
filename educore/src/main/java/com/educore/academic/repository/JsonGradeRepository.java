package com.educore.academic.repository;

import com.educore.academic.model.Grade;
import com.educore.academic.model.GradeType;
import com.educore.storage.JsonFile;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Stores grades in grades.json (loaded on start, written after every change).
public class JsonGradeRepository implements GradeRepository {
	private final JsonFile file;
	private final List<Grade> grades = new ArrayList<>();

	public JsonGradeRepository(Path directory){
		this.file = new JsonFile(directory.resolve("grades.json"));
		for(Map<String, Object> r : file.readAll()){
			grades.add(new Grade(
					JsonFile.requireString(r, "id"),
					JsonFile.requireString(r, "studentId"),
					JsonFile.requireString(r, "teacherId"),
					JsonFile.requireString(r, "subject"),
					GradeType.valueOf(JsonFile.requireString(r, "type")),
					(int) JsonFile.requireNumber(r, "score")));
		}
	}

	private void persist(){
		List<Map<String, Object>> records = new ArrayList<>();
		for(Grade g : grades){
			Map<String, Object> r = new LinkedHashMap<>();
			r.put("id", g.getId());
			r.put("studentId", g.getStudentId());
			r.put("teacherId", g.getTeacherId());
			r.put("subject", g.getSubject());
			r.put("type", g.getType().name());
			r.put("score", g.getScore());
			records.add(r);
		}
		file.writeAll(records);
	}

	@Override
	public void save(Grade grade){
		if(grade == null){
			throw new IllegalArgumentException("Grade cannot be null");
		}
		if(findById(grade.getId()).isPresent()){
			throw new IllegalArgumentException("Grade ID already exists");
		}
		grades.add(grade);
		persist();
	}

	@Override
	public List<Grade> findByStudentId(String studentId){
		List<Grade> result = new ArrayList<>();
		for(Grade g : grades){
			if(g.getStudentId().equals(studentId)){
				result.add(g);
			}
		}
		return result;
	}

	@Override
	public List<Grade> findByStudentIdAndSubject(String studentId, String subject){
		List<Grade> result = new ArrayList<>();
		for(Grade g : grades){
			if(g.getStudentId().equals(studentId) && g.getSubject().equals(subject)){
				result.add(g);
			}
		}
		return result;
	}

	@Override
	public Optional<Grade> findById(String id){
		for(Grade g : grades){
			if(g.getId().equals(id)){
				return Optional.of(g);
			}
		}
		return Optional.empty();
	}

	@Override
	public void update(Grade grade){
		if(grade == null){
			throw new IllegalArgumentException("Grade cannot be null");
		}
		for(int i = 0; i < grades.size(); i++){
			if(grades.get(i).getId().equals(grade.getId())){
				grades.set(i, grade);
				persist();
				return;
			}
		}
		throw new IllegalArgumentException("Grade not found");
	}
}
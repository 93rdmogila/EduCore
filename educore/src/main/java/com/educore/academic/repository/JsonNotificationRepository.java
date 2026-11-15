package com.educore.academic.repository;

import com.educore.academic.model.Notification;
import com.educore.storage.JsonFile;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Stores notifications in notifications.json (loaded on start, written after every change).
public class JsonNotificationRepository implements NotificationRepository {
	private final JsonFile file;
	private final List<Notification> notifications = new ArrayList<>();

	public JsonNotificationRepository(Path directory){
		this.file = new JsonFile(directory.resolve("notifications.json"));
		for(Map<String, Object> r : file.readAll()){
			notifications.add(new Notification(
					JsonFile.requireString(r, "id"),
					JsonFile.requireString(r, "studentId"),
					JsonFile.requireString(r, "gradeId"),
					JsonFile.requireString(r, "message"),
					JsonFile.requireNumber(r, "createdAt")));
		}
	}

	@Override
	public void save(Notification notification){
		if(notification == null){
			throw new IllegalArgumentException("Notification cannot be null");
		}
		notifications.add(notification);
		List<Map<String, Object>> records = new ArrayList<>();
		for(Notification n : notifications){
			Map<String, Object> r = new LinkedHashMap<>();
			r.put("id", n.getId());
			r.put("studentId", n.getStudentId());
			r.put("gradeId", n.getGradeId());
			r.put("message", n.getMessage());
			r.put("createdAt", n.getCreatedAt());
			records.add(r);
		}
		file.writeAll(records);
	}

	@Override
	public List<Notification> findByStudentId(String studentId){
		List<Notification> result = new ArrayList<>();
		for(Notification n : notifications){
			if(n.getStudentId().equals(studentId)){
				result.add(n);
			}
		}
		return result;
	}
}
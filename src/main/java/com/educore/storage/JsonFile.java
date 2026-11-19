package com.educore.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// One JSON file that holds an array of flat records (objects).
// Creates the file on first use, validates content, writes safely (temp file + backup).
public class JsonFile {
	private final Path path;

	public JsonFile(Path path){
		if(path == null){
			throw new IllegalArgumentException("Path cannot be null.");
		}
		this.path = path;
	}

	@SuppressWarnings("unchecked")
	public synchronized List<Map<String, Object>> readAll(){
		try{
			if(path.getParent() != null){
				Files.createDirectories(path.getParent());
			}
			if(!Files.exists(path)){
				Files.write(path, "[]".getBytes(StandardCharsets.UTF_8));
			}
			String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
			if(content.isBlank()){
				return new ArrayList<>();
			}
			Object parsed;
			try{
				parsed = Json.parse(content);
			}catch(IllegalArgumentException e){
				throw new IllegalStateException(path.getFileName() + " is corrupted. " + e.getMessage()
						+ " (a backup may exist: " + path.getFileName() + ".bak)", e);
			}
			if(!(parsed instanceof List)){
				throw new IllegalStateException(path.getFileName() + " must contain a JSON array.");
			}
			List<Map<String, Object>> result = new ArrayList<>();
			for(Object item : (List<Object>) parsed){
				if(!(item instanceof Map)){
					throw new IllegalStateException(path.getFileName() + " must contain an array of objects.");
				}
				result.add((Map<String, Object>) item);
			}
			return result;
		}catch(IOException e){
			throw new IllegalStateException("Cannot read " + path + ": " + e.getMessage(), e);
		}
	}

	public synchronized void writeAll(List<Map<String, Object>> records){
		try{
			if(path.getParent() != null){
				Files.createDirectories(path.getParent());
			}
			Path temp = path.resolveSibling(path.getFileName() + ".tmp");
			Files.write(temp, Json.write(new ArrayList<Object>(records)).getBytes(StandardCharsets.UTF_8));
			if(Files.exists(path)){
				Files.copy(path, path.resolveSibling(path.getFileName() + ".bak"),
						StandardCopyOption.REPLACE_EXISTING);
			}
			try{
				Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
			}catch(AtomicMoveNotSupportedException e){
				Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
			}
		}catch(IOException e){
			throw new IllegalStateException("Cannot write " + path + ": " + e.getMessage(), e);
		}
	}

	// ---------- helpers for reading records ----------

	public static String requireString(Map<String, Object> record, String key){
		Object value = record.get(key);
		if(!(value instanceof String)){
			throw new IllegalStateException("Missing or invalid field '" + key + "' in " + record);
		}
		return (String) value;
	}

	public static String optionalString(Map<String, Object> record, String key){
		Object value = record.get(key);
		return value instanceof String ? (String) value : null;
	}

	public static long requireNumber(Map<String, Object> record, String key){
		Object value = record.get(key);
		if(!(value instanceof Number)){
			throw new IllegalStateException("Missing or invalid field '" + key + "' in " + record);
		}
		return ((Number) value).longValue();
	}
}

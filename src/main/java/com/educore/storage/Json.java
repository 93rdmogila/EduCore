package com.educore.storage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Minimal JSON reader/writer (no external libraries).
// parse: object -> Map<String,Object>, array -> List<Object>, string -> String,
//        integer -> Long, decimal -> Double, true/false -> Boolean, null -> null.
public final class Json {
	private final String text;
	private int pos = 0;

	private Json(String text){
		this.text = text;
	}

	public static Object parse(String text){
		if(text == null){
			throw new IllegalArgumentException("JSON text cannot be null.");
		}
		Json parser = new Json(text);
		parser.skipWhitespace();
		Object value = parser.readValue();
		parser.skipWhitespace();
		if(parser.pos != text.length()){
			throw parser.error("Unexpected data after JSON value");
		}
		return value;
	}

	private IllegalArgumentException error(String message){
		return new IllegalArgumentException("Invalid JSON at position " + pos + ": " + message);
	}

	private void skipWhitespace(){
		while(pos < text.length() && Character.isWhitespace(text.charAt(pos))){
			pos++;
		}
	}

	private char peek(){
		if(pos >= text.length()){
			throw error("Unexpected end of text");
		}
		return text.charAt(pos);
	}

	private void expect(char c){
		if(peek() != c){
			throw error("Expected '" + c + "' but found '" + peek() + "'");
		}
		pos++;
	}

	private Object readValue(){
		char c = peek();
		if(c == '{'){
			return readObject();
		}
		if(c == '['){
			return readArray();
		}
		if(c == '"'){
			return readString();
		}
		if(text.startsWith("true", pos)){
			pos += 4;
			return Boolean.TRUE;
		}
		if(text.startsWith("false", pos)){
			pos += 5;
			return Boolean.FALSE;
		}
		if(text.startsWith("null", pos)){
			pos += 4;
			return null;
		}
		if(c == '-' || Character.isDigit(c)){
			return readNumber();
		}
		throw error("Unexpected character '" + c + "'");
	}

	private Map<String, Object> readObject(){
		Map<String, Object> result = new LinkedHashMap<>();
		expect('{');
		skipWhitespace();
		if(peek() == '}'){
			pos++;
			return result;
		}
		while(true){
			skipWhitespace();
			String key = readString();
			skipWhitespace();
			expect(':');
			skipWhitespace();
			result.put(key, readValue());
			skipWhitespace();
			if(peek() == ','){
				pos++;
			}else{
				expect('}');
				return result;
			}
		}
	}

	private List<Object> readArray(){
		List<Object> result = new ArrayList<>();
		expect('[');
		skipWhitespace();
		if(peek() == ']'){
			pos++;
			return result;
		}
		while(true){
			skipWhitespace();
			result.add(readValue());
			skipWhitespace();
			if(peek() == ','){
				pos++;
			}else{
				expect(']');
				return result;
			}
		}
	}

	private String readString(){
		expect('"');
		StringBuilder sb = new StringBuilder();
		while(true){
			char c = peek();
			pos++;
			if(c == '"'){
				return sb.toString();
			}
			if(c != '\\'){
				sb.append(c);
				continue;
			}
			char e = peek();
			pos++;
			switch(e){
				case '"': sb.append('"'); break;
				case '\\': sb.append('\\'); break;
				case '/': sb.append('/'); break;
				case 'b': sb.append('\b'); break;
				case 'f': sb.append('\f'); break;
				case 'n': sb.append('\n'); break;
				case 'r': sb.append('\r'); break;
				case 't': sb.append('\t'); break;
				case 'u':
					if(pos + 4 > text.length()){
						throw error("Bad unicode escape");
					}
					try{
						sb.append((char) Integer.parseInt(text.substring(pos, pos + 4), 16));
					}catch(NumberFormatException ex){
						throw error("Bad unicode escape");
					}
					pos += 4;
					break;
				default:
					throw error("Bad escape '\\" + e + "'");
			}
		}
	}

	private Object readNumber(){
		int start = pos;
		if(peek() == '-'){
			pos++;
		}
		while(pos < text.length() && "0123456789.eE+-".indexOf(text.charAt(pos)) >= 0){
			pos++;
		}
		String number = text.substring(start, pos);
		try{
			if(number.contains(".") || number.contains("e") || number.contains("E")){
				return Double.valueOf(number);
			}
			return Long.valueOf(number);
		}catch(NumberFormatException e){
			throw error("Bad number '" + number + "'");
		}
	}

	// ---------- writer ----------

	public static String write(Object value){
		StringBuilder sb = new StringBuilder();
		writeValue(sb, value, 0);
		return sb.toString();
	}

	private static void indent(StringBuilder sb, int level){
		for(int i = 0; i < level; i++){
			sb.append("  ");
		}
	}

	@SuppressWarnings("unchecked")
	private static void writeValue(StringBuilder sb, Object value, int level){
		if(value == null){
			sb.append("null");
		}else if(value instanceof String){
			writeString(sb, (String) value);
		}else if(value instanceof Number || value instanceof Boolean){
			sb.append(value.toString());
		}else if(value instanceof Map){
			Map<String, Object> map = (Map<String, Object>) value;
			if(map.isEmpty()){
				sb.append("{}");
				return;
			}
			sb.append("{\n");
			int i = 0;
			for(Map.Entry<String, Object> entry : map.entrySet()){
				indent(sb, level + 1);
				writeString(sb, entry.getKey());
				sb.append(": ");
				writeValue(sb, entry.getValue(), level + 1);
				sb.append(++i < map.size() ? ",\n" : "\n");
			}
			indent(sb, level);
			sb.append("}");
		}else if(value instanceof List){
			List<Object> list = (List<Object>) value;
			if(list.isEmpty()){
				sb.append("[]");
				return;
			}
			sb.append("[\n");
			for(int i = 0; i < list.size(); i++){
				indent(sb, level + 1);
				writeValue(sb, list.get(i), level + 1);
				sb.append(i + 1 < list.size() ? ",\n" : "\n");
			}
			indent(sb, level);
			sb.append("]");
		}else{
			throw new IllegalArgumentException("Cannot write type " + value.getClass().getName());
		}
	}

	private static void writeString(StringBuilder sb, String s){
		sb.append('"');
		for(int i = 0; i < s.length(); i++){
			char c = s.charAt(i);
			switch(c){
				case '"': sb.append("\\\""); break;
				case '\\': sb.append("\\\\"); break;
				case '\n': sb.append("\\n"); break;
				case '\r': sb.append("\\r"); break;
				case '\t': sb.append("\\t"); break;
				default:
					if(c < 0x20){
						sb.append(String.format("\\u%04x", (int) c));
					}else{
						sb.append(c);
					}
			}
		}
		sb.append('"');
	}
}
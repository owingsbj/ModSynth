package com.gallantrealm.javason;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public final class Javason {

	public static void serialize(Object object, Writer writer) throws Exception {
		if (object == null) {
			writer.write("null");
		} else {
			List<Object> objects = new ArrayList<Object>();
			harvestObjects(object, objects);
			writer.write("{\n");
			writer.write("  \"objects\": [");
			for (int i = 0; i < objects.size(); i++) {
				serializeObject(i, objects.get(i), objects, writer);
			}
			writer.write("\n");
			writer.write("  ]\n");
			writer.write("}");
		}
	}

	private static void harvestObjects(Object object, List<Object> objects) throws Exception {
		if (!objects.contains(object)) {
			objects.add(object);
			Field[] fields = object.getClass().getDeclaredFields();
			for (Field field : fields) {
				if (!Modifier.isTransient(field.getModifiers()) && !Modifier.isStatic(field.getModifiers())) {
					if (field.getType().isPrimitive()) {
					} else if (field.getType() == String.class) {
					} else {
						field.setAccessible(true);
						Object o = field.get(object);
						if (o != null) {
							harvestObjects(o, objects);
						}
					}
				}
			}
		}
	}

	public static Object deserialize(Reader reader) throws Exception {
		return null;
	}

	private static void serializeObject(int i, Object object, List<Object> objects, Writer writer) throws Exception {
		if (i > 0) {
			writer.write(",\n");
		} else {
			writer.write("\n");
		}
		writer.write("    {\n");
		writer.write("      \"object_id\": \"" + object.getClass().getSimpleName() + "-" + i + "\",\n");
		writer.write("      \"class\": \"" + object.getClass().getName() + "\"");
		serializeFields(object, writer, objects);
		writer.write("\n    }");
	}

	private static void serializeFields(Object object, Writer writer, List<Object> objects) throws Exception {
		Field[] fields = object.getClass().getDeclaredFields();
		for (Field field : fields) {
			if (!Modifier.isTransient(field.getModifiers()) && !Modifier.isStatic(field.getModifiers())) {
				if (field.getType().isPrimitive()) {
					field.setAccessible(true);
					writer.write(",\n");
					writer.write("      \"" + field.getName() + "\": ");
					if (field.getType().equals(Boolean.TYPE)) {
						writer.write(String.valueOf(field.getBoolean(object)));
					} else if (field.getType().equals(Byte.TYPE)) {
						writer.write(String.valueOf(field.getByte(object)));
					} else if (field.getType().equals(Character.TYPE)) {
						char c = field.getChar(object);
						if (c == '"') {
							writer.write("\"" + field.getName() + "\": \"\\\"\"");
						} else {
							writer.write("\"" + field.getName() + "\": \"" + field.getChar(object) + "\"");
						}
					} else if (field.getType().equals(Short.TYPE)) {
						writer.write(String.valueOf(field.getShort(object)));
					} else if (field.getType().equals(Integer.TYPE)) {
						writer.write(String.valueOf(field.getInt(object)));
					} else if (field.getType().equals(Long.TYPE)) {
						writer.write(String.valueOf(field.getLong(object)));
					} else if (field.getType().equals(Float.TYPE)) {
						writer.write(String.valueOf(field.getFloat(object)));
					} else if (field.getType().equals(Double.TYPE)) {
						writer.write(String.valueOf(field.getDouble(object)));
					}
				} else if (field.getType() == String.class) {
					field.setAccessible(true);
					writer.write(",\n");
					writer.write("      \"" + field.getName() + "\": \"" + field.get(object) + "\"");
				} else {
					field.setAccessible(true);
					writer.write(",\n");
					Object o = field.get(object);
					if (o == null) {
						writer.write("      \"" + field.getName() + "\": null");
					} else {
						String object_id = o.getClass().getSimpleName() + "-" + objects.indexOf(o);
						writer.write("      \"" + field.getName() + "\": \"" + object_id + "\"");
					}
				}
			}
		}
	}

	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

	private static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
}

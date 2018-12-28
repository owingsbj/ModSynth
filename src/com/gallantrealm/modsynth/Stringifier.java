package com.gallantrealm.modsynth;

public class Stringifier {
	StringBuffer buffer = new StringBuffer();
	int indent = 0;

	public Stringifier add(String name, Object o) {
		if (o != null) {
			addIndent();
			buffer.append(name);
			if (o instanceof Stringifiable) {
				buffer.append(" {\n");
				indent++;
				((Stringifiable) o).stringify(this);
				indent--;
				addIndent();
				buffer.append("}\n");
			} else if (o instanceof double[]) {
				double[] o_array = (double[])o;
				buffer.append(" ").append(o_array.length);
				for (int i = 0; i < o_array.length; i++) {
					buffer.append(" ").append(o_array[i]);
				}
				buffer.append("\n");
			} else {
				buffer.append(" ").append(o).append("\n");
			}
		}
		return this;
	}
	
	private void addIndent() {
		for (int i = 0; i < indent; i++) {
			buffer.append("    ");
		}
	}

	public String toString() {
		return buffer.toString();
	}
}

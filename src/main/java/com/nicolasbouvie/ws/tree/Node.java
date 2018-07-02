package com.nicolasbouvie.ws.tree;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Node implements Serializable {
	private static final long serialVersionUID = 8576890855369986770L;
	private static final String QUOTE = "\"";
	private static final String EQUALS = ":";
	private static final String OPEN = "{";
	private static final String CLOSE = "}";
	private static final String COMMA = ",";

	private String id;
	private Node parent;
	private String text;
	private List<Node> children;
	private File file;
	
	public Node(File file) {
		String id = new String(file.getAbsolutePath().getBytes(Charset.defaultCharset()));
		id = Normalizer.normalize(id, Normalizer.Form.NFD);  
		id = id.replaceAll("^\\p{ASCII}]", "");  
		
		this.id = id;
		this.text = file.getName();
		this.setFile(file);
	}
	
	public String getId() {
		return id;
	}

	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public List<Node> getChildren() {
		return children;
	}

	public void setChildren(List<Node> children) {
		this.children = children;
	}
	
	public String getParentId() {
		return parent == null ? "#" : parent.getId();
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(OPEN);
		sb.append(QUOTE).append("id").append(QUOTE).append(EQUALS).append(QUOTE).append(getId()).append(QUOTE).append(COMMA);
		sb.append(QUOTE).append("parent").append(QUOTE).append(EQUALS).append(QUOTE).append(getParentId()).append(QUOTE).append(COMMA);
		sb.append(QUOTE).append("text").append(QUOTE).append(EQUALS).append(QUOTE).append(getText()).append(QUOTE);
		if (file.isDirectory()) {
			sb.append(COMMA);
			if (children == null) {
				sb.append(QUOTE).append("children").append(QUOTE).append(EQUALS).append(true);
			} else {
				sb.append(QUOTE).append("children").append(QUOTE).append(EQUALS).append(getChildren());
			}
		}
		
		sb.append(CLOSE);
		return sb.toString();
	}

	public Map<String, Node> setChildren() {
		Map<String, Node> map = new HashMap<String, Node>();
		children = new ArrayList<Node>();
		for (File f : file.listFiles()) {
			Node node = new Node(f);
			node.setParent(this);
			children.add(node);
			map.put(f.getAbsolutePath(), node);
		}
		return map;
	}
}

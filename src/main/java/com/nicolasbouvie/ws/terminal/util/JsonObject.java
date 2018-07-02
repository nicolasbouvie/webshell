package com.nicolasbouvie.ws.terminal.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

public class JsonObject {
	private Map<String, String> params = new HashMap<String, String>();
	
	public void setParam(String param, String value) {
		params.put(param, value);
	}
	public void appendParam(String param, String value) {
		if (params.containsKey(param)) {
			value = params.get(param) + value;
		}
		setParam(param, value);
	}
	
	private String escapeValue(String value) {
		return value.replaceAll("\\\\", "\\\\\\\\").replaceAll("\\\"", "\\\\\"");
	}
	
	public void print(HttpServletResponse response) throws IOException {
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		out.print(this.toString());
		out.flush();
		out.close();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("{");
		for (String param : params.keySet()) {
			if (!params.get(param).isEmpty()) {
				sb.append("\"").append(param).append("\": ");
				sb.append("\"").append(escapeValue(params.get(param))).append("\", ");
			}
		}
		if (sb.length() >= 2) {
			sb.delete(sb.length()-2, sb.length());
		}
		sb.append("}");
		return sb.toString();
	}
}

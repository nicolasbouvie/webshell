package com.nicolasbouvie.ws.tree;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AjaxTree extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();

		String id = request.getParameter("id");
		String json;
		Map<String, Node> nodes;
		if ("#".equals(id)) {
			nodes = new HashMap<String, Node>();
			Node root = new Node(new File("/"));
			nodes.put(root.getId(), root);
			json = escape(root.toString());
			request.getSession().setAttribute("nodes", nodes);
		} else {
			nodes = (Map<String, Node>) request.getSession().getAttribute("nodes");
			Node node = nodes.get(id);
			if (node.getFile().isDirectory()) {
				nodes.putAll(node.setChildren());
			}
			json = escape(node.getChildren().toString());

		}
		out.println(json);

		out.flush();
		out.close();
	}

	private String escape(String text) {
		return text.replaceAll("\n", "\\\\n").replaceAll("\r", "");
	}
}

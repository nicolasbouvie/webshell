package com.nicolasbouvie.ws.terminal;

import java.io.File;
import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class Terminal extends HttpServlet {
	private static final long serialVersionUID = 3125480185151509554L;
	private static final String JSP = "WEB-INF/terminal.jsp";

	protected static final String WORKING_DIR_SESSION_KEY = "path";
	protected static final File HOME = new File(System.getProperty("user.home"));
	
	protected File getWorkingDir(HttpSession session) {
		if (session.getAttribute(WORKING_DIR_SESSION_KEY) == null) {
			session.setAttribute(WORKING_DIR_SESSION_KEY, HOME);
		}
		return (File) session.getAttribute(WORKING_DIR_SESSION_KEY);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		RequestDispatcher rd = request.getRequestDispatcher(JSP);
		File path = getWorkingDir(request.getSession());
		request.setAttribute("workingDir", path.getCanonicalPath());
		rd.forward(request, response);
	}
}

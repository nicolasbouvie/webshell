package com.nicolasbouvie.ws.terminal;

import java.io.File;
import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Terminal extends HttpServlet {
	private static final long serialVersionUID = 3125480185151509554L;
	private static final String JSP = "WEB-INF/terminal.jsp";

	public static final String WORKING_DIR_PARAM = "workingDirectory";
	public static final File HOME = new File(System.getProperty("user.home"));
	
	protected File getWorkingDir(HttpServletRequest request) {
		if (request.getParameter(WORKING_DIR_PARAM) == null) {
			return HOME;
		}
		return new File(request.getParameter(WORKING_DIR_PARAM));
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		RequestDispatcher rd = request.getRequestDispatcher(JSP);
		File path = getWorkingDir(request);
		request.setAttribute("workingDir", path.getCanonicalPath());
		rd.forward(request, response);
	}
}

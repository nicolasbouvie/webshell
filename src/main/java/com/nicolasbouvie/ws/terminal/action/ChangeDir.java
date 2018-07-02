package com.nicolasbouvie.ws.terminal.action;

import com.nicolasbouvie.ws.terminal.Terminal;
import com.nicolasbouvie.ws.terminal.util.JsonObject;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class ChangeDir extends Terminal {
	private static final String CD_HOME_DIR = "~";
	private static final String CD_LAST_DIR = "-";
	private static final String OLD_WORKING_DIR_SESSION_KEY = "oldPath";
	private static final long serialVersionUID = 962539219021371777L;

	public static final String PARAM = "directory";

	private File getLastWorkingDir(HttpSession session) {
		if (session.getAttribute(OLD_WORKING_DIR_SESSION_KEY) == null) {
			return getWorkingDir(session);
		}
		return (File) session.getAttribute(OLD_WORKING_DIR_SESSION_KEY);
	}
	
	private File changeWorkingDir(HttpSession session, File newPath) {
		File oldPath = getWorkingDir(session);
		session.setAttribute(OLD_WORKING_DIR_SESSION_KEY, oldPath);
		session.setAttribute(WORKING_DIR_SESSION_KEY, newPath);
		return oldPath;
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		HttpSession session = req.getSession();
		File path = getWorkingDir(session);
		String dir = req.getParameter(PARAM);
		if (CD_LAST_DIR.equals(dir)) {
			path = getLastWorkingDir(session);
		} else if (CD_HOME_DIR.equals(dir)) {
			path = HOME;
		} else {
			path = new File(dir.startsWith(File.separator) ? dir : path.getCanonicalPath() + File.separator + dir);
		}

		JsonObject json = new JsonObject();
		if (path.exists() && path.isDirectory()) {
			json.setParam("workingDir", path.getCanonicalPath());
			changeWorkingDir(session, path);
		} else if (path.exists() && !path.isDirectory()) { 
			json.setParam("message", "Not a directory");
		} else {
			json.setParam("message", "No such file or directory");
		}
		json.print(resp);
	}
}

package com.nicolasbouvie.ws.terminal.action;

import com.nicolasbouvie.ws.terminal.Terminal;
import com.nicolasbouvie.ws.terminal.util.JsonObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

public class ChangeDir extends Terminal {
	private static final long serialVersionUID = 962539219021371777L;

	private static final String CD_HOME_DIR = "~";
	public static final String PARAM = "directory";

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		File path = getWorkingDir(req);
		String dir = req.getParameter(PARAM);
		if (CD_HOME_DIR.equals(dir)) {
			path = HOME;
		} else {
			path = new File(dir.startsWith(File.separator) ? dir : path.getCanonicalPath() + File.separator + dir);
		}

		JsonObject json = new JsonObject();
		if (path.exists() && path.isDirectory()) {
			json.setParam("workingDir", path.getCanonicalPath());
		} else if (path.exists() && !path.isDirectory()) { 
			json.setParam("message", "Not a directory");
		} else {
			json.setParam("message", "No such file or directory");
		}
		json.print(resp);
	}
}

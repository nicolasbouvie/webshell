package com.nicolasbouvie.ws.terminal.action;

import com.nicolasbouvie.ws.terminal.ProcessExecutor;
import com.nicolasbouvie.ws.terminal.Terminal;
import com.nicolasbouvie.ws.terminal.util.Html;
import com.nicolasbouvie.ws.terminal.util.JsonObject;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class ExecuteCommand extends Terminal {
	private static final long serialVersionUID = -6992712347630834913L;
	private static final String EXECUTOR_SESSION_KEY = ExecuteCommand.class.getName();
	public static final String PARAM = "command";

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession();
		JsonObject json = new JsonObject();
		String cmd = request.getParameter(PARAM).replaceAll("\\&gt;", ">").replaceAll("\\&lt;", "<");

		ProcessExecutor process = (ProcessExecutor) session.getAttribute(EXECUTOR_SESSION_KEY);
		if (process == null) {
			try {
				process = new ProcessExecutor(cmd, getWorkingDir(request));
			} catch (Exception e) {
				json.setParam("error", e.getMessage() + Html.NEW_LINE);
				json.setParam("finished", "true");
			}
			session.setAttribute(EXECUTOR_SESSION_KEY, process);
		}

		if (process != null) {
			if (process.isFinished()) {
				json.setParam("finished", "true");
				killCommand(request.getSession());
			}
			json.setParam("out", process.getStdout());
			json.setParam("error", process.getError());
		}
		json.print(response);
	}
	
	public static void killCommand(HttpSession session) {
		session.setAttribute(EXECUTOR_SESSION_KEY, null);
	}
}

package com.nicolasbouvie.ws.terminal.action;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class KillProcess  extends HttpServlet {
	private static final long serialVersionUID = 7031589725009189444L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
		ExecuteCommand.killCommand(req.getSession());
	}
}

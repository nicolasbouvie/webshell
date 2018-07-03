package com.nicolasbouvie.ws.terminal.action;

import com.nicolasbouvie.ws.terminal.Terminal;
import com.nicolasbouvie.ws.terminal.util.Html;
import com.nicolasbouvie.ws.terminal.util.JsonObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.*;

public class AutoComplete extends Terminal {
	private static final long serialVersionUID = 3400432427828175396L;
	private static final TreeSet<String> COMMANDS_ON_PATH = new TreeSet<String>();
	public static final String PARAM = "complete";
	public static final String PARAM_CMD = "completeCMD";

	@Override
	public void init() {
		String path = System.getenv().get("Path");
		if (path == null) {
			path = System.getenv().get("PATH");
		}
		String[] paths = path.split(File.pathSeparator);
		for (String p : paths) {
			File dir = new File(p);
			if (dir.exists() && dir.isDirectory()) {
				for (File file : dir.listFiles()) {
					if (file.isFile()) {
						COMMANDS_ON_PATH.add(file.getName());
					}
				}
			}
		}
		COMMANDS_ON_PATH.add("wsget");
		COMMANDS_ON_PATH.add("wsedit");
	}
	
	private List<String> getValidCommands(String cmd) {
		Iterator<String> it = COMMANDS_ON_PATH.tailSet(cmd).iterator();
		List<String> validCommands = new ArrayList<String>();
		String command;
		while (it.hasNext() && (command = it.next()).startsWith(cmd)) {
			validCommands.add(command);
		}
		Collections.sort(validCommands);
		return validCommands;
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		final String cmd = request.getParameter(PARAM_CMD);
		final String complete;
		if (cmd != null && (cmd.startsWith(".") || cmd.startsWith(File.separator))) {
			complete = cmd;
		} else {
			complete = request.getParameter(PARAM);
		}
		JsonObject json = new JsonObject();
		
		if (complete != null) {
			File path = getWorkingDir(request);
			
			String lastDir = getNormalizedPath(path, complete);
			String lastPart = complete;
			if (complete.contains(File.separator)) {
				lastPart = complete.substring(complete.lastIndexOf(File.separator)+1);
			}
			File[] list = new File(lastDir).listFiles(new AutoCompleteFilter(lastDir, lastPart));
			if (list != null && list.length > 0) {
				Arrays.sort(list);
				StringBuilder eq = new StringBuilder(list[0].getPath());
				if (list.length > 1) {
					for (File f1 : list) {
						while (!f1.getPath().startsWith(eq.toString())) {
							eq.deleteCharAt(eq.length()-1);
						}
						json.appendParam("options", f1.getName());
						if (f1.isDirectory()) {
							json.appendParam("options", File.separator);
						}
						json.appendParam("options", Html.NEW_LINE);
					}
				}
				String comp = eq.substring(eq.lastIndexOf(File.separator+lastPart) + lastPart.length()+1);
				if (!comp.isEmpty() && !new File(lastDir).getCanonicalPath().equals(new File(comp).getCanonicalPath())) {
					if (list.length == 1) {
						comp += list[0].isDirectory() ? File.separator : Html.SPACE;
					}
					json.setParam(PARAM, comp);
				}
			}
		} else if (cmd != null) {
			List<String> commands = getValidCommands(cmd);
			if (!commands.isEmpty()) {
				StringBuilder eq = new StringBuilder(commands.get(0));
				if (commands.size() > 1) {
					for (String command : commands) {
						while (!command.startsWith(eq.toString())) {
							eq.deleteCharAt(eq.length()-1);
						}
						json.appendParam("options", command);
						json.appendParam("options", Html.NEW_LINE);
					}
				}
				String comp = eq.substring(cmd.length());
				if (!comp.isEmpty()) {
					json.appendParam(PARAM, comp);
				}
			}
		}
		json.print(response);
	}
	
	private String getNormalizedPath(File wd, String path) throws IOException {
		File file;
		path = path.contains(File.separator) ? path.substring(0, path.lastIndexOf(File.separator)+1) : "";

		if (path.startsWith(File.separator)) {
			file = new File(path);
		} else {
			file = new File(wd.getPath() + File.separator + path);
		}
		String ret = file.getCanonicalPath();
		return ret.endsWith(File.separator) ? ret : ret + File.separator;
	}
	
	private static class AutoCompleteFilter implements FileFilter {
		private String path;
		public AutoCompleteFilter(String wd, String search) {
			path = wd + search;
		}
		@Override
		public boolean accept(File pathname) {
			try {
				return pathname.getCanonicalPath().startsWith(path);
			} catch (IOException e) {
				return false;
			}
		}
	}
}

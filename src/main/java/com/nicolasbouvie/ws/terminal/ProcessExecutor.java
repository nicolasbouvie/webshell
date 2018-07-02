package com.nicolasbouvie.ws.terminal;

import com.nicolasbouvie.ws.terminal.util.Html;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class ProcessExecutor implements HttpSessionBindingListener, Serializable {
	private static final long serialVersionUID = -7076770815316267929L;
	private static final ExecutorService executorService = Executors.newCachedThreadPool();
	private static final int MAX_LINES = 1000;
	private final Semaphore stdoutSemaphore = new Semaphore(MAX_LINES);
	private final Semaphore stderrSemaphore = new Semaphore(MAX_LINES);
	private final AtomicInteger readers = new AtomicInteger(2);
	private Process process;
	private Writer stdout;
	private Writer stderr;
	private boolean finished;

	private class BufferWriter implements Runnable {
		private String cmd;
		private File file;
		private InputStream is;
		private Writer writer;
		private Semaphore semaphore;

		private BufferWriter(Writer writer, InputStream is, Semaphore semaphore) {
			this.is = is;
			this.writer = writer;
			this.semaphore = semaphore;
		}

		private BufferWriter(Writer writer, File file, String cmd) {
			this.writer = writer;
			this.file = file;
			this.cmd = cmd;
		}

		public void run() {
			if (file != null) {
				try {
					byte[] content = null;
					if (cmd.equals("wsget")) {
						content = FileUtils.readFileToByteArray(file);
					} else if (cmd.equals("wsedit")) {
						String strContent = file.exists() && file.canRead() ? FileUtils.readFileToString(file, Charset.defaultCharset()) : "";
						content = strContent.getBytes();
					}
					stdout.write(cmd+ "://" + file.getName() + "//" + Base64.encodeBase64String(content));
					finished = true;
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			try {
				String line;
				while (process != null && (line = br.readLine()) != null) {
					if (writer instanceof StringWriter) {
						semaphore.acquire();
					}

					if (process != null) {
						synchronized (process) {
							if (writer == null) {
								break;
							}
							if (writer instanceof StringWriter) {
								writer.append(line.replaceAll("<", "&lt;").replaceAll(">", "&gt;")).append(Html.NEW_LINE);
							} else {
								writer.append(line).append("\n");
							}
						}
					}
				}
				synchronized(ProcessExecutor.this) {
					if (readers.decrementAndGet() == 0 && process != null) {
						process.destroy();
						process = null;
						finished = true;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				IOUtils.closeQuietly(br);
				IOUtils.closeQuietly(writer);
			}
		}
	}

	public ProcessExecutor(String command, File wd) throws IOException {
		LinkedList<String> commandParts = new LinkedList<String>(Arrays.asList(command.split("\\s")));

		initWriters(commandParts, wd);

		command = commandParts.toString();
		command = command.substring(1, command.length()-1).replace(",", " ");

		if (commandParts.peekFirst().equals("wsedit") || commandParts.peekFirst().equals("wsget")) {
			File file = new File(commandParts.peekLast().startsWith("/") ? commandParts.peekLast() : wd.getAbsolutePath()+File.separator+commandParts.peekLast());
			executorService.submit(new BufferWriter(stdout, file, commandParts.peekFirst()));
		} else {
			this.process = Runtime.getRuntime().exec(command, null, wd);
			executorService.submit(new BufferWriter(stdout, process.getInputStream(), stdoutSemaphore));
			executorService.submit(new BufferWriter(stderr, process.getErrorStream(), stderrSemaphore));
		}
	}

	private void initWriters(LinkedList<String> commandParts, File wd) throws IOException {
		if (commandParts.peekFirst().equals("wsget") || commandParts.peekFirst().equals("wsedit")) {
			if (commandParts.size() != 2) {
				throw new IllegalArgumentException("Wrong number of arguments, please provide file to download. "+commandParts.peekFirst()+" <file>");
			}
		}

		if (commandParts.size() > 2) {
			initFileWriter(wd, commandParts);
			if (commandParts.size() > 2) {
				initFileWriter(wd, commandParts);
			}
		}
		initStringWriter();
	}

	private void initStringWriter() {
		if (this.stdout == null) {
			this.stdout = new StringWriter();
		}
		if (this.stderr == null) {
			this.stderr = new StringWriter();
		}
	}

	private void initFileWriter(File wd, LinkedList<String> commandParts) throws IOException {
		String fileName = commandParts.pollLast();
		String appender = commandParts.pollLast();

		boolean set = false;
		if (this.stdout == null){
			if (">".equals(appender)) {
				this.stdout = newFileWriter(fileName, false, wd);
				set = true;
			} else if (">>".equals(appender)) {
				this.stdout = newFileWriter(fileName, true, wd);
				set = true;
			}
		}
		if (this.stderr == null) {
			if ("2>".equals(appender)) {
				this.stderr = newFileWriter(fileName, false, wd);
				set = true;
			} else if ("2>>".equals(appender)) {
				this.stderr = newFileWriter(fileName, true, wd);
				set = true;
			}
		}
		if (!set) {
			commandParts.addLast(appender);
			commandParts.addLast(fileName);
		}
	}

	private FileWriter newFileWriter(String fileName, boolean append, File wd) throws IOException {
		File file = new File(fileName.startsWith("/") ? fileName : wd.getAbsolutePath()+File.separator+fileName);

		if (file.exists() && !file.isFile()) {
			throw new IOException("Impossible to write on folder " + fileName);
		}
		if (file.exists() && !file.canWrite() || !file.getParentFile().canWrite()) {
			throw new IOException("No permission to write " + fileName);
		}

		if (!append && file.exists() && !file.delete()) {
			throw new IOException("Impossible to rewrite " + fileName);
		}
		if (!file.exists() && !file.createNewFile()) {
			throw new IOException("Impossible to create file " + fileName);
		}

		return new FileWriter(file, append);
	}

	public boolean isFinished() {
		return finished || (stdout == null && stderr == null);
	}
	
	public String getStdout() {
		return getText(stdout, stdoutSemaphore);
	}
	
	public String getError() {
		return getText(stderr, stderrSemaphore);
	}

	private String getText(Writer writer, Semaphore semaphore) {
		synchronized(writer) {
			if (writer instanceof StringWriter) {
				String buf = writer.toString();
				try {
					writer.write("");
				} catch (IOException e) {
					e.printStackTrace();
				}
				semaphore.release(MAX_LINES - semaphore.availablePermits());
				return buf;
			}
			return "";
		}
	}

	@Override
	public void valueBound(HttpSessionBindingEvent event) {
	}

	@Override
	public void valueUnbound(HttpSessionBindingEvent event) {
		if (process != null) {
			process.destroy();
			process = null;
			stderrSemaphore.release(MAX_LINES);
			stdoutSemaphore.release(MAX_LINES);
		}
	}
}

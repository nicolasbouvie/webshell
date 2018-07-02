package com.nicolasbouvie.ws.terminal.action;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class SaveTextFile extends HttpServlet {
    public static final String FILE_PARAM = "filename";
    public static final String CONTENT_PARAM = "content";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String filename = req.getParameter(FILE_PARAM);
        String content = new String(Base64.decodeBase64(req.getParameter(CONTENT_PARAM)), Charset.defaultCharset());

        File file = new File(filename);
        FileUtils.writeStringToFile(file, content, Charset.defaultCharset());
    }
}

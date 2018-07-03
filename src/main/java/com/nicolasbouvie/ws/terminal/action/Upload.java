package com.nicolasbouvie.ws.terminal.action;

import com.nicolasbouvie.ws.terminal.Terminal;
import com.nicolasbouvie.ws.terminal.util.JsonObject;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class Upload extends Terminal {
    private static final long serialVersionUID = -2009929294302671514L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        File wd = getWorkingDir(req);
        if (!wd.canWrite()) {
            throw new ServletException("Invalid path, no permission to write");
        }

        JsonObject json = new JsonObject();
        try {
            List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(req);
            for (FileItem item : items) {
                String fileName = FilenameUtils.getName(item.getName());
                File file = new File(wd.getAbsolutePath() + File.separator + fileName);

                if (item.isFormField() || file.equals(wd)) continue;
                while (file.exists()) {
                    fileName += "_"+System.currentTimeMillis();
                    file = new File(wd.getAbsolutePath() + File.separator + fileName);
                }
                item.write(file);
            }
        } catch (Exception e) {
            json.setParam("error", "Cannot parse multipart request. " +e.getMessage());
            throw new ServletException("Cannot parse multipart request.", e);
        }
        json.print(resp);
    }
}

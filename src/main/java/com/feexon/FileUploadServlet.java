package com.feexon;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static org.apache.commons.fileupload.disk.DiskFileItemFactory.DEFAULT_SIZE_THRESHOLD;
import static org.apache.commons.io.FileUtils.copyFile;
import static org.apache.commons.io.FileUtils.copyInputStreamToFile;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.io.FilenameUtils.getName;
import static org.apache.commons.io.IOUtils.closeQuietly;

/**
 * Created by Administrator on 2016-07-16.
 */
public class FileUploadServlet extends HttpServlet {
  private static final String PARAM_TEMP_DIR = "tempDir";
  private static final String PARAM_UPLOAD_DIR = "uploadDir";
  private static final String DEFAULT_TEMP_DIR = "temp";
  private static final String DEFAULT_UPLOAD_DIR = "upload";
  private String tempDir = DEFAULT_TEMP_DIR;
  private String uploadDir = DEFAULT_UPLOAD_DIR;
  private ServletFileUpload multipartParser;

  @Override public void init() throws ServletException {
    super.init();
    setParameters();
    createMultipartParser();
  }

  private void setParameters() {
    tempDir = getInitParameter(PARAM_TEMP_DIR, DEFAULT_TEMP_DIR);
    uploadDir = getInitParameter(PARAM_UPLOAD_DIR, DEFAULT_UPLOAD_DIR);
  }

  private String getInitParameter(String name, String defaultValue) {
    if (isBlank(getInitParameter(name))) return defaultValue;
    return getInitParameter(name);
  }

  private void createMultipartParser() throws ServletException {
    try {
      multipartParser = new ServletFileUpload(createTempFileStoreIfNecessary());
    } catch (IOException e) {
      throw new ServletException(e);
    }
  }

  private FileItemFactory createTempFileStoreIfNecessary() throws IOException {
    return new DiskFileItemFactory(DEFAULT_SIZE_THRESHOLD, mkdir(in(tempDir)));
  }

  private File mkdir(File directory) throws IOException {
    FileUtils.forceMkdir(directory);
    return directory;
  }

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    renderTo(req, resp, file(getName(req.getRequestURI()), in(uploadDir)));
  }

  private void renderTo(HttpServletRequest req, HttpServletResponse resp, File file) throws IOException {
    ServletOutputStream out = resp.getOutputStream();
    try {
      copyFile(file, out);
    } finally {
      closeQuietly(out);
    }
  }

  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    if (isMultipart(req)) {
      try {
        List<String> filenames = saveUploadFiles(req);
        respondTo(req, resp, filenames);
      } catch (IOException e) {
        throw e;
      } catch (Exception e) {
        throw new ServletException(e);
      }
    }
  }

  private boolean isMultipart(HttpServletRequest req) {
    return ServletFileUpload.isMultipartContent(req);
  }

  private List<String> saveUploadFiles(HttpServletRequest req) throws FileUploadException, IOException {
    List<String> filenames = new ArrayList<>();
    for (FileItem item : multipartParser.parseRequest(req)) filenames.add(save(item));
    return filenames;
  }

  private void respondTo(HttpServletRequest req, HttpServletResponse resp, List<String> filenames) throws IOException, JSONException {
    PrintWriter out = resp.getWriter();
    out.write(paths(req, filenames).toString());
    out.close();
  }

  @Override protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    super.service(setRequestEncoding(req), resp);
  }

  private HttpServletRequest setRequestEncoding(HttpServletRequest req) throws UnsupportedEncodingException {
    req.setCharacterEncoding("UTF-8");
    return req;
  }

  private JSONObject paths(HttpServletRequest req, List<String> filenames) throws JSONException {
    JSONArray paths = new JSONArray();
    for (String filename : filenames) paths.put(path(req, filename));
    JSONObject root = new JSONObject();
    root.put("files", paths);
    return root;
  }

  private String save(FileItem item) throws IOException {
    InputStream source = item.getInputStream();
    try {
      String filename = generateFileName(item.getName());
      copyInputStreamToFile(source, file(filename, in(uploadDir)));
      return filename;
    } finally {
      closeQuietly(source);
    }
  }

  private File in(String path) {
    return path.startsWith("/") ? new File(path.substring(1)) : file(path);
  }

  private File file(String path) {
    return new File(getServletContext().getRealPath(path));
  }

  private File file(String filename, File parent) {
    return new File(parent, filename);
  }

  private String path(HttpServletRequest req, String file) {
    return req.getServletPath() + "/" + file;
  }

  private String generateFileName(String filename) {
    if (hasNoExtension(filename)) return randomName();
    return randomName() + "." + getExtension(filename);
  }

  private String randomName() {
    return UUID.randomUUID().toString();
  }

  private boolean hasNoExtension(String filename) {
    return isBlank(getExtension(filename));
  }

  private boolean isBlank(String source) {
    return source == null || source.isEmpty();
  }
}

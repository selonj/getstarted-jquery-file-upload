package com.feexon;

import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created by Administrator on 2016-07-16.
 */
public class FileUploadTest {
  private final String contextPath = "/jquery-file-upload";
  private final int serverPort = 1000;
  private final HttpClient client = HttpClients.createDefault();
  private final Server server = new Server(serverPort);

  @Before public void setUp() throws Exception {
    server.setStopAtShutdown(true);
    server.setHandler(runAsWebApplication());
    server.start();
  }

  private Handler runAsWebApplication() {
    WebAppContext webapp = new WebAppContext();
    webapp.setWar("src/main/webapp");
    webapp.setContextPath(contextPath);
    webapp.addOverrideDescriptor("/WEB-INF/web.xml");
    return webapp;
  }

  @After public void tearDown() throws Exception {
    server.stop();
  }

  @Test public void uploadsFileToServer() throws Exception {
    String content = "test";

    String location = theLocationOf(uploaded(file("readme.md", content)));

    assertThat(location, endsWith(".md"));

    assertThat(theContentOf(fetched(location)), equalTo(content));
  }

  private HttpResponse uploaded(HttpEntity file) throws IOException {
    return execute(postWithEntity(toURL("/upload"), file));
  }

  private HttpResponse fetched(String file) throws IOException {
    return execute(new HttpGet(toURL(file)));
  }

  private String theContentOf(HttpResponse response) throws IOException {
    return IOUtils.toString(response.getEntity().getContent());
  }

  private String theLocationOf(HttpResponse response) throws IOException, JSONException {
    return toJSONObject(theContentOf(response)).getJSONArray("files").getString(0);
  }

  private JSONObject toJSONObject(String content) throws IOException, JSONException {
    return new JSONObject(new JSONTokener(content));
  }

  private HttpResponse execute(HttpUriRequest request) throws IOException {
    HttpResponse response = client.execute(request);
    assertResponseThatOk(response);
    return response;
  }

  private HttpUriRequest postWithEntity(String url, HttpEntity entity) {
    HttpPost request = new HttpPost(url);
    request.setEntity(entity);
    return request;
  }

  private HttpEntity file(String filename, String content) {
    return MultipartEntityBuilder.create().addBinaryBody("files", content.getBytes(), ContentType.APPLICATION_OCTET_STREAM, filename).build();
  }

  private String toURL(String path) {
    if (!path.startsWith("/")) return path;
    return baseURL() + path;
  }

  private String baseURL() {
    return String.format("http://localhost:%d%s", serverPort, contextPath);
  }

  private void assertResponseThatOk(HttpResponse response) {
    assertThat(response.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
  }
}

package express.middleware;

import express.events.HttpRequest;
import express.http.Request;
import express.http.Response;
import express.multipart.MultiPartData;
import express.multipart.MultiPartStream;

import java.io.IOException;
import java.util.HashMap;

public class BodyParser implements HttpRequest {

  private final long MAX_SIZE;

  public BodyParser() {
    this.MAX_SIZE = -1;
  }

  public BodyParser(long maxFieldSize) {
    this.MAX_SIZE = maxFieldSize;
  }

  @Override
  public void handle(Request req, Response res) {

    HashMap<String, MultiPartData> dataMap = new HashMap<>();
    try {
      String boundary = "--" + req.getContentType().replaceAll("multipart/form-data; boundary=", "") + "\r\n";
      MultiPartStream stream = new MultiPartStream(req.getBody(), boundary.getBytes(), MAX_SIZE);
      MultiPartData data;

      while ((data = stream.read()) != null) {
        dataMap.put(data.getHead(), data);
      }

      req.setFormData(dataMap);
    } catch (IOException e) {
      // TODO: Handle error
      e.printStackTrace();
    }
  }
}

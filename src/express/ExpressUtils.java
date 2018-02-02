package express;

import express.utils.MIMETypes;

import java.io.*;

public class ExpressUtils {

  /**
   * Write all data from an InputStream in an String
   *
   * @param is The source inputstream
   * @return The data as string
   */
  public static String streamToString(InputStream is) {
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(is));
      StringBuilder sb = new StringBuilder();
      String line;

      while ((line = br.readLine()) != null)
        sb.append(line);

      return sb.toString();
    } catch (IOException e) {
      // TODO: Handle error
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Returns the MIME-Type of an file.
   *
   * @param file The file.
   * @return The MIME-Type.
   */
  public static String getContentType(File file) {
    String filename = file.getAbsolutePath().replaceAll("^(.*\\.|.*\\|.+$)", "");
    String contentType = MIMETypes.get().get(filename);

    if (contentType == null)
      contentType = "text/plain";

    return contentType;
  }

}

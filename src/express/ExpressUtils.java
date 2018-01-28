package express;

import com.sun.net.httpserver.Headers;
import express.cookie.Cookie;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressUtils {

  private final static HashMap<String, String> CTS = new HashMap<>();

  static {
    CTS.put("aac", "audio/aac");
    CTS.put("abw", "application/x-abiword");
    CTS.put("arc", "application/octet-stream");
    CTS.put("avi", "video/x-msvideo");
    CTS.put("azw", "application/vnd.amazon.ebook");
    CTS.put("bin", "application/octet-stream");
    CTS.put("bz", "application/x-bzip");
    CTS.put("bz2", "application/x-bzip2");
    CTS.put("csh", "application/x-csh");
    CTS.put("css", "text/css");
    CTS.put("csv", "text/csv");
    CTS.put("doc", "application/msword");
    CTS.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    CTS.put("eot", "application/vnd.ms-fontobject");
    CTS.put("epub", "application/epub+zip");
    CTS.put("gif", "image/gif");
    CTS.put("htm", "text/html");
    CTS.put("html", "text/html");
    CTS.put("ico", "image/x-icon");
    CTS.put("ics", "text/calendar");
    CTS.put("jar", "application/java-archive");
    CTS.put("jpeg", "image/jpeg");
    CTS.put("jpg", "image/jpeg");
    CTS.put("js", "application/javascript");
    CTS.put("json", "application/json");
    CTS.put("mid", "audio/midi");
    CTS.put("midi", "audio/midi");
    CTS.put("mpeg", "video/mpeg");
    CTS.put("mpkg", "application/vnd.apple.installer+xml");
    CTS.put("odp", "application/vnd.oasis.opendocument.presentation");
    CTS.put("ods", "application/vnd.oasis.opendocument.spreadsheet");
    CTS.put("odt", "application/vnd.oasis.opendocument.text");
    CTS.put("oga", "audio/ogg");
    CTS.put("ogv", "video/ogg");
    CTS.put("ogx", "application/ogg");
    CTS.put("otf", "font/otf");
    CTS.put("png", "image/png");
    CTS.put("pdf", "application/pdf");
    CTS.put("ppt", "application/vnd.ms-powerpoint");
    CTS.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
    CTS.put("rar", "application/x-rar-compressed");
    CTS.put("rtf", "application/rtf");
    CTS.put("sh", "application/x-sh");
    CTS.put("svg", "image/svg+xml");
    CTS.put("swf", "application/x-shockwave-flash");
    CTS.put("tar", "application/x-tar");
    CTS.put("tif", "image/tiff");
    CTS.put("tiff", "image/tiff");
    CTS.put("ts", "application/typescript");
    CTS.put("ttf", "font/ttf");
    CTS.put("vsd", "application/vnd.visio");
    CTS.put("wav", "audio/x-wav");
    CTS.put("weba", "audio/webm");
    CTS.put("webm", "video/webm");
    CTS.put("webp", "image/webp");
    CTS.put("woff", "font/woff");
    CTS.put("woff2", "font/woff2");
    CTS.put("xhtml", "application/xhtml+xml");
    CTS.put("xls", "application/vnd.ms-excel");
    CTS.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    CTS.put("xml", "application/xml");
    CTS.put("xul", "application/vnd.mozilla.xul+xml");
    CTS.put("zip", "application/zip");
    CTS.put("3gp", "video/3gpp");
    CTS.put("3g2", "video/3gpp2");
    CTS.put("7z", "application/x-7z-compressed");
  }

  public static HashMap<String, Cookie> parseCookies(Headers headers) {
    HashMap<String, Cookie> cookieList = new HashMap<>();
    List<String> headerCookies = headers.get("Cookie");

    if (headerCookies == null || headerCookies.size() == 0) {
      return cookieList;
    }

    String hcookies = headerCookies.get(0);

    String[] cookies = hcookies.split(";");
    for (String cookie : cookies) {
      String[] split = cookie.split("=");
      String name = split[0].trim();
      String value = split[1].trim();
      cookieList.put(name, new Cookie(name, value));
    }

    return cookieList;
  }

  public static HashMap<String, String> parseRawQuery(URI uri) {
    HashMap<String, String> querys = new HashMap<>();
    String rawQuery = uri.getRawQuery();

    if (rawQuery == null)
      return querys;

    Matcher mat = Pattern.compile("(.+?)=(.+?)(&|$)").matcher(rawQuery);
    while (mat.find()) {
      try {
        String key = URLDecoder.decode(mat.group(1), "UTF8");
        String val = URLDecoder.decode(mat.group(2), "UTF8");
        querys.put(key, val);
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
    }

    return querys;
  }

  public static String getContentType(String fileExtension) {
    String ct = CTS.get(fileExtension);

    if (ct == null)
      ct = "text/plain";

    return ct;
  }
}

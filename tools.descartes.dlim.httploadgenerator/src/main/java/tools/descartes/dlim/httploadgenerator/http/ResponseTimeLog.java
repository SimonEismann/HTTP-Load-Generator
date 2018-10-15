package tools.descartes.dlim.httploadgenerator.http;

public class ResponseTimeLog {

  public String url;
  public long start;
  public long end;
  
  public ResponseTimeLog(String url, long start, long end) {
    this.url = url;
    this.start = start;
    this.end = end;
  }
}

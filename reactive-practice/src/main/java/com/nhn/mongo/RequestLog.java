package com.nhn.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author haekyu cho
 */
@Document
public class RequestLog {

  @Id
  private long id;
  private String url;
  private String remoteIp;

  public long getId() {
    return id;
  }

  public String getUrl() {
    return url;
  }

  public String getRemoteIp() {
    return remoteIp;
  }

  public void setId(long id) {
    this.id = id;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public void setRemoteIp(String remoteIp) {
    this.remoteIp = remoteIp;
  }

  public RequestLog() {}

  public RequestLog(long id, String url, String remoteIp) {
    this.id = id;
    this.url = url;
    this.remoteIp = remoteIp;
  }

  @Override
  public String toString() {
    return "RequestLog{" + "id=" + id + ", url='" + url + '\'' + ", remoteIp='" + remoteIp + '\'' + '}';
  }
}

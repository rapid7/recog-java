package com.rapid7.recog;

import java.net.URL;
import java.time.Instant;

public class RecogVersion {

  private String repository;
  private String fullname;
  private String description;
  private URL htmlUrl;
  private String gitUrl;
  private String owner;
  private String tag;
  private String sha1;
  private Instant date;

  public RecogVersion(String repository, String fullname, String description, String owner, URL htmlUrl, String gitUrl, String tag, String sha1, Instant date) {
    this.repository = repository;
    this.fullname = fullname;
    this.description = description;
    this.owner = owner;
    this.htmlUrl = htmlUrl;
    this.gitUrl = gitUrl;
    this.tag = tag;
    this.sha1 = sha1;
    this.date = date;
  }

  public String getRepository() {
    return repository;
  }

  public String getFullName() {
    return fullname;
  }

  public String getDescription() {
    return description;
  }

  public URL getHtmlUrl() {
    return htmlUrl;
  }

  public String getGitUrl() {
    return gitUrl;
  }

  public String getOwner() {
    return owner;
  }

  public String getTag() {
    return tag;
  }

  public String getSha1() {
    return sha1;
  }

  public Instant getDate() {
    return date;
  }
}

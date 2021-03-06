package com.nhn.webflux.reactive.user.model;

public class UserRequest {

  private long id;
  private String name;
  private String email;
  private long teamId = 1L;

  public UserRequest(String name, String email) {
    this.id = 0;
    this.name = name;
    this.email = email;
    this.teamId = 0;
  }

  public UserRequest(long id, String name, String email, int teamId) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.teamId = teamId;
  }

  public UserRequest() {}

  public void setId(long id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setTeamId(long teamId) {
    this.teamId = teamId;
  }

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getEmail() {
    return email;
  }

  public long getTeamId() {
    return teamId;
  }

  @Override
  public String toString() {
    return "UserRequest{" + "id=" + id + ", name='" + name + '\'' + ", email='" + email + '\'' + ", teamId=" + teamId
           + '}';
  }
}

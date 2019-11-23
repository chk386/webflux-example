package com.nhn.rdbc;

import org.springframework.data.annotation.Id;

/**
 * @author haekyu cho
 */

public class User {

  @Id
  private long id;
  private String name;
  private String email;
  private long teamId = 1;

  public User(String name, String email) {
    this.name = name;
    this.email = email;
  }

  public User() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  @Override
  public String toString() {
    return "User{" + "id=" + id + ", name='" + name + '\'' + ", email='" + email + '\'' + '}';
  }
}

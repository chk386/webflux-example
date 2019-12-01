package com.nhn.webflux.reactive.user.model;

/**
 * @author haekyu cho
 */

public class UserResponse {
  private final long id;
  private final String name;
  private final String email;
  private final long teamId;
  private final String teamName;
  private final long companyId;
  private final String companyName;

  public UserResponse(long id, String name, String email, long teamId, String teamName, long companyId,
                      String companyName) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.teamId = teamId;
    this.teamName = teamName;
    this.companyId = companyId;
    this.companyName = companyName;
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

  public String getTeamName() {
    return teamName;
  }

  public long getCompanyId() {
    return companyId;
  }

  public String getCompanyName() {
    return companyName;
  }
}

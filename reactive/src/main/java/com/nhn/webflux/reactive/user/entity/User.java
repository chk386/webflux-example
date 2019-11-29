package com.nhn.webflux.reactive.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nhn.webflux.reactive.team.entity.Team;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

/**
 * @author haekyu cho
 */

@Entity
@Document
public class User implements Serializable {

  @Id
  @GeneratedValue
  private Long id;
  private String name;
  private String email;
  @OneToOne(optional = false)
  @JoinColumn
  @JsonIgnore
  private Team team = new Team();

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

  public Team getTeam() {
    return team;
  }

  public void setTeam(Team team) {
    this.team = team;
  }

  @Override
  public String toString() {
    return "User{" + "id=" + id + ", name='" + name + '\'' + ", email='" + email + '\'' + ", team=" + team + '}';
  }
}

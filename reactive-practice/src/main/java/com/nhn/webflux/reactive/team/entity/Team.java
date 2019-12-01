package com.nhn.webflux.reactive.team.entity;

import com.nhn.webflux.reactive.company.entity.Company;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Entity
public class Team implements Serializable {
  @Id
  @GeneratedValue
  private Long id;
  private String name;

  @OneToOne(optional = false)
  @JoinColumn
  private Company company = new Company();

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

  public Company getCompany() {
    return company;
  }

  public void setCompany(Company company) {
    this.company = company;
  }
}

package com.nhn.webflux.reactive.user;

public class User {

    private long id;
    private String name;
    private String email;
    private int teamId;

    public User(String name, String email) {
        this.id = 0;
        this.name = name;
        this.email = email;
        this.teamId = 0;
    }

    User(long id, String name, String email, int teamId) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.teamId = teamId;
    }

    public User() {}

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTeamId(int teamId) {
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

    public int getTeamId() {
        return teamId;
    }

    @Override
    public String toString() {
        return "User{" + "id=" + id + ", name='" + name + '\'' + ", email='" + email + '\'' + ", teamId=" + teamId
               + '}';
    }
}

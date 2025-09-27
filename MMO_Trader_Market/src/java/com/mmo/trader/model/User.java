package com.mmo.trader.model;

import java.io.Serializable;

/**
 * Represents a user in the system.
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int id;
    private final String username;
    private final String email;
    private final Role role;
    private final String password;

    public User(int id, String username, String email, Role role, String password) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public String getPassword() {
        return password;
    }
}

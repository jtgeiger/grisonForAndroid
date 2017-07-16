package com.sibilantsolutions.grisonforandroid.domain.model;

/**
 * Camera definition model.
 * <p>
 * Created by jt on 7/15/17.
 */

public class CamDef {

    private int id;
    private String name;
    private String host;
    private int port;
    private String username;
    private String password;

    public CamDef(String name, String host, int port, String username, String password) {

        this.name = name;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

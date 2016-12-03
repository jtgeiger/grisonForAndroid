package com.sibilantsolutions.grisonforandroid.domain;

public class CamDef {

    private String name;
    private String host;
    private int port;
    private String username;
    private String password;

    public CamDef(String name, String host, int port, String username, String password) {

        this.name = name;
        this.host = host;
        this.password = password;
        this.port = port;
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public String getPassword() {
        return password;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

}

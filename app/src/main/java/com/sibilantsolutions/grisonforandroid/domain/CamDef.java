package com.sibilantsolutions.grisonforandroid.domain;

import java.io.Serializable;

public class CamDef implements Serializable {

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CamDef camDef = (CamDef) o;

        if (port != camDef.port) return false;
        if (!name.equals(camDef.name)) return false;
        if (!host.equals(camDef.host)) return false;
        if (!username.equals(camDef.username)) return false;
        return password.equals(camDef.password);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + host.hashCode();
        result = 31 * result + port;
        result = 31 * result + username.hashCode();
        result = 31 * result + password.hashCode();
        return result;
    }

}

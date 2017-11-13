package com.dbkoda.drill.services;

import com.dbkoda.drill.model.ConnectionProfile;

import java.sql.Connection;

public class DrillConnection {
    private String id;

    private Connection connection;

    private ConnectionProfile profile;

    public DrillConnection(Connection connection, ConnectionProfile profile) {
        this.connection = connection;
        this.profile = profile;
        this.id = profile.getId();
    }

    public String getId() {
        return id;
    }

    public Connection getConnection() {
        return connection;
    }

    public ConnectionProfile getProfile() {
        return profile;
    }
}

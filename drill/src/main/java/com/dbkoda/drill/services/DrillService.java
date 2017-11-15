package com.dbkoda.drill.services;

import com.dbkoda.drill.exceptions.DrillException;
import com.dbkoda.drill.model.ConnectionProfile;
import com.dbkoda.drill.utils.DrillLogger;
import com.dbkoda.drill.utils.StatusCode;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.sql.*;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

@Service("DrillService")
public class DrillService implements DrillLogger {

    private static final String DRILL_URL = "jdbc:drill:drillbit=localhost:31010;schema=";

    private Map<String, DrillConnection> connectionMap = new Hashtable<>();

    private RestTemplate restTemplate;

    public DrillService() {
        this.restTemplate = new RestTemplate();
    }

    public void createConnection(ConnectionProfile connectionProfile) {
        try {
            Class.forName("org.apache.drill.jdbc.Driver");
            info("create drill connection " + connectionProfile.getAlias());
            String connectionId = this.createConnectionId(connectionProfile.getId(), connectionProfile.getDatabase());
            if (this.connectionMap.containsKey(connectionId)) {
                info("Connection already exist");
                return;
            }
            addProfile(connectionProfile);
            Connection connection = DriverManager.getConnection(DRILL_URL + connectionProfile.getAlias());
            DrillConnection drillConnection = new DrillConnection(connection, connectionProfile);
            connectionMap.put(this.createConnectionId(connectionProfile.getId(), connectionProfile.getDatabase()), drillConnection);
            Statement statement = connection.createStatement();
            statement.execute("use " + connectionProfile.getDatabase());
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new DrillException(StatusCode.CONNECTION_CREATE_ERROR, "Failed to create connection for " + connectionProfile.getAlias());
        }
    }

    public void addProfile(ConnectionProfile connectionProfile) {
        Profile profile = new Profile();
        profile.setName(connectionProfile.getAlias());
        Config config = new Config();
        config.setType("mongo");
        config.setEnabled(true);
        config.setConnection(connectionProfile.getUrl());
        profile.setConfig(config);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Gson gson = new Gson();
        String json = gson.toJson(profile);
        HttpEntity<String> entity = new HttpEntity<String>(json, headers);
        Object o = this.restTemplate.exchange("http://localhost:8047/storage/myplugin.json",
                HttpMethod.POST, entity, Object.class);
        info("get message " + o);
    }

    public void removeProfile(String id, String schema) {
        this.removeProfile(this.createConnectionId(id, schema));
    }

    /**
     * remove all connections under this profile
     *
     * @param id
     */
    public void removeProfileSchemas(String id) {
        info("Remove all schemas under " + id);
        this.connectionMap.forEach((k, v) -> {
            if (k.indexOf(id) == 0) {
                this.removeProfile(id, v.getProfile().getDatabase());
            }
        });
        this.connectionMap.keySet().removeIf(e -> e.indexOf(id) == 0);

    }

    private void removeProfile(String connectionId) {
        DrillConnection connection = this.connectionMap.get(connectionId);
        if (connection == null) {
            throw new DrillException(StatusCode.CONNECTION_NOT_FOUND, "Cant find connection id " + connectionId);
        }
        Map<String, Object> param = new Hashtable<>();
        param.put("uri", "/storage/" + connection.getProfile().getAlias() + "/delete");
        param.put("method", "GET");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Gson gson = new Gson();
        String json = gson.toJson(param);
        HttpEntity<String> entity = new HttpEntity<>(json, headers);
        Object o = this.restTemplate.getForObject("http://localhost:8047/storage/" + connection.getProfile().getAlias() + "/delete", Object.class, new HashMap<>());
        info("get message " + o);
    }

    public String executeSql(String id, String schema, String sql) {
        String connectionId = this.createConnectionId(id, schema);
        info("execute sql " + sql + " on " + connectionId);
        if (!this.connectionMap.containsKey(connectionId)) {
            throw new DrillException(StatusCode.CONNECTION_NOT_FOUND, "Cant find connection id " + id);
        }
        DrillConnection connection = this.connectionMap.get(connectionId);
        try {
            String[] split = sql.split("\n");
            if (split.length == 1) {
                Statement st = connection.getConnection().createStatement();
                JsonArray jsonData = resultSetToJson(st.executeQuery(sql));
                JsonObject ret = new JsonObject();
                ret.add("output", jsonData);
                info("get executed output " + ret.toString());
                return ret.toString();
            } else {
                JsonArray array = new JsonArray();
                for (String cmd : split) {
                    if (cmd.trim().isEmpty()) {
                        continue;
                    }
                    Statement st = connection.getConnection().createStatement();
                    JsonArray jsonData = resultSetToJson(st.executeQuery(cmd));
                    array.add(jsonData);
                }
                JsonObject ret = new JsonObject();
                ret.add("output", array);
                info("get executed output " + ret.toString());
                return ret.toString();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DrillException(StatusCode.SQL_EXCEPTION, "Execution Failed: " + e.getMessage(), e);
        }
    }

    private JsonArray resultSetToJson(ResultSet rSet) {
        JsonArray data = new JsonArray();
        try {
            ResultSetMetaData metaData = rSet.getMetaData();
            while (rSet.next()) {
                JsonObject obj = new JsonObject();
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    String value = rSet.getString(i);
                    if (value == null) {
                        value = "";
                    }
                    obj.add(metaData.getColumnName(i), new JsonPrimitive(value));
                }
                data.add(obj);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    public Map<String, DrillConnection> getProfiles() {
        Map ret = new HashMap();
        this.connectionMap.forEach((k, v) -> ret.put(k, new DrillConnection(null, v.getProfile())));
        return ret;
    }

    private String createConnectionId(String id, String schema) {
        return id + "-" + schema;
    }

    public void removeProfiles() {
        this.connectionMap.forEach((k, v) -> this.removeProfile(k));
    }
}

package com.dbkoda.drill.services;

import com.dbkoda.drill.exceptions.DrillException;
import com.dbkoda.drill.model.ConnectionProfile;
import com.dbkoda.drill.utils.DrillLogger;
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
//            Class.forName("org.apache.drill.jdbc.Driver");
            Connection connection = DriverManager.getConnection(DRILL_URL + connectionProfile.getAlias());
            DrillConnection drillConnection = new DrillConnection(connection, connectionProfile);
            connectionMap.put(drillConnection.getId(), drillConnection);
            addProfile(drillConnection.getId());
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DrillException("Failed to create connection for " + connectionProfile.getAlias());
        }
    }

    public Object addProfile(String id) {
        DrillConnection connection = this.connectionMap.get(id);
        Profile profile = new Profile();
        profile.setName(connection.getProfile().getAlias());
        Config config = new Config();
        config.setType("mongo");
        config.setEnabled(true);
        config.setConnection(connection.getProfile().getUrl());
        profile.setConfig(config);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Gson gson = new Gson();
        String json = gson.toJson(profile);
        HttpEntity<String> entity = new HttpEntity<String>(json, headers);

        Object o = this.restTemplate.exchange("http://localhost:8047/storage/myplugin.json",
                HttpMethod.POST, entity, Object.class);
        info("get message " + o);
        return o;
    }

    public Object removeProfile(String id) {
        DrillConnection connection = this.connectionMap.get(id);
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
        return o;
    }

    public String executeSql(String id, String sql) {
        if (!this.connectionMap.containsKey(id)) {
            throw new DrillException("Cant find connection id " + id);
        }
        DrillConnection connection = this.connectionMap.get(id);
        try {
            Statement st = connection.getConnection().createStatement();
            return resultSetToJson(st.executeQuery(sql));
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DrillException("Execution Failed on connection " + id, e);
        }
    }

    private String resultSetToJson(ResultSet rSet) {
        JsonArray data = new JsonArray();
        try {
            ResultSetMetaData metaData = rSet.getMetaData();
            while (rSet.next()) {
                JsonObject obj = new JsonObject();
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    obj.add(metaData.getColumnName(i), new JsonPrimitive(rSet.getString(i)));
                }
                data.add(obj);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data.toString();
    }

    public Map<String, DrillConnection> getProfiles() {
        Map ret = new HashMap();
        this.connectionMap.forEach((k, v) -> ret.put(k, new DrillConnection(null, v.getProfile())));
        return ret;
    }

}

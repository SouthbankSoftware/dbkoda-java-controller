package com.dbkoda.drill.services;

import com.dbkoda.drill.exceptions.DrillException;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.Hashtable;
import java.util.Map;

@Service("DrillService")
public class DrillService {

    private static final String DRILL_URL = "jdbc:drill:drillbit=localhost:31010;schema=";

    private Map<String, Connection> connectionMap = new Hashtable<>();

    public void createConnection(String id, String schema) throws DrillException {
        Connection connection = null;
        try {
//            Class.forName("org.apache.drill.jdbc.Driver");
            connection = DriverManager.getConnection(DRILL_URL + schema);
            connectionMap.put(id, connection);
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("select * from sys.`memory` limit 10");
            while (rs.next()) {
                System.out.println(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DrillException("Failed to create connection for " + schema);
        }
    }

    public String executeSql(String id, String sql) {
        if(!this.connectionMap.containsKey(id)){
            throw new DrillException("Cant find connection id " + id);
        }
        Connection connection = this.connectionMap.get(id);
        try {
            Statement st = connection.createStatement();
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
            while(rSet.next() ) {
                JsonObject obj = new JsonObject();
                for(int i=1; i<=metaData.getColumnCount(); i++) {
                    obj.add(metaData.getColumnName(i), new JsonPrimitive(rSet.getString(i)));
                }
                data.add(obj);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data.toString();
    }
}

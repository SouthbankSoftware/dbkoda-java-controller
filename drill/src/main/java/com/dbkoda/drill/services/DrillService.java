package com.dbkoda.drill.services;

import org.springframework.stereotype.Service;

import java.sql.*;

@Service("DrillService")
public class DrillService {

    public void createConnection() {
        Connection connection = null;
        try {
//            Class.forName("org.apache.drill.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:drill:drillbit=localhost:31010;schema=test");
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("select * from sys.`memory` limit 10");
            while (rs.next()) {
                System.out.println(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

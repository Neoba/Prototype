/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neoba.dsync.client;

import java.sql.*;

/**
 *
 * @author Atul Vinayak
 */
public class DBInit {
    //Run Once only!
    public static void main(String args[]) {
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:G:\\Lab\\test.db");
            System.out.println("Opened database successfully");

            stmt = c.createStatement();
            String sql = "CREATE TABLE entity "
                    + "(eidm LONG     NOT NULL,"
                    + " eidl LONG     NOT NULL,"
                    + " name          CHAR(50) NOT NULL, "
                    + " age           INT NOT NULL, "
                    + " dictionary    BLOB NOT NULL, "
                    + " delta         BLOB NOT NULL, PRIMARY KEY(eidm,eidl))";
            stmt.executeUpdate(sql);
            stmt.close();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Table created successfully");
    }
}

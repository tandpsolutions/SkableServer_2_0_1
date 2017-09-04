/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package support;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author bhaumik
 */
public class DBHelper {

    static DBHelper helper;
    private static MysqlDataSource mysqlDSAdmin = null;
    private static MysqlDataSource mysqlDSLoginDB = null;
    private static MysqlDataSource mysqlDSMain = null;

    public static DBHelper GetDBHelper() {
        helper = new DBHelper();
        if (mysqlDSAdmin == null) {
            setupAdminConnectionPool();
        }
        if (mysqlDSLoginDB == null) {
            setupLoginDBConnectionPool();
        }
        if (mysqlDSMain == null) {
            setupMainConnectionPool();
        }
        return helper;
    }

    private static void setupAdminConnectionPool() {
        String url = "jdbc:mysql://" + Constant.ip + ":" + 3306 + "/ipearl0116?sessionVariables=sql_mode=''";
        mysqlDSAdmin = new MysqlDataSource();
        mysqlDSAdmin.setURL(url);
        mysqlDSAdmin.setUser("root");
        mysqlDSAdmin.setPassword("root");
    }

    private static void setupLoginDBConnectionPool() {
        String url = "jdbc:mysql://" + Constant.ip + ":" + 3306 + "/ipearllogindb?sessionVariables=sql_mode=''";
        mysqlDSLoginDB = new MysqlDataSource();
        mysqlDSLoginDB.setURL(url);
        mysqlDSLoginDB.setUser("root");
        mysqlDSLoginDB.setPassword("root");
    }

    private static void setupMainConnectionPool() {
        String url = "jdbc:mysql://" + Constant.ip + ":" + 3306 + "/skablemain?sessionVariables=sql_mode=''";
        mysqlDSMain = new MysqlDataSource();
        mysqlDSMain.setURL(url);
        mysqlDSMain.setUser("root");
        mysqlDSMain.setPassword("root");
    }

    public Connection getConnMpAdmin() {
        try {
            return mysqlDSAdmin.getConnection();
        } catch (SQLException ex) {
            return null;
        }
    }

    public Connection getConnMpLogin() {
        try {
            return mysqlDSLoginDB.getConnection();
        } catch (SQLException ex) {
            return null;
        }
    }

    public Connection getMainConnection() {
        try {
            return mysqlDSMain.getConnection();
        } catch (SQLException ex) {
            return null;
        }
    }
}

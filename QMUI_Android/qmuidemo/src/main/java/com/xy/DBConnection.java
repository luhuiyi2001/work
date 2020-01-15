package com.xy;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据库连接层MYSQL
 * @author Administrator
 *
 */
public class DBConnection {
    
    
    /**
     * 连接数据
     * @return
     */
    public static Connection getDBConnection()
    {
        // 1. 注册驱动
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        // 获取数据库的连接
        try {
            Connection conn  = java.sql.DriverManager.getConnection("jdbc:mysql://114.55.67.172:3306/stock?useUnicode=true&characterEncoding=utf-8", "root", "root");
            return conn;
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
        return null;
    }
    
}
package com.wangyang.common.utils;

import com.wangyang.SentyBuild;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * mysql 数据库的连接池支持
 */
public class MysqlUtil {

    private static HikariDataSource dataSource = null;

    /**
     * 数据库连接池初始化方法
     */
    public static void build(){
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl((String) SentyBuild.getConf("databases.url"));
        config.setUsername((String) SentyBuild.getConf("databases.user"));
        config.setPassword((String) SentyBuild.getConf("databases.password"));
        config.setMaximumPoolSize((int) SentyBuild.getConf("hikari.pool.maxsize")); // 设置最大连接数
        config.setMinimumIdle((int) SentyBuild.getConf("hikari.pool.minidle")); // 设置最小空闲连接数
        config.setIdleTimeout((long) SentyBuild.getConf("hikari.pool.idle.timeout")); // 设置空闲连接超时时间
        config.setMaxLifetime((long) SentyBuild.getConf("hikari.pool.max.lifetime")); // 设置连接最大存活时间

        dataSource = new HikariDataSource(config);
    }

    /**
     * 获取连接的方法
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * 关闭连接的方法，但是不关连接池
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}

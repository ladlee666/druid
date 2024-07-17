package com.alibaba.druid.analysis.meta.constants;

/**
 * @author LENOVO
 * @date 2024/6/18 15:10
 */
public interface DbConstants {

    String ORACLE_URL_PREFIX = "jdbc:oracle:thin:@%s:%s/%s";
    String SQLSERVER_URL_PREFIX = "jdbc:sqlserver://%s:%s;database=%s;encrypt=false";
    String MYSQL_URL_PREFIX = "jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=utf8";
    String DM_URL_PREFIX = "jdbc:dm://%s:%s/%s";

    String DM_DRIVER = "dm.jdbc.driver.DmDriver";
    String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
    String MYSQL_DRIVER_6 = "com.mysql.cj.jdbc.Driver";
    String ORACLE_DRIVER = "oracle.jdbc.OracleDriver";
    String ORACLE_DRIVER2 = "oracle.jdbc.driver.OracleDriver";
    String POSTGRESQL_DRIVER = "org.postgresql.Driver";
}

package com.alibaba.druid.analysis.meta;

import com.alibaba.druid.DbType;
import com.alibaba.druid.util.Utils;
import com.alibaba.druid.analysis.meta.alter.CreatorFactory;
import com.alibaba.druid.analysis.meta.alter.ProviderManager;
import com.alibaba.druid.analysis.meta.constants.DbConstants;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author LENOVO
 * @date 2024/6/18 15:06
 */
public class MetaUtil implements DbConstants {

    private static Boolean mysql_driver_version_6;

    public static String getConnectionUrl(String dbType, String host, Long port, String database) {
        if (DbType.mysql.equals(dbType)) {
            return String.format(MYSQL_URL_PREFIX, host, port, database);
        }
        if (DbType.sqlserver.equals(dbType) || "mssql".equals(dbType)) {
            return String.format(SQLSERVER_URL_PREFIX, host, port, database);
        }
        if (DbType.oracle.equals(dbType)) {
            return String.format(ORACLE_URL_PREFIX, host, port, database);
        }
        if (DbType.dm.equals(dbType)) {
            return String.format(DM_URL_PREFIX, host, port, database);
        }
        throw new IllegalArgumentException("db:" + dbType + " is not be supported");
    }

    public static String getDriverClassName(String rawUrl) {
        if (StringUtils.isBlank(rawUrl)) {
            return null;
        }
        if (rawUrl.startsWith("jdbc:mysql:")) {
            if (mysql_driver_version_6 == null) {
                mysql_driver_version_6 = Utils.loadClass(MYSQL_DRIVER_6) != null;
            }
            if (mysql_driver_version_6) {
                return MYSQL_DRIVER_6;
            } else {
                return MYSQL_DRIVER;
            }
        } else if (rawUrl.startsWith("jdbc:oracle:") //
                || rawUrl.startsWith("JDBC:oracle:")) {
            return ORACLE_DRIVER;
        } else if (rawUrl.startsWith("jdbc:microsoft:")) {
            return "com.microsoft.jdbc.sqlserver.SQLServerDriver";
        } else if (rawUrl.startsWith("jdbc:sqlserver:")) {
            return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        } else if (rawUrl.startsWith("jdbc:dm:")) {
            return DM_DRIVER;
        } else if (rawUrl.startsWith("jdbc:postgresql:")) {
            return POSTGRESQL_DRIVER;
        } else {
            throw new IllegalArgumentException("unknown jdbc driver : " + rawUrl);
        }
    }

    public static Class<?> loadClass(String className) {
        Class<?> clazz = null;

        if (className == null) {
            return null;
        }

        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            // skip
        }

        ClassLoader ctxClassLoader = Thread.currentThread().getContextClassLoader();
        if (ctxClassLoader != null) {
            try {
                clazz = ctxClassLoader.loadClass(className);
            } catch (ClassNotFoundException e) {
                // skip
            }
        }

        return clazz;
    }

    /**
     * MySQL：`com.mysql.cj.MysqlType`
     * Oracle：`oracle.jdbc.OracleType`
     * SQL server：`com.microsoft.sqlserver.jdbc.JDBCType`
     * 达梦：dm.jdbc.driver.DmdbType
     *
     * @param dbType
     * @return
     */
    public static List<String> getColumnTypes(String dbType) {
        CreatorFactory provider = ProviderManager.getInstance().getFactoryProvider(DbType.of(dbType));
        return provider.columnTypeNames();
    }

}

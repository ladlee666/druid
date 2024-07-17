package com.alibaba.druid.analysis.seq.range.impl.db;

import cn.hutool.core.util.IdUtil;
import com.alibaba.druid.analysis.seq.SeqException;
import com.alibaba.druid.analysis.seq.range.RangeInfo;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.*;

import static com.alibaba.druid.analysis.seq.range.impl.db.DbConstant.*;

@Slf4j
public class DbHelper {

    /**
     * @param dataSource
     * @param tableName
     * @param dbType
     */
    public static void createTable(DataSource dataSource, String tableName, String dbType) {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = dataSource.getConnection();
            stmt = conn.createStatement();
            stmt.executeUpdate(getCreateSql(dbType, tableName));
        } catch (SQLException e) {
            throw new SeqException(e);
        } finally {
            closeQuietly(stmt);
            closeQuietly(conn);
        }
    }

    public static RangeInfo selectRange(DataSource dataSource, String tableName, String name, long stepStart, Integer step) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            log.info("SQL_SELECT_RANGE");
            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(SQL_SELECT_RANGE.replace("#tableName", tableName));
            stmt.setString(1, name);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                insertRange(dataSource, tableName, name, stepStart, step);
                return null;
            }
            Long oldValue = rs.getLong(1);
            Integer dbStep = rs.getInt(2);
            // 循环
            Boolean loop = rs.getBoolean(3);
            // 最大值
            Long dbMax = rs.getLong(4);
            // 单词取值范围
            Integer range = rs.getInt(5);
            if (oldValue < 0) {
                String msg =
                        "Sequence value cannot be less than zero, value = " + oldValue + ", please check table sequence"
                                + tableName;
                throw new SeqException(msg);
            }

            if (oldValue > Long.MAX_VALUE - DELTA) {
                String msg =
                        "Sequence value overflow, value = " + oldValue + ", please check table sequence" + tableName;
                throw new SeqException(msg);
            }
            return new RangeInfo(oldValue, dbStep, loop, dbMax, range);
        } catch (SQLException e) {
            throw new SeqException(e);
        } finally {
            closeQuietly(rs);
            closeQuietly(stmt);
            closeQuietly(conn);
        }
    }

    /**
     * insert range data (init)
     *
     * @param dataSource
     * @param tableName
     * @param name
     * @param stepStart
     */
    public static void insertRange(DataSource dataSource, String tableName, String name, Long stepStart, Integer step) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            log.info("SQL_INSERT_RANGE");
            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(SQL_INSERT_RANGE.replace("#tableName", tableName));
            stmt.setString(1, IdUtil.getSnowflakeNextIdStr());
            stmt.setString(2, name);
            stmt.setLong(3, stepStart);
            stmt.setInt(4, step);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new SeqException(e);
        } finally {
            closeQuietly(stmt);
            closeQuietly(conn);
        }
    }

    public static boolean updateRange(DataSource dataSource, String tableName, Long newValue, Long oldValue, String name) {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            log.info("SQL_UPDATE_RANGE");
            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(SQL_UPDATE_RANGE.replace("#tableName", tableName));
            stmt.setLong(1, newValue);
            stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            stmt.setString(3, name);
            stmt.setLong(4, oldValue);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            throw new SeqException(e);
        } finally {
            closeQuietly(stmt);
            closeQuietly(conn);
        }
    }

    private static String getCreateSql(String dbType, String tableName) {
        switch (dbType) {
            case "oracle":
                return SQL_ORACLE_CREATE_TABLE.replace("#tableName", tableName);
            case "mysql":
                return SQL_MYSQL_CREATE_TABLE.replace("#tableName", tableName);
            default:
                throw new SeqException("不支持的数据库类型!");
        }
    }

    private static void closeQuietly(AutoCloseable closeable) {
        if (null != closeable) {
            try {
                closeable.close();
            } catch (Throwable e) {
            }
        }
    }
}

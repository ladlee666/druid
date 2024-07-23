package com.alibaba.druid.analysis.meta;

import com.alibaba.druid.DbType;
import com.alibaba.druid.util.JdbcUtils;
import com.alibaba.druid.analysis.meta.constants.View;
import com.alibaba.druid.analysis.meta.model.MetaDBInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author LENOVO
 * @date 2024/6/14 16:53
 */
@Slf4j
public class MetaDBHelper {

    protected static final ScheduledExecutorService CLEAR = new ScheduledThreadPoolExecutor(1,
            new NamedThreadFactory("metaDbConnectionTimeoutClear", 1, true));

    private final static Map<MetaDBInfo, ConnectionHolder> CONN_CACHE = new ConcurrentHashMap<>();
    private final static Map<DataSource, ConnectionHolder> DS_CACHE = new ConcurrentHashMap<>();

    /**
     * 秒
     */
    private static final Long TIME_TO_EXPIRY = 60L * 6;

    static {
        init();
    }

    public static void init() {
        CLEAR.scheduleAtFixedRate(() -> {
            for (Map.Entry<MetaDBInfo, ConnectionHolder> entry : CONN_CACHE.entrySet()) {
                ConnectionHolder holder = entry.getValue();
                synchronized (holder.getLOCK()) {
                    try {
                        if (holder.getConnection().isClosed() || holder.getExpiryTime().isBefore(LocalDateTime.now())) {
                            releaseConnection(holder);
                            CONN_CACHE.remove(entry.getKey());
                        }
                    } catch (Exception ex) {
                        log.error("清理连接异常:{}", ex.getMessage(), ex);
                    }
                }
            }
        }, 3000, 15000, TimeUnit.MILLISECONDS);
    }

    public static ConnectionHolder getConnectionFromDs(DataSource dataSource) {
        return DS_CACHE.computeIfAbsent(dataSource, ds -> {
            try {
                return new ConnectionHolder(dataSource.getConnection(), LocalDateTime.now().plusSeconds(TIME_TO_EXPIRY));
            } catch (Exception ex) {
                throw new RuntimeException("获取Connection异常:" + ex.getMessage(), ex);
            }
        });
    }

    public static ConnectionHolder getConnection(MetaDBInfo metaDBInfo) throws SQLException {
        ConnectionHolder newHolder = CONN_CACHE.computeIfAbsent(metaDBInfo, info -> {
            try {
                if (StringUtils.isBlank(metaDBInfo.getDbType())) {
                    metaDBInfo.setDbType(JdbcUtils.getDbType(metaDBInfo.getUrl(), metaDBInfo.getDriverClass()));
                }
                String driverClass = metaDBInfo.getDriverClass();
                Class.forName(driverClass);
                String url = appendExtraParams(metaDBInfo.getUrl(), metaDBInfo.getExtraParams());
                Connection conn = DriverManager.getConnection(url, metaDBInfo.getUserName(), metaDBInfo.getPassword());
                return new ConnectionHolder(conn, LocalDateTime.now().plusSeconds(TIME_TO_EXPIRY));
            } catch (Exception ex) {
                throw new RuntimeException("获取连接异常.", ex);
            }
        });
        synchronized (newHolder.getLOCK()) {
            if (newHolder.getConnection().isClosed() || newHolder.getExpiryTime().isBefore(LocalDateTime.now())) {
                String url = appendExtraParams(metaDBInfo.getUrl(), metaDBInfo.getExtraParams());
                Connection conn = DriverManager.getConnection(url, metaDBInfo.getUserName(), metaDBInfo.getPassword());
                ConnectionHolder holder = new ConnectionHolder(conn, LocalDateTime.now());
                CONN_CACHE.put(metaDBInfo, holder);
                return holder;
            }
        }
        return newHolder;
    }

    public static ResultSet getResultSet(MetaDBInfo metaDBInfo, String tableNamePattern) throws SQLException {
        ConnectionHolder holder = getConnection(metaDBInfo);
        if (holder == null || holder.getConnection() == null) {
            throw new RuntimeException("connection is not available.");
        }
        Connection conn = holder.getConnection();
        DatabaseMetaData metaData = conn.getMetaData();
        // oracle:null mysql:库名
        String catalog = conn.getCatalog();
        // oracle:用户空间/库名(username) mysql:null
        String schema = conn.getSchema();
        return metaData.getTables(catalog, schema, tableNamePattern, new String[]{View.TABLE});
    }

    public static DatabaseMetaData getMetaData(MetaDBInfo metaDBInfo) throws SQLException {
        ConnectionHolder holder = getConnection(metaDBInfo);
        if (holder == null || holder.getConnection() == null) {
            throw new RuntimeException("connection is not available.");
        }
        Connection conn = holder.getConnection();
        return conn.getMetaData();
    }

    public static void releaseConnection(ConnectionHolder holder) {
        if (holder != null && holder.getConnection() != null) {
            try {
                if (!holder.getConnection().isClosed()) {
                    holder.setExpiryTime(LocalDateTime.now().plusMinutes(TIME_TO_EXPIRY));
                }
//                holder.getConnection().close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void releaseStmt(Statement x) {
        if (x == null) {
            return;
        }
        try {
            x.close();
        } catch (Exception e) {
            boolean printError = true;

            if (e instanceof SQLRecoverableException
                    && "Closed Connection".equals(e.getMessage())) {
                printError = false;
            }

            if (printError) {
                log.debug("close statement error", e);
            }
        }
    }

    public static void releaseResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ex) {
                log.error("rs关闭失败:{}", ex.getMessage(), ex);
            }
        }
    }

    private static String appendExtraParams(String url, Map<String, Object> extraParams) {
        if (extraParams == null || extraParams.size() == 0) {
            return url;
        }
        Pair<String, Map<String, String>> pair = parserUrlQuery(url);
        Map<String, String> originQuery = pair.getValue();
        Map<String, ?> mergedMap = Stream.concat(extraParams.entrySet().stream(), originQuery.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (existingValue, newValue) -> newValue));
        String host = pair.getKey();
        StringBuilder sb = new StringBuilder(host);
        if (mergedMap.size() > 0) {
            sb.append("?");
        }
        mergedMap.forEach((k, v) -> sb.append(k).append("=").append(v).append("&"));
        String finalUrl = sb.toString();
        return finalUrl.substring(0, finalUrl.length() - 1);
    }

    private static Pair<String, Map<String, String>> parserUrlQuery(String url) {
        Map<String, String> params = new HashMap<>();
        if (StringUtils.isBlank(url)) {
            return Pair.of(url, params);
        }
        int i = url.indexOf('?');
        if (i == -1) {
            return Pair.of(url, params);
        }
        String queryStr = url.substring(i + 1);
        if (StringUtils.isBlank(queryStr)) {
            return Pair.of(url, params);
        }
        String host = url.substring(0, i);
        String[] pairs = queryStr.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            params.put(kv[0].trim(), kv[1].trim());
        }
        return Pair.of(host, params);
    }


}

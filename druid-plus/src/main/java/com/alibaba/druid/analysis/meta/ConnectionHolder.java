package com.alibaba.druid.analysis.meta;

import java.sql.Connection;
import java.time.LocalDateTime;

/**
 * @author LENOVO
 * @date 2024/6/14 16:55
 */
public class ConnectionHolder {

    private final Connection connection;

    /**
     * 过期时间
     */
    private LocalDateTime expiryTime;

    protected final Object LOCK = new Object();

    public ConnectionHolder(Connection connection, LocalDateTime expiryTime) {
        this.connection = connection;
        this.expiryTime = expiryTime;
    }

    public Connection getConnection() {
        return connection;
    }

    public Object getLOCK() {
        return LOCK;
    }

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(LocalDateTime expiryTime) {
        this.expiryTime = expiryTime;
    }
}

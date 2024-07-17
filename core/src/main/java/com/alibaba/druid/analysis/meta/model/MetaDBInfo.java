package com.alibaba.druid.analysis.meta.model;


import com.alibaba.druid.DbType;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author LENOVO
 * @date 2024/6/14 16:49
 */
public class MetaDBInfo {

    /**
     * 数据库连接url
     */
    private String url;

    /**
     * 数据库类型
     */
    private String dbType;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 密码
     */
    private String password;

    /**
     * 驱动class
     */
    private String driverClass;

    /**
     * 扩展参数
     */
    private Map<String, Object> extraParams;

    public static Builder builder() {
        return new Builder();
    }

    public MetaDBInfo() {
    }

    public MetaDBInfo(Builder builder) {
        check(builder);
        this.url = builder.url;
        this.dbType = builder.dbType;
        this.userName = builder.userName;
        this.password = builder.password;
        this.driverClass = builder.driverClass;
        if (builder.extraParams == null) {
            this.extraParams = new HashMap<>();
        } else {
            this.extraParams = builder.extraParams;
        }
        if (DbType.oracle.equals(dbType)) {
            this.extraParams.put("remarks", true);
        } else if (DbType.mysql.equals(dbType)) {
            this.extraParams.put("useInformationSchema", true);
        }
    }

    private void check(Builder builder) {
        if (StringUtils.isAnyBlank(builder.url, builder.dbType, builder.userName, builder.password, builder.driverClass)) {
            throw new IllegalArgumentException("Missing necessary parameters[url/dbType/userName/password/driverClass].");
        }
    }

    public MetaDBInfo(String url, String dbType, String userName, String password, String driverClass) {
        this.url = url;
        this.dbType = dbType;
        this.userName = userName;
        this.password = password;
        this.driverClass = driverClass;
        this.extraParams = new HashMap<>();
        if (DbType.oracle.equals(dbType)) {
            this.extraParams.put("remarks", true);
        } else if (DbType.mysql.equals(dbType)) {
            this.extraParams.put("useInformationSchema", true);
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    public Map<String, Object> getExtraParams() {
        return extraParams;
    }

    public void setExtraParams(Map<String, Object> extraParams) {
        this.extraParams = extraParams;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetaDBInfo that = (MetaDBInfo) o;
        return Objects.equals(url, that.url) && Objects.equals(dbType, that.dbType) && Objects.equals(userName, that.userName) && Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, dbType, userName, password);
    }

    public static class Builder {
        private String url;
        private String dbType;
        private String userName;
        private String password;
        private String driverClass;
        private Map<String, Object> extraParams;

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder dbType(String dbType) {
            this.dbType = dbType;
            return this;
        }

        public Builder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder driverClass(String driverClass) {
            this.driverClass = driverClass;
            return this;
        }

        public Builder extraParams(Map<String, Object> extraParams) {
            this.extraParams = extraParams;
            return this;
        }

        public MetaDBInfo build() {
            return new MetaDBInfo(this);
        }
    }
}

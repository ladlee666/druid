package com.alibaba.druid.analysis.sql.model;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.analysis.sql.visitor.CustomVisitor;
import lombok.Builder;
import lombok.Data;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;

@Data
public class ChainContext {

    private SQLStatement sqlStatement;

    private SQLStatement countStatement;

    private CustomVisitor visitor;

    private ExecuteContext context;

    private DbType dbType;

    private boolean authWrap;

    private List<String> paramKeys;

    private JdbcTemplate jdbcTemplate;

    private String userId;

    private String deptId;

    private Integer roleRange;

    private List<String> childDeptIds;

    private DataSource dataSource;

    private List<Object> autoIds;

    /**
     * oracle PL/SQL 输出
     */
    private boolean dbmsOutput;

    ChainContext(SQLStatement sqlStatement, SQLStatement countStatement, CustomVisitor visitor, ExecuteContext context, DbType dbType, boolean authWrap, List<String> paramKeys, JdbcTemplate jdbcTemplate, String userId, String deptId, Integer roleRange, List<String> childDeptIds, DataSource dataSource, List<Object> autoIds, boolean dbmsOutput) {
        this.sqlStatement = sqlStatement;
        this.countStatement = countStatement;
        this.visitor = visitor;
        this.context = context;
        this.dbType = dbType;
        this.authWrap = authWrap;
        this.paramKeys = paramKeys;
        this.jdbcTemplate = jdbcTemplate;
        this.userId = userId;
        this.deptId = deptId;
        this.roleRange = roleRange;
        this.childDeptIds = childDeptIds;
        this.dataSource = dataSource;
        this.autoIds = autoIds;
        this.dbmsOutput = dbmsOutput;
    }

    public static ChainContext.ChainContextBuilder builder() {
        return new ChainContext.ChainContextBuilder();
    }

    public static class ChainContextBuilder {
        private SQLStatement sqlStatement;
        private SQLStatement countStatement;
        private CustomVisitor visitor;
        private ExecuteContext context;
        private DbType dbType;
        private boolean authWrap;
        private List<String> paramKeys;
        private JdbcTemplate jdbcTemplate;
        private String userId;
        private String deptId;
        private Integer roleRange;
        private List<String> childDeptIds;
        private DataSource dataSource;
        private List<Object> autoIds;
        private boolean dbmsOutput;

        ChainContextBuilder() {
        }

        public ChainContext.ChainContextBuilder sqlStatement(SQLStatement sqlStatement) {
            this.sqlStatement = sqlStatement;
            return this;
        }

        public ChainContext.ChainContextBuilder countStatement(SQLStatement countStatement) {
            this.countStatement = countStatement;
            return this;
        }

        public ChainContext.ChainContextBuilder visitor(CustomVisitor visitor) {
            this.visitor = visitor;
            return this;
        }

        public ChainContext.ChainContextBuilder context(ExecuteContext context) {
            this.context = context;
            return this;
        }

        public ChainContext.ChainContextBuilder dbType(DbType dbType) {
            this.dbType = dbType;
            return this;
        }

        public ChainContext.ChainContextBuilder authWrap(boolean authWrap) {
            this.authWrap = authWrap;
            return this;
        }

        public ChainContext.ChainContextBuilder paramKeys(List<String> paramKeys) {
            this.paramKeys = paramKeys;
            return this;
        }

        public ChainContext.ChainContextBuilder jdbcTemplate(JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
            return this;
        }

        public ChainContext.ChainContextBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public ChainContext.ChainContextBuilder deptId(String deptId) {
            this.deptId = deptId;
            return this;
        }

        public ChainContext.ChainContextBuilder roleRange(Integer roleRange) {
            this.roleRange = roleRange;
            return this;
        }

        public ChainContext.ChainContextBuilder childDeptIds(List<String> childDeptIds) {
            this.childDeptIds = childDeptIds;
            return this;
        }

        public ChainContext.ChainContextBuilder dataSource(DataSource dataSource) {
            this.dataSource = dataSource;
            return this;
        }

        public ChainContext.ChainContextBuilder autoIds(List<Object> autoIds) {
            this.autoIds = autoIds;
            return this;
        }

        public ChainContext.ChainContextBuilder dbmsOutput(boolean dbmsOutput) {
            this.dbmsOutput = dbmsOutput;
            return this;
        }

        public ChainContext build() {
            return new ChainContext(this.sqlStatement, this.countStatement, this.visitor, this.context, this.dbType, this.authWrap, this.paramKeys, this.jdbcTemplate, this.userId, this.deptId, this.roleRange, this.childDeptIds, this.dataSource, this.autoIds, this.dbmsOutput);
        }

        public String toString() {
            return "ChainContext.ChainContextBuilder(sqlStatement=" + this.sqlStatement + ", countStatement=" + this.countStatement + ", visitor=" + this.visitor + ", context=" + this.context + ", dbType=" + this.dbType + ", authWrap=" + this.authWrap + ", paramKeys=" + this.paramKeys + ", jdbcTemplate=" + this.jdbcTemplate + ", userId=" + this.userId + ", deptId=" + this.deptId + ", roleRange=" + this.roleRange + ", childDeptIds=" + this.childDeptIds + ", dataSource=" + this.dataSource + ", autoIds=" + this.autoIds + ", dbmsOutput=" + this.dbmsOutput + ")";
        }
    }

}

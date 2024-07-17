package com.alibaba.druid.analysis.meta.alter.chain;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.analysis.meta.model.MetaDBColumn;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LENOVO
 * @date 2024/6/24 11:17
 */
public class AlterContext {

    private final List<MetaDBColumn> metaDBColumns;

    private final DbType dbType;

    private final String tableName;

    private final List<SQLStatement> stmts = new ArrayList<>();

    public AlterContext(List<MetaDBColumn> metaDBColumns, DbType dbType, String tableName) {
        this.metaDBColumns = metaDBColumns;
        this.dbType = dbType;
        this.tableName = tableName;
    }

    public List<MetaDBColumn> getMetaDBColumns() {
        return metaDBColumns;
    }

    public DbType getDbType() {
        return dbType;
    }

    public String getTableName() {
        return tableName;
    }

    public List<SQLStatement> getStmts() {
        return stmts;
    }
}

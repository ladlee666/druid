package com.alibaba.druid.analysis.sql.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class TableModel {

    private String tableName;

    private boolean isSubQuery;

    private String alias;

    public TableModel(String tableName, boolean isSubQuery, String alias) {
        this.tableName = tableName;
        this.isSubQuery = isSubQuery;
        this.alias = alias;
    }
}

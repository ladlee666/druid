package com.alibaba.druid.analysis.sql.model;

import lombok.Data;

@Data
public class ColumnParam {

    /**
     * 列名称
     */
    private String columnName;

    /**
     * 列类型
     *
     * @see java.sql.Types
     */
    private Integer columnType;

    /**
     * 列类型名称
     */
//    private String columnTypeName;

    public ColumnParam(String columnName, Integer columnType) {
        this.columnName = columnName;
        this.columnType = columnType;
    }
}

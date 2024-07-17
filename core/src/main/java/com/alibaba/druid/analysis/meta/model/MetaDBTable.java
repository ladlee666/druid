package com.alibaba.druid.analysis.meta.model;

import lombok.Data;

import java.util.*;

/**
 * @author LENOVO
 * @date 2024/6/5 16:38
 */
@Data
public class MetaDBTable {

    private String tableName;
    private String tableCat;
    private String tableSchema;
    private String tableType;
    private String remarks;
    private List<MetaDBColumn> metaDBColumns = new ArrayList<>();
    private Map<String, List<Pk>> pk = new HashMap<>();
    /**
     * 数据库字段类型
     */
    private List<String> columnTypes;
    /**
     * 主键字段名列表
     */
    private Set<String> pkNames = new LinkedHashSet<>();

    public static MetaDBTable create(String tableName) {
        return new MetaDBTable(tableName);
    }

    public MetaDBTable setColumn(MetaDBColumn column) {
        this.metaDBColumns.add(column);
        return this;
    }

    public boolean isPk(String columnName) {
        return getPkNames().contains(columnName);
    }

    public MetaDBTable addPk(String pkColumnName) {
        this.pkNames.add(pkColumnName);
        return this;
    }

    public MetaDBTable() {
    }

    public MetaDBTable(String tableName) {
        this.tableName = tableName;
    }

    public MetaDBTable(String tableName, String tableCat, String tableSchema, String tableType, String remarks) {
        this.tableName = tableName;
        this.tableCat = tableCat;
        this.tableSchema = tableSchema;
        this.tableType = tableType;
        this.remarks = remarks;
    }
}

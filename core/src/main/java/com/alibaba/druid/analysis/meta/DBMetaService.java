package com.alibaba.druid.analysis.meta;

import com.alibaba.druid.analysis.meta.model.MetaDBColumn;
import com.alibaba.druid.analysis.meta.model.MetaDBTable;

import java.util.List;

/**
 * @author LENOVO
 * @date 2024/6/5 16:23
 */
public interface DBMetaService {

    List<MetaDBTable> getTableInfo(String tableNamePattern);

    /**
     * 查询表和表关联的字段信息(科筛选s)
     *
     * @param tableNamePattern
     * @param columnNamePattern
     * @return
     */
    List<MetaDBTable> getTables(String tableNamePattern, String columnNamePattern, boolean ignoreColumn, String... types);

    /**
     * @param tableNamePattern
     * @param types            查询类型 com.alibaba.druid.analysis.meta.constants.View
     * @return
     */
    List<MetaDBTable> getTables(String tableNamePattern, String... types);

    /**
     * 获取列定义
     *
     * @param tableName
     * @param columnName
     * @return
     */
    List<MetaDBColumn> getColumns(String tableName, String columnName);

}

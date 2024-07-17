package com.alibaba.druid.analysis.meta;

import com.alibaba.druid.analysis.meta.constants.TableLabel;
import com.alibaba.druid.analysis.meta.constants.View;
import com.alibaba.druid.analysis.meta.model.MetaDBColumn;
import com.alibaba.druid.analysis.meta.model.MetaDBInfo;
import com.alibaba.druid.analysis.meta.model.MetaDBTable;
import com.alibaba.druid.analysis.meta.model.Pk;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author LENOVO
 * @date 2024/6/14 16:48
 */
@Slf4j
public class DefaultDBMetaService implements DBMetaService {

    private MetaDBInfo metaDBInfo;
    private DataSource dataSource;

    public DefaultDBMetaService(MetaDBInfo metaDBInfo) {
        this.metaDBInfo = metaDBInfo;
    }

    public DefaultDBMetaService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<MetaDBTable> getTables(String tableNamePattern, String... types) {
        return selectTable(tableNamePattern, types);
    }

    @Override
    public List<MetaDBTable> getTableInfo(String tableNamePattern) {
        return selectTable(tableNamePattern, (String) null);
    }

    @Override
    public List<MetaDBTable> getTables(String tableNamePattern, String columnNamePattern, boolean ignoreColumn, String... types) {
        ConnectionHolder holder = null;
        List<MetaDBTable> metaDbTableList = new ArrayList<>();
        if (types == null || types.length == 0) {
            types = new String[]{View.TABLE};
        }
        try {
            holder = MetaDBHelper.getConnection(metaDBInfo);
            Connection conn = holder.getConnection();
            String catalog = conn.getCatalog();
            String schema = conn.getSchema();
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tableResultSet = metaData.getTables(catalog, schema, tableNamePattern, types);

            while (tableResultSet.next()) {
                String tableName = tableResultSet.getString(TableLabel.TABLE_NAME);
                MetaDBTable table = MetaDBTable.create(tableName);
                // 获得主键
                try (ResultSet rs = metaData.getPrimaryKeys(catalog, schema, tableName)) {
                    if (null != rs) {
                        List<Pk> pks = new ArrayList<>();
                        while (rs.next()) {
                            Pk pk = Pk.create(rs);
                            pks.add(pk);
                            table.addPk(pk.getColumnName());
                        }
                        table.setPk(pks.stream().collect(Collectors.groupingBy(Pk::getPkName)));
                    }
                }
                table.setTableCat(tableResultSet.getString(TableLabel.TABLE_CAT));
                table.setTableSchema(tableResultSet.getString(TableLabel.TABLE_SCHEM));
                table.setTableType(tableResultSet.getString(TableLabel.TABLE_TYPE));
                String tableRemark = tableResultSet.getString(TableLabel.REMARKS);
                table.setRemarks(tableRemark == null ? "" : tableRemark);
                if (!ignoreColumn) {
                    try (ResultSet rs = metaData.getColumns(catalog, schema, tableName, columnNamePattern)) {
                        if (rs != null) {
                            while (rs.next()) {
                                table.setColumn(MetaDBColumn.create(table, tableName, rs));
                            }
                        }
                    }
                }
                metaDbTableList.add(table);
            }
            MetaDBHelper.releaseResultSet(tableResultSet);
            return metaDbTableList;
        } catch (SQLException ex) {
            log.error("获取table信息失败:{}", ex.getMessage(), ex);
        } finally {
            MetaDBHelper.releaseConnection(holder);
        }
        return metaDbTableList;
    }


    private List<MetaDBTable> selectTable(String tableNamePattern, String... types) {
        ConnectionHolder holder = null;
        List<MetaDBTable> metaDbTableList = new ArrayList<>();
        try {
            if (dataSource != null) {
                holder = MetaDBHelper.getConnectionFromDs(dataSource);
            } else {
                holder = MetaDBHelper.getConnection(metaDBInfo);
            }
            if (holder == null || holder.getConnection() == null) {
                throw new RuntimeException("connection is not available.");
            }
            Connection conn = holder.getConnection();
            String catalog = conn.getCatalog();
            String schema = conn.getSchema();
            DatabaseMetaData metaData = conn.getMetaData();
            if (types == null || types.length == 0) {
                types = new String[]{View.TABLE};
            }
            ResultSet tableResultSet = metaData.getTables(catalog, schema, tableNamePattern, types);
            while (tableResultSet.next()) {
                String tableName = tableResultSet.getString(TableLabel.TABLE_NAME);
                String tableCat = tableResultSet.getString(TableLabel.TABLE_CAT);
                String tableSchema = tableResultSet.getString(TableLabel.TABLE_SCHEM);
                String tableType = tableResultSet.getString(TableLabel.TABLE_TYPE);
                String tableRemark = tableResultSet.getString(TableLabel.REMARKS);
                MetaDBTable table = new MetaDBTable(tableName, tableCat, tableSchema, tableType, tableRemark == null ? "" : tableRemark);
                metaDbTableList.add(table);
            }
            MetaDBHelper.releaseResultSet(tableResultSet);
            return metaDbTableList;
        } catch (SQLException ex) {
            log.error("获取table信息失败:{}", ex.getMessage(), ex);
        } finally {
            MetaDBHelper.releaseConnection(holder);
        }
        return metaDbTableList;
    }

    @Override
    public List<MetaDBColumn> getColumns(String tableName, String columnName) {
        ConnectionHolder holder = null;
        List<MetaDBColumn> metaDBColumns = new ArrayList<>();
        try {
            holder = MetaDBHelper.getConnection(metaDBInfo);
            Connection conn = holder.getConnection();
            String catalog = conn.getCatalog();
            String schema = conn.getSchema();
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getColumns(catalog, schema, tableName, columnName)) {
                while (rs.next()) {
                    MetaDBColumn metaDBColumn = MetaDBColumn.create(null, tableName, rs);
                    metaDBColumns.add(metaDBColumn);
                }
            }
            return metaDBColumns;
        } catch (SQLException ex) {
            log.error("获取column信息失败:{}", ex.getMessage(), ex);
        } finally {
            MetaDBHelper.releaseConnection(holder);
        }
        return metaDBColumns;
    }
}

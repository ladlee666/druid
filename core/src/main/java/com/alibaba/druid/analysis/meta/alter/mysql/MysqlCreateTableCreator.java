package com.alibaba.druid.analysis.meta.alter.mysql;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLDataType;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableItem;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.analysis.meta.MetaDBHelper;
import com.alibaba.druid.analysis.meta.alter.AlterCreator;
import com.alibaba.druid.analysis.meta.alter.AlterMode;
import com.alibaba.druid.analysis.meta.model.MetaDBColumn;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LENOVO
 * @date 2024/6/26 10:38
 */
public class MysqlCreateTableCreator extends AlterCreator {

    public MysqlCreateTableCreator(List<MetaDBColumn> columns) {
        super(columns);
    }

    @Override
    protected SQLDataType createDataType(MetaDBColumn metaDBColumn) {
        throw new UnsupportedOperationException("Operation not allowed.");
    }

    @Override
    protected SQLColumnDefinition createColumnDefinition(MetaDBColumn metaDBColumn) {
        throw new UnsupportedOperationException("Operation not allowed.");
    }

    @Override
    protected List<SQLAlterTableItem> getAlterItems(List<MetaDBColumn> metaDBColumns) {
        throw new UnsupportedOperationException("Operation not allowed.");
    }

    @Override
    protected String getCreateTableScript(Connection connection, List<String> tables) {
        List<String> ddlList = getTableDDL(connection, tables);
        StringBuilder buf = new StringBuilder();
        for (String ddl : ddlList) {
            buf.append(ddl);
            buf.append(';');
        }
        return buf.toString();
    }

    @Override
    protected DbType getDbType() {
        return DbType.mysql;
    }

    @Override
    protected AlterMode getMode() {
        return AlterMode.CREATE;
    }

    private List<String> getTableDDL(Connection conn, List<String> tables) {
        List<String> ddlList = new ArrayList<String>();

        Statement stmt = null;
        try {
            for (String table : tables) {
                if (stmt == null) {
                    stmt = conn.createStatement();
                }

                ResultSet rs = null;
                try {
                    rs = stmt.executeQuery("show create table " + table);
                    if (rs.next()) {
                        String ddl = rs.getString(2);
                        ddlList.add(ddl);
                    }
                } finally {
                    MetaDBHelper.releaseResultSet(rs);
                }
            }
        } catch (Exception ex) {
            System.err.println("Get table ddl exception:" + ex.getMessage());
        } finally {
            MetaDBHelper.releaseStmt(stmt);
        }

        return ddlList;
    }

//    public static void main(String[] args) throws ClassNotFoundException, SQLException {
//        String url = MetaUtil.getConnectionUrl("mysql", "10.10.10.112", 3306L, "wisdomHospital-prod");
//        String driverClassName = MetaUtil.getDriverClassName(url);
//        MetaDBInfo metaDBInfo = MetaDBInfo.builder()
//                .dbType("mysql")
//                .url(url)
//                .driverClass(driverClassName)
//                .userName("root")
//                .password("root")
//                .build();
//
//        String ddl = AlterLoader.printDDL(metaDBInfo, "tb_registered");
//        System.out.println(ddl);
//    }

}

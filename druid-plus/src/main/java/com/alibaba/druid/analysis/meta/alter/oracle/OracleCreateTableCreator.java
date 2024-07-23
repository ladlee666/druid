package com.alibaba.druid.analysis.meta.alter.oracle;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLDataType;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.analysis.meta.MetaDBHelper;
import com.alibaba.druid.analysis.meta.alter.AlterCreator;
import com.alibaba.druid.analysis.meta.alter.AlterMode;
import com.alibaba.druid.analysis.meta.constants.ColumnLabel;
import com.alibaba.druid.analysis.meta.constants.TableLabel;
import com.alibaba.druid.analysis.meta.constants.View;
import com.alibaba.druid.analysis.meta.model.MetaDBColumn;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LENOVO
 * @date 2024/6/26 9:58
 */
public class OracleCreateTableCreator extends AlterCreator {

    public OracleCreateTableCreator(List<MetaDBColumn> columns) {
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
        return DbType.oracle;
    }

    @Override
    protected AlterMode getMode() {
        return AlterMode.CREATE;
    }

    private List<String> getTableDDL(Connection connection, List<String> tables) {
        List<String> ddlList = new ArrayList<String>();

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select DBMS_METADATA.GET_DDL('TABLE', TABLE_NAME) FROM user_tables");

            if (tables.size() > 0) {
                sql.append(" WHERE TABLE_NAME IN (");
                for (int i = 0; i < tables.size(); ++i) {
                    if (i != 0) {
                        sql.append(", ?");
                    } else {
                        sql.append("?");
                    }
                }
                sql.append(")");
            }
            pstmt = connection.prepareStatement(sql.toString());
            for (int i = 0; i < tables.size(); ++i) {
                pstmt.setString(i + 1, tables.get(i));
            }
            rs = pstmt.executeQuery();
            while (rs.next()) {
                String ddl = rs.getString(1);
                ddlList.add(ddl);
            }
            for (String tableName : tables) {
                // 获取元数据生成备注
                setComments(connection, tableName, ddlList);
            }
        } catch (Exception ex) {
            System.err.println("Get table ddl exception:" + ex.getMessage());
        } finally {
            MetaDBHelper.releaseResultSet(rs);
            MetaDBHelper.releaseStmt(pstmt);
        }

        return ddlList;
    }

    private void setComments(Connection connection, String dbTableName, List<String> ddlList) throws SQLException {
        List<SQLStatement> stmts = new ArrayList<>();
        String catalog = connection.getCatalog();
        String schema = connection.getSchema();
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet tableResultSet = metaData.getTables(catalog, schema, dbTableName, new String[]{View.TABLE});
        while (tableResultSet.next()) {
            String tableSchem = tableResultSet.getString(TableLabel.TABLE_SCHEM);
            String tableName = tableResultSet.getString(TableLabel.TABLE_NAME);
            String tableComment = tableResultSet.getString(TableLabel.REMARKS);
            if (StringUtils.isNotBlank(tableComment)) {
                SQLStatement tableStmt = createCommentStatement(tableSchem, tableName, null, tableComment);
                stmts.add(tableStmt);
            }
            try (ResultSet rs = metaData.getColumns(catalog, schema, tableName, null)) {
                if (rs != null) {
                    while (rs.next()) {
                        String columnName = rs.getString(ColumnLabel.COLUMN_NAME);
                        String columnComment = rs.getString(ColumnLabel.REMARKS);
                        if (StringUtils.isNotBlank(columnComment)) {
                            SQLStatement columnStmt = createCommentStatement(tableSchem, tableName, columnName, columnComment);
                            stmts.add(columnStmt);
                        }
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(stmts)) {
            String comments = SQLUtils.toSQLString(stmts, DbType.oracle);
            ddlList.add(comments);
        }
    }

    /**
     * 备注信息
     *
     * @param tableSchem 库
     * @param tableName  表
     * @param columnName 字段
     * @param comment    备注
     * @return
     */
    private SQLStatement createCommentStatement(String tableSchem, String tableName, String columnName, String comment) {
        SQLCommentStatement commentStatement = new SQLCommentStatement();
        commentStatement.setComment(new SQLCharExpr(comment));
        if (columnName == null) {
            commentStatement.setType(SQLCommentStatement.Type.TABLE);
            SQLPropertyExpr expr = new SQLPropertyExpr(new SQLIdentifierExpr(tableSchem), tableName);
            commentStatement.setOn(new SQLExprTableSource(expr));
        } else {
            commentStatement.setType(SQLCommentStatement.Type.COLUMN);
            SQLPropertyExpr expr = new SQLPropertyExpr(new SQLPropertyExpr(new SQLIdentifierExpr(tableSchem), tableName), columnName);
            commentStatement.setOn(new SQLExprTableSource(expr));
        }
        commentStatement.setAfterSemi(true);
        return commentStatement;
    }

}

package com.alibaba.druid.analysis.meta.alter.oracle;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLDataType;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableItem;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.sql.ast.statement.SQLCommentStatement;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.analysis.meta.alter.AlterCreator;
import com.alibaba.druid.analysis.meta.alter.AlterMode;
import com.alibaba.druid.analysis.meta.alter.chain.AlterContext;
import com.alibaba.druid.analysis.meta.model.MetaDBColumn;
import com.alibaba.druid.analysis.meta.model.extend.DbTable;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * oracle修改字段注释: COMMENT ON COLUMN employees.employee_id IS 'Unique identifier for an employee';
 * oracle修改表注释:  COMMENT ON TABLE employees IS 'Table stores employee information';
 *
 * @author LENOVO
 * @date 2024/7/1 11:20
 */
public class OracleCommentCreator extends AlterCreator {

    public OracleCommentCreator(List<MetaDBColumn> columns) {
        super(columns);
    }

    public OracleCommentCreator(AlterContext context) {
        super(context.getMetaDBColumns());
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
    public List<SQLStatement> createStatement(List<MetaDBColumn> columns, String tableName, DbType dbType) {
        if (StringUtils.isBlank(tableName)) {
            return null;
        }
        List<SQLStatement> stmts = new ArrayList<>();
        for (MetaDBColumn metaDBColumn : columns) {
            if (StringUtils.isBlank(metaDBColumn.getRemarks())) {
                continue;
            }
            SQLCommentStatement commentStatement = new SQLCommentStatement();
            commentStatement.setComment(new SQLCharExpr(metaDBColumn.getRemarks()));
            if (metaDBColumn instanceof DbTable) {
                commentStatement.setType(SQLCommentStatement.Type.TABLE);
                SQLIdentifierExpr expr = new SQLIdentifierExpr(tableName);
                commentStatement.setOn(new SQLExprTableSource(expr));
            } else {
                commentStatement.setType(SQLCommentStatement.Type.COLUMN);
                SQLPropertyExpr expr = new SQLPropertyExpr(new SQLIdentifierExpr(tableName), metaDBColumn.getColumnName());
                commentStatement.setOn(new SQLExprTableSource(expr));
            }
            commentStatement.setAfterSemi(true);
            stmts.add(commentStatement);
        }
        return stmts;
    }

    @Override
    protected DbType getDbType() {
        return DbType.oracle;
    }

    @Override
    protected AlterMode getMode() {
        return AlterMode.MODIFY;
    }

}

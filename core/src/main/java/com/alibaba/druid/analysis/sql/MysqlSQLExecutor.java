package com.alibaba.druid.analysis.sql;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLLimit;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.analysis.spi.Service;
import com.alibaba.druid.analysis.sql.model.ExecuteContext;


@Service(name = "mysql")
public class MysqlSQLExecutor extends AbstractSQLExecutor {

    @Override
    public void page(SQLStatement target, ExecuteContext context) {
        int count = getCount(context.getParam());
        int offset = getOffset(context.getParam());

        SQLSelectStatement selectStmt = (SQLSelectStatement) target;
        SQLSelect select = selectStmt.getSelect();

        SQLSelectQueryBlock queryBlock = (SQLSelectQueryBlock) select.getQuery();

        SQLLimit limit = queryBlock.getLimit();
        if (limit != null) {
            if (offset > 0) {
                limit.setOffset(new SQLIntegerExpr(offset));
                limit.setRowCount(new SQLIntegerExpr(count));
            }
        }

        if (limit == null) {
            limit = new SQLLimit();
            if (offset > 0) {
                limit.setOffset(new SQLIntegerExpr(offset));
            }
            limit.setRowCount(new SQLIntegerExpr(count));
            queryBlock.setLimit(limit);
        }
        select.setQuery(queryBlock);
    }

    @Override
    public SQLStatement count(SQLStatement target, ExecuteContext context) {
        SQLStatement countSqlStatement = target.clone();
        return count(countSqlStatement, DbType.mysql);
    }

    @Override
    public SQLSelectQueryBlock buildQuery(String alias, String columnName, SQLSelect select, SQLExpr sqlExpr) {
        MySqlSelectQueryBlock newBlock = (MySqlSelectQueryBlock) createQueryBlock();
        newBlock.getSelectList().add(new SQLSelectItem(new SQLPropertyExpr(new SQLIdentifierExpr(alias), columnName)));
//        newBlock.setFrom(new SQLSubqueryTableSource(new SQLSelect(selectQueryBlock), alias));
        newBlock.setFrom(new SQLSubqueryTableSource(select.clone(), alias));
        newBlock.setWhere(sqlExpr);
        return newBlock;
    }

    @Override
    public SQLSelectQueryBlock createQueryBlock() {
        return new MySqlSelectQueryBlock();
    }

    @Override
    public DbType getDbType() {
        return DbType.mysql;
    }

}

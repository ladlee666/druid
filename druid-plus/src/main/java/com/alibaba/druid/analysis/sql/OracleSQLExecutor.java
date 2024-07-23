package com.alibaba.druid.analysis.sql;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleSelectQueryBlock;
import com.alibaba.druid.analysis.spi.Service;
import com.alibaba.druid.analysis.sql.model.ExecuteContext;

import static com.alibaba.druid.analysis.sql.Constants.*;


@Service(name = "oracle")
public class OracleSQLExecutor extends AbstractSQLExecutor {

    public OracleSQLExecutor() {
    }

    @Override
    public void page(SQLStatement target, ExecuteContext context) {
        Integer offset = getOffset(context.getParam());
        Integer count = getCount(context.getParam());
        SQLSelectStatement selectStmt = (SQLSelectStatement) target;
        SQLSelect select = selectStmt.getSelect();
        SQLSelectQuery query = select.getQuery();
        if (query instanceof SQLSelectQueryBlock) {
            OracleSelectQueryBlock queryBlock = (OracleSelectQueryBlock) query;
            SQLOrderBy orderBy = select.getOrderBy();
            if (orderBy == null && queryBlock.getOrderBy() != null) {
                orderBy = queryBlock.getOrderBy();
            }

            if (queryBlock.getGroupBy() == null && orderBy == null && offset <= 0) {
                SQLExpr condition = new SQLBinaryOpExpr(new SQLIdentifierExpr(ORACLE_ROWNUM_NAME),
                        SQLBinaryOperator.LessThanOrEqual,
                        new SQLNumberExpr(count),
                        DbType.oracle);
                if (queryBlock.getWhere() == null) {
                    queryBlock.setWhere(condition);
                } else {
                    queryBlock.setWhere(new SQLBinaryOpExpr(queryBlock.getWhere(),
                            SQLBinaryOperator.BooleanAnd,
                            condition,
                            DbType.oracle));
                }
                return;
            }
        }

        OracleSelectQueryBlock countQueryBlock = (OracleSelectQueryBlock) createQueryBlock();
        countQueryBlock.getSelectList().add(new SQLSelectItem(new SQLPropertyExpr(new SQLIdentifierExpr("XX"), ALL_COLUMN)));
        countQueryBlock.getSelectList().add(new SQLSelectItem(new SQLIdentifierExpr(ORACLE_ROWNUM_NAME), ORACLE_RN_NAME));

//        countQueryBlock.setFrom(new SQLSubqueryTableSource(select.clone(), "XX"));
        countQueryBlock.setFrom(new SQLSubqueryTableSource(select.getQuery(), "XX"));
        countQueryBlock.setWhere(new SQLBinaryOpExpr(new SQLIdentifierExpr(ORACLE_ROWNUM_NAME),
                SQLBinaryOperator.LessThanOrEqual,
                new SQLNumberExpr(count + offset),
                DbType.oracle));

        select.setOrderBy(null);
        if (offset <= 0) {
            select.setQuery(countQueryBlock);
        }

        OracleSelectQueryBlock offsetQueryBlock = (OracleSelectQueryBlock) createQueryBlock();
        offsetQueryBlock.getSelectList().add(new SQLSelectItem(new SQLAllColumnExpr()));
        offsetQueryBlock.setFrom(new SQLSubqueryTableSource(new SQLSelect(countQueryBlock), "XXX"));
        offsetQueryBlock.setWhere(new SQLBinaryOpExpr(new SQLIdentifierExpr(ORACLE_RN_NAME),
                SQLBinaryOperator.GreaterThan,
                new SQLNumberExpr(offset),
                DbType.oracle));

        select.setQuery(offsetQueryBlock);
    }

    @Override
    public SQLStatement count(SQLStatement target, ExecuteContext context) {
//        SQLStatement countSqlStatement = target.clone();
        return count(target, DbType.oracle);
    }

    @Override
    public SQLSelectQueryBlock buildQuery(String alias, String columnName, SQLSelect select, SQLExpr sqlExpr) {
        OracleSelectQueryBlock newBlock = (OracleSelectQueryBlock) createQueryBlock();
        newBlock.getSelectList().add(new SQLSelectItem(new SQLPropertyExpr(new SQLIdentifierExpr(alias), columnName)));
        newBlock.setFrom(new SQLSubqueryTableSource(select.clone(), alias));
        newBlock.setWhere(sqlExpr);
        return newBlock;
    }

    @Override
    public SQLSelectQueryBlock createQueryBlock() {
        return new OracleSelectQueryBlock();
    }

    @Override
    public DbType getDbType() {
        return DbType.oracle;
    }

}

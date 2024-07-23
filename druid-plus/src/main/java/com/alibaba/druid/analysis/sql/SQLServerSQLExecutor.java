package com.alibaba.druid.analysis.sql;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.*;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.dialect.sqlserver.ast.SQLServerSelectQueryBlock;
import com.alibaba.druid.sql.dialect.sqlserver.ast.SQLServerTop;
import com.alibaba.druid.analysis.spi.Service;
import com.alibaba.druid.analysis.sql.model.ExecuteContext;

import static com.alibaba.druid.analysis.sql.Constants.ALL_COLUMN;


@Service(name = "sqlserver")
public class SQLServerSQLExecutor extends AbstractSQLExecutor {

    private final String SQLSERVER_PAGE_NAME = "ROW_NUMBER";

    @Override
    public void page(SQLStatement target, ExecuteContext context) {
        int count = getCount(context.getParam());
        int offset = getOffset(context.getParam());

        SQLSelectStatement selectStmt = (SQLSelectStatement) target;
        SQLSelect select = selectStmt.getSelect();

        SQLBinaryOpExpr gtExpr = new SQLBinaryOpExpr(new SQLIdentifierExpr("RN"),
                SQLBinaryOperator.GreaterThan, new SQLNumberExpr(offset), DbType.sqlserver);
        SQLBinaryOpExpr lteqExpr = new SQLBinaryOpExpr(new SQLIdentifierExpr("RN"),
                SQLBinaryOperator.LessThanOrEqual, new SQLNumberExpr(count + offset), DbType.sqlserver);
        SQLBinaryOpExpr pageCondition = new SQLBinaryOpExpr(gtExpr, SQLBinaryOperator.BooleanAnd, lteqExpr,
                DbType.sqlserver);

        SQLSelectQuery query = select.getQuery();
        if (query instanceof SQLSelectQueryBlock) {
            SQLServerSelectQueryBlock queryBlock = (SQLServerSelectQueryBlock) query;
            if (offset < 0) {
                queryBlock.setTop(new SQLServerTop(new SQLNumberExpr(count)));
            }

            SQLAggregateExpr aggregateExpr = new SQLAggregateExpr(SQLSERVER_PAGE_NAME);
            SQLOrderBy orderBy = select.getOrderBy();
            if (orderBy != null) {
                aggregateExpr.setOver(new SQLOver(orderBy));
                select.setOrderBy(null);
            } else if (queryBlock.getOrderBy() != null) {
                aggregateExpr.setOver(new SQLOver(queryBlock.getOrderBy()));
                queryBlock.setOrderBy(null);
            } else {
                SQLOrderBy orderByTime = new SQLOrderBy(new SQLCurrentTimeExpr(SQLCurrentTimeExpr.Type.CURRENT_TIMESTAMP));
                aggregateExpr.setOver(new SQLOver(orderByTime));
            }
            clearOrderBy(queryBlock);
            queryBlock.getSelectList().add(new SQLSelectItem(aggregateExpr, "RN"));
            SQLSelectQueryBlock countQueryBlock = buildQuery("XX", ALL_COLUMN, select.clone(), pageCondition);
            select.setQuery(countQueryBlock);
        } else {
            SQLServerSelectQueryBlock countQueryBlock = (SQLServerSelectQueryBlock) createQueryBlock();
            countQueryBlock.getSelectList().add(new SQLSelectItem(new SQLPropertyExpr(new SQLIdentifierExpr("XX"), ALL_COLUMN)));
            countQueryBlock.setFrom(new SQLSubqueryTableSource(select.clone(), "XX"));

            if (offset < 0) {
                countQueryBlock.setTop(new SQLServerTop(new SQLNumberExpr(count)));
                select.setQuery(countQueryBlock);
            } else {
                SQLAggregateExpr aggregateExpr = new SQLAggregateExpr(SQLSERVER_PAGE_NAME);
                SQLOrderBy orderBy = select.getOrderBy();
                if (orderBy != null) {
                    aggregateExpr.setOver(new SQLOver(orderBy));
                    select.setOrderBy(null);
                } else {
                    SQLOrderBy orderByTime = new SQLOrderBy(new SQLCurrentTimeExpr(SQLCurrentTimeExpr.Type.CURRENT_TIMESTAMP));
                    aggregateExpr.setOver(new SQLOver(orderByTime));
                }

                countQueryBlock.getSelectList().add(new SQLSelectItem(aggregateExpr, "RN"));

                SQLServerSelectQueryBlock offsetQueryBlock = (SQLServerSelectQueryBlock) createQueryBlock();
                offsetQueryBlock.getSelectList().add(new SQLSelectItem(new SQLAllColumnExpr()));
                offsetQueryBlock.setFrom(new SQLSubqueryTableSource(new SQLSelect(countQueryBlock), "XXX"));
                offsetQueryBlock.setWhere(pageCondition);

                select.setQuery(offsetQueryBlock);
            }
        }
    }

    @Override
    public SQLStatement count(SQLStatement target, ExecuteContext param) {
        SQLStatement countSqlStatement = target.clone();
        return count(countSqlStatement, DbType.sqlserver);
    }

    @Override
    public SQLSelectQueryBlock buildQuery(String alias, String columnName, SQLSelect select, SQLExpr sqlExpr) {
        SQLServerSelectQueryBlock newBlock = (SQLServerSelectQueryBlock) createQueryBlock();
        newBlock.getSelectList().add(new SQLSelectItem(new SQLPropertyExpr(new SQLIdentifierExpr(alias), columnName)));
        newBlock.setFrom(new SQLSubqueryTableSource(select.clone(), alias));
        newBlock.setWhere(sqlExpr);
        return newBlock;
    }

    @Override
    public SQLSelectQueryBlock createQueryBlock() {
        return new SQLServerSelectQueryBlock();
    }

    @Override
    public DbType getDbType() {
        return DbType.sqlserver;
    }

}

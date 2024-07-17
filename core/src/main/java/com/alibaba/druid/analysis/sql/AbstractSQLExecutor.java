package com.alibaba.druid.analysis.sql;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLDataTypeImpl;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLSetQuantifier;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import com.alibaba.druid.analysis.sql.model.ExecuteActionParam;
import com.alibaba.druid.analysis.sql.model.ExecuteContext;
import com.alibaba.druid.analysis.sql.visitor.CustomVisitor;

import java.util.List;
import java.util.Map;

import static cn.hutool.core.util.RandomUtil.BASE_CHAR;

public abstract class AbstractSQLExecutor implements SQLExecutor<ExecuteContext, SQLStatement> {

    public abstract DbType getDbType();

    @Override
    public void where(SQLStatement target, ExecuteContext context) {
        if (!isSQLSelectStatement(target)) {
            return;
        }
        ExecuteActionParam param = context.getParam();
        if (CollUtil.isEmpty(param.getWhere()) && CollUtil.isEmpty(param.getBetween())
                && CollUtil.isEmpty(param.getDateBetween())) {
            return;
        }

        String alias = RandomUtil.randomString(BASE_CHAR, 3);
        SQLSelectStatement selectStmt = (SQLSelectStatement) target;
        SQLSelect select = selectStmt.getSelect();
        SQLSelectQueryBlock whereQueryBlock = createQueryBlock();
        whereQueryBlock.getSelectList().add(new SQLSelectItem(new SQLPropertyExpr(new SQLIdentifierExpr(alias), Constants.ALL_COLUMN)));
        whereQueryBlock.setFrom(new SQLSubqueryTableSource(new SQLSelect(select.getQuery()), alias));

        whereQueryBlock.addWhere(setWhere(alias, param.getWhere()));
        whereQueryBlock.addWhere(setBetween(alias, param.getBetween()));
        whereQueryBlock.addWhere(setDateBetween(alias, getDbType(), param.getDateBetween()));

        if (whereQueryBlock.getWhere() != null) {
            select.setQuery(whereQueryBlock);
        }
    }

    @Override
    public void parse(SQLStatement target, ExecuteContext context, SQLASTVisitor visitor) {
        accept(target, visitor);

    }

    @Override
    public void auth(SQLStatement target, ExecuteContext context, SQLASTVisitor visitor) {
        if (!isSQLSelectStatement(target)) {
            return;
        }
        SQLSelectStatement selectStatement = (SQLSelectStatement) target;
        SQLSelect select = selectStatement.getSelect();
        if (select == null || select.getQueryBlock() == null) {
            return;
        }
        accept(target, visitor);
    }

    protected Integer getOffset(ExecuteActionParam param) {
        int page = param.getPage() == null ? 1 : param.getPage();
        int size = param.getSize() == null ? 10 : param.getSize();
        return (page - 1) * size;
    }

    protected Integer getCount(ExecuteActionParam param) {
        return (param.getSize() == null ? 10 : param.getSize());
    }

    protected SQLExpr setWhere(String tableAlias, Map<String, Object> whereParams) {
        if (CollUtil.isEmpty(whereParams)) {
            return null;
        }
        SQLExpr expr = null;
        for (Map.Entry<String, Object> entry : whereParams.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (StrUtil.isBlank(key) || ObjectUtil.isNull(value)) {
                continue;
            }
            SQLBinaryOpExpr opExpr = new SQLBinaryOpExpr(new SQLPropertyExpr(new SQLIdentifierExpr(tableAlias), key),
                    SQLBinaryOperator.Like,
                    new SQLCharExpr("%" + value + "%"));
            expr = SQLBinaryOpExpr.and(expr, opExpr);
        }
        return expr;
    }


    protected SQLExpr setDateBetween(String tableAlias, DbType dbType, Map<String, List<Map<String, Object>>> dateBetween) {
        if (CollUtil.isEmpty(dateBetween)) {
            return null;
        }
        SQLExpr expr = null;
        for (Map.Entry<String, List<Map<String, Object>>> entry : dateBetween.entrySet()) {
            String key = entry.getKey();
            List<Map<String, Object>> dateList = entry.getValue();
            if (StrUtil.isBlank(key) || CollUtil.isEmpty(dateList)) {
                continue;
            }
            SQLPropertyExpr keyExpr = new SQLPropertyExpr(new SQLIdentifierExpr(tableAlias), key);
            for (Map<String, Object> singleDate : dateList) {
                SQLExpr beginExpr;
                SQLExpr endExpr;
                if (dbType == DbType.sqlserver) {
                    beginExpr = new SQLCastExpr(new SQLCharExpr(StrUtil.toString(singleDate.get(START))),
                            new SQLDataTypeImpl("DATETIME"));
                    endExpr = new SQLCastExpr(new SQLCharExpr(StrUtil.toString(singleDate.get(END))),
                            new SQLDataTypeImpl("DATETIME"));
                } else if (dbType == DbType.oracle || dbType == DbType.mysql) {
                    beginExpr = new SQLDateExpr(StrUtil.toString(singleDate.get(START)));
                    endExpr = new SQLDateExpr(StrUtil.toString(singleDate.get(END)));
                } else {
                    throw new IllegalArgumentException("无效的数据库类型");
                }
                SQLBetweenExpr betweenExpr = new SQLBetweenExpr(keyExpr, beginExpr, endExpr);
                expr = SQLBinaryOpExpr.and(expr, betweenExpr);
            }
        }
        return expr;
    }

//    public static void main(String[] args) {
//        SQLPropertyExpr keyExpr = new SQLPropertyExpr(new SQLIdentifierExpr("alias"), "create_time");
//        SQLCharExpr format = new SQLCharExpr("yyyy-MM-dd HH24:mi:ss");
//        SQLExpr beginExpr = new SQLMethodInvokeExpr("to_date", null, new SQLCharExpr("2024-04-23 10:27:44"), format);
//        SQLExpr endExpr = new SQLMethodInvokeExpr("to_date", null, new SQLCharExpr("2024-04-30 10:28:51"), format);
//        SQLBetweenExpr betweenExpr = new SQLBetweenExpr(keyExpr, beginExpr, endExpr);
//        System.out.println(betweenExpr);
//
//        String sql = "select * from user where create_time between to_date('2024-03-25','yyyy-mm-dd') and to_date('2024-03-26','yyyy-mm-dd')";
//        SQLStatement sqlStatement = SQLUtils.parseSingleStatement(sql, DbType.oracle);
//        System.out.println(sqlStatement);
//    }

    protected SQLExpr setBetween(String tableAlias, Map<String, Map<String, Object>> between) {
        if (CollUtil.isEmpty(between)) {
            return null;
        }
        SQLExpr expr = null;
        for (Map.Entry<String, Map<String, Object>> entry : between.entrySet()) {
            String key = entry.getKey();
            Map<String, Object> betweenMap = entry.getValue();
            if (CollUtil.isEmpty(betweenMap)) {
                continue;
            }
            SQLPropertyExpr keyExpr = new SQLPropertyExpr(new SQLIdentifierExpr(tableAlias), key);
            SQLCharExpr beginExpr = new SQLCharExpr(StrUtil.toString(betweenMap.get(START)));
            SQLCharExpr endExpr = new SQLCharExpr(StrUtil.toString(betweenMap.get(END)));
            SQLBetweenExpr betweenExpr = new SQLBetweenExpr(keyExpr, beginExpr, endExpr);
            expr = SQLBinaryOpExpr.and(expr, betweenExpr);
        }
        return expr;
    }

    protected boolean isSQLSelectStatement(SQLStatement sqlStatement) {
        return (sqlStatement instanceof SQLSelectStatement);
    }

    protected void accept(SQLStatement target, SQLASTVisitor visitor) {
        CustomVisitor customVisitor = (CustomVisitor) visitor;
        if (customVisitor == null) {
            return;
        }
        if (!customVisitor.initStats()) {
            target.accept(visitor);
            ((CustomVisitor) visitor).init();
        }
    }

    public SQLSelectStatement count(SQLStatement sqlStatement, DbType dbType) {
        if (!(sqlStatement instanceof SQLSelectStatement)) {
            throw new IllegalArgumentException("不支持的分页类型SQL：" + sqlStatement);
        }
        SQLSelectStatement selectStatement = (SQLSelectStatement) sqlStatement;
        SQLSelect select = selectStatement.getSelect();
        if (select.getOrderBy() != null) {
            select.setOrderBy(null);
        }
        SQLSelectQuery query = select.getQuery();
        clearOrderBy(query);

        if (query instanceof SQLSelectQueryBlock) {
            SQLSelectQueryBlock queryBlock = (SQLSelectQueryBlock) query;

            if (queryBlock.getGroupBy() != null && queryBlock.getGroupBy().getItems().size() > 0) {
                if (queryBlock.getSelectList().size() == 1 &&
                        (queryBlock.getSelectList().get(0).getExpr() instanceof SQLAllColumnExpr)) {
                    queryBlock.getSelectList().clear();
                    queryBlock.getSelectList().add(new SQLSelectItem(new SQLIntegerExpr(1)));
                }
                return createCountBySubQuery(select, dbType);
            }

            List<SQLSelectItem> selectList = queryBlock.getSelectList();
            int option = queryBlock.getDistionOption();
            if (option == SQLSetQuantifier.DISTINCT && selectList.size() >= 1) {


//                SQLAggregateExpr countExpr = new SQLAggregateExpr("COUNT", SQLAggregateOption.DISTINCT);
//                for (SQLSelectItem sqlSelectItem : selectList) {
//                    countExpr.addArgument(sqlSelectItem.getExpr());
//                }
//                selectList.clear();
//                queryBlock.setDistionOption(0);
//                queryBlock.addSelectItem(countExpr);

                return createCountBySubQuery(select, dbType);


            } else {
                selectList.clear();
                selectList.add(createCountItem());
            }
            select.setQuery(queryBlock);
            return selectStatement;
        } else if (query instanceof SQLUnionQuery) {
            return createCountBySubQuery(select, dbType);
        }
        throw new IllegalStateException();
    }

    protected SQLSelectStatement createCountBySubQuery(SQLSelect select, DbType dbType) {
        SQLSelectQueryBlock countSelectQuery = createSelectBlock(select);

        SQLSelect countSelect = new SQLSelect(countSelectQuery);

        return new SQLSelectStatement(countSelect, dbType);
    }

    protected SQLSelectQueryBlock createSelectBlock(SQLSelect select) {
        SQLSelectQueryBlock countSelectQuery = createQueryBlock();
        SQLSelectItem countItem = createCountItem();
        countSelectQuery.getSelectList().add(countItem);

        SQLSubqueryTableSource fromSubQuery = new SQLSubqueryTableSource(select);
        fromSubQuery.setAlias("alias_count");
        countSelectQuery.setFrom(fromSubQuery);
        return countSelectQuery;
    }

    protected SQLSelectItem createCountItem() {
        SQLAggregateExpr countExpr = new SQLAggregateExpr("COUNT");
        countExpr.addArgument(new SQLAllColumnExpr());
        return new SQLSelectItem(countExpr);
    }

    public void clearOrderBy(SQLSelectQuery query) {
        if (query instanceof SQLSelectQueryBlock) {
            SQLSelectQueryBlock queryBlock = (SQLSelectQueryBlock) query;
            if (queryBlock.getOrderBy() != null) {
                queryBlock.setOrderBy(null);
            }
            SQLTableSource from = queryBlock.getFrom();
            if (from != null && from instanceof SQLSubqueryTableSource) {
                SQLSubqueryTableSource subQueryTableSource = (SQLSubqueryTableSource) from;
                SQLSelectQuery subQuery = subQueryTableSource.getSelect().getQuery();
                clearOrderBy(subQuery);
            }
            return;
        }
        if (query instanceof SQLUnionQuery) {
            SQLUnionQuery union = (SQLUnionQuery) query;
            if (union.getOrderBy() != null) {
                union.setOrderBy(null);
            }
            clearOrderBy(union.getLeft());
            clearOrderBy(union.getRight());
        }
    }

    public abstract SQLSelectQueryBlock buildQuery(String alias, String columnName, SQLSelect select, SQLExpr sqlExpr);

    public abstract SQLSelectQueryBlock createQueryBlock();
}

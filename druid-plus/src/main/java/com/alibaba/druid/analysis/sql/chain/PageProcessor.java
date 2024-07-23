package com.alibaba.druid.analysis.sql.chain;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.analysis.spi.EnhancedServiceLoader;
import com.alibaba.druid.analysis.sql.AbstractSQLExecutor;
import com.alibaba.druid.analysis.sql.SQLExecutor;
import com.alibaba.druid.analysis.sql.model.ChainContext;
import com.alibaba.druid.analysis.sql.model.ExecuteActionParam;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PageProcessor extends AbstractProcessor {

    private AbstractSQLExecutor sqlExecutor;

    private SQLStatement countStatement;

    @Override
    protected void before(ChainContext context) {
        sqlExecutor = (AbstractSQLExecutor) EnhancedServiceLoader.load(SQLExecutor.class, context.getDbType().name());
    }

    @Override
    protected void execute(ChainContext context) {
        ExecuteActionParam actionParam = context.getContext().getParam();
        if (null == actionParam.getEnablePage() || !actionParam.getEnablePage()) {
            return;
        }
        SQLStatement sqlStatement = context.getSqlStatement();
        if (!(sqlStatement instanceof SQLSelectStatement)) {
            return;
        }
//        SQLStatement countStmt = sqlStatement.clone();
//        sqlExecutor.page(sqlStatement, context.getContext());
//        countStatement = sqlExecutor.count(countStmt, context.getContext());

        String countSql = SQLUtils.toSQLString(sqlStatement, context.getDbType());
        sqlExecutor.page(sqlStatement, context.getContext());
        countStatement = sqlExecutor.count(SQLUtils.parseSingleStatement(countSql, context.getDbType()), context.getContext());
    }

    @Override
    protected void after(ChainContext context) {
        context.setCountStatement(getCountStatement());
    }

    public SQLStatement getCountStatement() {
        return countStatement;
    }
}

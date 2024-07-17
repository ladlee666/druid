package com.alibaba.druid.analysis.sql.chain;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlEvalVisitorImpl;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleEvalVisitor;
import com.alibaba.druid.sql.dialect.sqlserver.visitor.SQLServerEvalVisitor;
import com.alibaba.druid.sql.visitor.SQLEvalVisitor;
import com.alibaba.druid.sql.visitor.SQLEvalVisitorImpl;
import com.alibaba.druid.analysis.sql.method.AutoId;
import com.alibaba.druid.analysis.sql.model.ChainContext;

import javax.sql.DataSource;

public class AutoIdProcessor extends AbstractProcessor {

    @Override
    protected void before(ChainContext context) {

    }

    @Override
    protected void execute(ChainContext context) {
        SQLStatement sqlStatement = context.getSqlStatement();
        if (sqlStatement instanceof SQLInsertStatement) {
            DbType dbType = context.getDbType();
            SQLEvalVisitor evalVisitor = getVisitor(dbType);
            DataSource dataSource = context.getDataSource();
            AutoId autoId = new AutoId(dataSource, dbType.name());
            evalVisitor.registerFunction(autoId.functionName(), autoId);
            sqlStatement.accept(evalVisitor);
            context.setAutoIds(autoId.getResult());
        }
    }

    @Override
    protected void after(ChainContext context) {

    }

    protected SQLEvalVisitor getVisitor(DbType dbType) {
        SQLEvalVisitor evalVisitor;
        if (dbType == DbType.oracle) {
            evalVisitor = new OracleEvalVisitor();
        } else if (dbType == DbType.mysql) {
            evalVisitor = new MySqlEvalVisitorImpl();
        } else if (dbType == DbType.sqlserver) {
            evalVisitor = new SQLServerEvalVisitor();
        } else {
            evalVisitor = new SQLEvalVisitorImpl();
        }
        return evalVisitor;
    }
}

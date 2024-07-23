package com.alibaba.druid.analysis.sql.chain;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.statement.SQLBlockStatement;
import com.alibaba.druid.sql.ast.statement.SQLExprStatement;
import com.alibaba.druid.sql.ast.statement.SQLIfStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleForStatement;
import com.alibaba.druid.analysis.sql.model.ChainContext;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * SQLBlockStatement
 * Get result of dbms_output.put_line(...)
 */
@Slf4j
public class BlockProcessor extends AbstractProcessor {

    public BlockProcessor() {
    }

    @Override
    protected void before(ChainContext context) {
    }

    @Override
    protected void execute(ChainContext context) {
        SQLStatement sqlStatement = context.getSqlStatement();
        if (!(sqlStatement instanceof SQLBlockStatement)) {
            return;
        }
        SQLBlockStatement blockStmt = (SQLBlockStatement) sqlStatement;
        List<SQLStatement> stmts = blockStmt.getStatementList();
        if (CollUtil.isEmpty(stmts)) {
            return;
        }
        parseStmts(stmts, context);
    }

    private void parseStmts(List<SQLStatement> stmts, ChainContext context) {
        if (stmts == null || stmts.isEmpty()) {
            return;
        }
        for (SQLStatement stmt : stmts) {
            parseStmt(stmt, context);
        }
    }

    private void parseStmt(SQLStatement stmt, ChainContext context) {
        if (context.isDbmsOutput()) {
            return;
        }
        if (stmt instanceof SQLExprStatement) {
            SQLExprStatement exprStmt = (SQLExprStatement) stmt;
            SQLExpr expr = exprStmt.getExpr();
            if (!(expr instanceof SQLMethodInvokeExpr)) {
                return;
            }
            SQLMethodInvokeExpr miExpr = (SQLMethodInvokeExpr) expr;
            SQLExpr owner = miExpr.getOwner();
            if (owner != null && (owner instanceof SQLIdentifierExpr)) {
                SQLIdentifierExpr identifierExpr = (SQLIdentifierExpr) owner;
                if (identifierExpr.getLowerName().equals("dbms_output")
                        && StrUtil.equalsAnyIgnoreCase(miExpr.getMethodName(), "put_line")) {
                    context.setDbmsOutput(true);
                    return;
                }
            }
        }
        if (stmt instanceof SQLIfStatement) {
            SQLIfStatement ifStmt = (SQLIfStatement) stmt;
            List<SQLStatement> ifStmts = ifStmt.getStatements();
            parseStmts(ifStmts, context);
            if (context.isDbmsOutput()) {
                return;
            }
            List<SQLIfStatement.ElseIf> elseIfList = ifStmt.getElseIfList();
            for (SQLIfStatement.ElseIf elseIf : elseIfList) {
                List<SQLStatement> statements = elseIf.getStatements();
                parseStmts(statements, context);
                if (context.isDbmsOutput()) {
                    return;
                }
            }
            SQLIfStatement.Else elseItem = ifStmt.getElseItem();
            if (elseItem == null) {
                return;
            }
            List<SQLStatement> elseStmts = elseItem.getStatements();
            parseStmts(elseStmts, context);
        }
        if (stmt instanceof OracleForStatement) {
            OracleForStatement forStatement = (OracleForStatement) stmt;
            List<SQLStatement> statements = forStatement.getStatements();
            if (statements == null) {
                return;
            }
            parseStmts(statements, context);
        }
    }

    @Override
    protected void after(ChainContext context) {
    }
}

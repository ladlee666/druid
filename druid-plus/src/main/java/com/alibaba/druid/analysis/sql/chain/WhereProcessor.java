package com.alibaba.druid.analysis.sql.chain;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlEvalVisitorImpl;
import com.alibaba.druid.sql.visitor.SQLASTOutputVisitor;
import com.alibaba.druid.sql.visitor.SQLEvalVisitorUtils;
import com.alibaba.druid.sql.visitor.functions.IfNull;
import com.alibaba.druid.util.JdbcConstants;
import com.alibaba.druid.analysis.spi.EnhancedServiceLoader;
import com.alibaba.druid.analysis.sql.AbstractSQLExecutor;
import com.alibaba.druid.analysis.sql.SQLExecutor;
import com.alibaba.druid.analysis.sql.model.ChainContext;
import com.alibaba.druid.analysis.sql.model.ExecuteActionParam;
import com.alibaba.druid.analysis.sql.model.ExecuteContext;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class WhereProcessor extends AbstractProcessor {

    private AbstractSQLExecutor sqlExecutor;

    @Override
    protected void before(ChainContext context) {
        sqlExecutor = (AbstractSQLExecutor) EnhancedServiceLoader.load(SQLExecutor.class, context.getDbType().name());
    }

    @Override
    protected void execute(ChainContext context) {
        if (context.getSqlStatement() == null
                || !(context.getSqlStatement() instanceof SQLSelectStatement)) {
            return;
        }
        ExecuteContext executeContext = context.getContext();
        if (executeContext == null) {
            return;
        }
        ExecuteActionParam param = executeContext.getParam();
        if (CollUtil.isEmpty(param.getWhere()) && CollUtil.isEmpty(param.getBetween())
                && CollUtil.isEmpty(param.getDateBetween())) {
            return;
        }
        checkArg(sqlExecutor);
        sqlExecutor.where(context.getSqlStatement(), context.getContext());
    }

    @Override
    protected void after(ChainContext context) {
    }

    /**
     * SQLEvalVisitorUtils 注册的functions 没有ifNull所有不支持
     * static void registerBaseFunctions() {
     *         functions.put("now", Now.instance);
     *         functions.put("concat", Concat.instance);
     *         functions.put("concat_ws", Concat.instance);
     *         functions.put("ascii", Ascii.instance);
     *         functions.put("bin", Bin.instance);
     *         functions.put("bit_length", BitLength.instance);
     *         functions.put("insert", Insert.instance);
     *         functions.put("instr", Instr.instance);
     *         functions.put("char", Char.instance);
     *         functions.put("elt", Elt.instance);
     *         functions.put("left", Left.instance);
     *         functions.put("locate", Locate.instance);
     *         functions.put("lpad", Lpad.instance);
     *         functions.put("ltrim", Ltrim.instance);
     *         functions.put("mid", Substring.instance);
     *         functions.put("substr", Substring.instance);
     *         functions.put("substring", Substring.instance);
     *         functions.put("right", Right.instance);
     *         functions.put("reverse", Reverse.instance);
     *         functions.put("len", Length.instance);
     *         functions.put("length", Length.instance);
     *         functions.put("char_length", Length.instance);
     *         functions.put("character_length", Length.instance);
     *         functions.put("trim", Trim.instance);
     *         functions.put("ucase", Ucase.instance);
     *         functions.put("upper", Ucase.instance);
     *         functions.put("lcase", Lcase.instance);
     *         functions.put("lower", Lcase.instance);
     *         functions.put("hex", Hex.instance);
     *         functions.put("unhex", Unhex.instance);
     *         functions.put("greatest", Greatest.instance);
     *         functions.put("least", Least.instance);
     *         functions.put("isnull", Isnull.instance);
     *         functions.put("if", If.instance);
     *         functions.put("to_date", ToDate.instance);
     *         functions.put("to_char", ToChar.instance);
     *         functions.put("dateadd", DateAdd.instance);
     *
     *         functions.put("md5", OneParamFunctions.instance);
     *         functions.put("bit_count", OneParamFunctions.instance);
     *         functions.put("soundex", OneParamFunctions.instance);
     *         functions.put("space", OneParamFunctions.instance);
     *     }
     *     参考:https://www.mianshigee.com/tutorial/Druid/26dc273e9f424799.md
     * @param args
     */
//    public static void main(String[] args) {
//        String sql = "SELECT isnull('666') FROM user";
//        SQLStatement statement = SQLUtils.parseSingleStatement(sql, DbType.mysql);
//        SQLSelectStatement selectStatement = (SQLSelectStatement) statement;
//        SQLSelect select = selectStatement.getSelect();
//        SQLSelectQueryBlock queryBlock = select.getQueryBlock();
//
//        for (SQLSelectItem item : queryBlock.getSelectList()) {
//            SQLExpr expr = item.getExpr();
//            if (expr instanceof SQLMethodInvokeExpr) {
//                Object eval = SQLEvalVisitorUtils.eval(DbType.oracle, expr);
//                System.out.println(eval);
//            }
//        }
//        System.out.println(statement);
//    }
}

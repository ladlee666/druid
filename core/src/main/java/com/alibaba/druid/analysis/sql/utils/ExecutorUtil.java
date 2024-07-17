package com.alibaba.druid.analysis.sql.utils;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.parser.SQLParserFeature;
import com.alibaba.druid.sql.parser.SQLType;
import com.alibaba.druid.analysis.spi.EnhancedServiceLoader;
import com.alibaba.druid.analysis.sql.chain.*;
import com.alibaba.druid.analysis.sql.model.ChainContext;
import com.alibaba.druid.analysis.sql.model.SQLStatementModel;
import com.alibaba.druid.analysis.sql.visitor.CustomVisitor;
import org.springframework.core.convert.ConversionService;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class ExecutorUtil {

    public static List<SQLStatementModel> execute(String sql, String dbType,
                                                  Queue<String> mustacheKeysInOrder,
                                                  ConversionService conversionService,
                                                  boolean wrap,
                                                  Map<String, Object> params,
                                                  ChainContext.ChainContextBuilder contextBuilder) {
        return execute(sql, dbType, mustacheKeysInOrder, conversionService, wrap, params, contextBuilder, false);
    }

    public static List<SQLStatementModel> execute(String sql, String dbType,
                                                  Queue<String> mustacheKeysInOrder,
                                                  ConversionService conversionService,
                                                  boolean wrap,
                                                  Map<String, Object> params,
                                                  ChainContext.ChainContextBuilder contextBuilder,
                                                  boolean insertAsClear) {
        List<SQLStatementModel> sqlModels = new ArrayList<>();
        List<SQLStatement> sqlStatements = getSqlStatements(sql, dbType);
        int size = sqlStatements.size();
        for (int i = 1; i <= sqlStatements.size(); i++) {
            SQLStatement sqlStatement = sqlStatements.get(i - 1);
            if (insertAsClear) {
                clearInsertStatementAlias(sqlStatement);
            }
            CustomVisitor visitor = EnhancedServiceLoader.load(CustomVisitor.class, dbType);
            AbstractProcessor processor = AbstractProcessor.link(new ParseProcessor(mustacheKeysInOrder),
                    new ParamProcessor(params), new WhereProcessor(), new AuthWrapProcessor(conversionService),
                    new PageProcessor(), new AutoIdProcessor(), new BlockProcessor());
            ChainContext chainContext = contextBuilder.sqlStatement(sqlStatement).visitor(visitor)
                    .authWrap(wrap).build();
            Trigger trigger = new Trigger(processor, chainContext);
            trigger.doProcess();

            sqlModels.add(new SQLStatementModel(sqlStatement, chainContext.getCountStatement(),
                    visitor.getVariantModels(), chainContext.getParamKeys(), sqlType(sqlStatement), size == i,
                    chainContext.getAutoIds(), chainContext.isDbmsOutput()));
        }
        return sqlModels;
    }

    /**
     * 清除insert语句 在 SQLUtils.toSQLString之后别名带上as字段后出错的问题
     * eg: INSERT INTO PHA_PATIENT_OUTPUT t (t.drug_dept_code, t.out_bill_code) VALUES (1000000007, '15366358');
     * -> INSERT INTO PHA_PATIENT_OUTPUT AS t (t.drug_dept_code, t.out_bill_code) VALUES (1000000007, '15366358');
     *
     * @param sqlStatement
     * @see com.alibaba.druid.sql.dialect.oracle.visitor.OracleOutputVisitor #visit(SQLExprTableSource x)
     */
    public static void clearInsertStatementAlias(SQLStatement sqlStatement) {
        if (sqlStatement instanceof SQLInsertStatement) {
            SQLInsertStatement insertStatement = (SQLInsertStatement) sqlStatement;
            insertStatement.setAlias(null);

            List<SQLExpr> columns = insertStatement.getColumns();
            for (SQLExpr column : columns) {
                if (column instanceof SQLPropertyExpr) {
                    SQLPropertyExpr propertyExpr = (SQLPropertyExpr) column;
                    SQLUtils.replaceInParent(propertyExpr, new SQLIdentifierExpr(propertyExpr.getName()));
                }
            }
        }
    }

    public static List<SQLStatementModel> execute(String sql, DbType dbType,
                                                  Queue<String> mustacheKeysInOrder,
                                                  ConversionService conversionService,
                                                  boolean wrap,
                                                  Map<String, Object> params,
                                                  ChainContext.ChainContextBuilder contextBuilder) {
        return execute(sql, dbType.name(), mustacheKeysInOrder, conversionService, wrap, params, contextBuilder);
    }

    public static List<SQLStatementModel> execute(String sql, DbType dbType,
                                                  Queue<String> mustacheKeysInOrder,
                                                  Map<String, Object> params,
                                                  ChainContext.ChainContextBuilder contextBuilder) {
        return execute(sql, dbType.name(), mustacheKeysInOrder, null, false, params, contextBuilder, false);
    }

    public static List<SQLStatementModel> execute(String sql, DbType dbType,
                                                  Queue<String> mustacheKeysInOrder,
                                                  Map<String, Object> params,
                                                  ChainContext.ChainContextBuilder contextBuilder,
                                                  boolean insertAsClear) {
        return execute(sql, dbType.name(), mustacheKeysInOrder,
                null, false, params, contextBuilder, insertAsClear);
    }

    public static List<SQLStatement> getSqlStatements(String sql, String dbType) {
        return SQLUtils.parseStatements(sql, dbType, SQLParserFeature.PrintSQLWhileParsingFailed, SQLParserFeature.SkipComments);
    }

    public static String getSql(SQLStatement sqlStatement, DbType dbType) {
        return SQLUtils.toSQLString(sqlStatement, dbType);
    }

    public static String getSql(SQLStatement sqlStatement, String dbType) {
        return SQLUtils.toSQLString(sqlStatement, dbType);
    }

    protected static SQLType sqlType(SQLStatement sqlStatement) {
        if (sqlStatement instanceof SQLSelectStatement) {
            return SQLType.SELECT;
        }
        if (sqlStatement instanceof SQLUpdateStatement) {
            return SQLType.UPDATE;
        }
        if (sqlStatement instanceof SQLInsertStatement) {
            return SQLType.INSERT;
        }
        if (sqlStatement instanceof SQLCreateStatement) {
            return SQLType.CREATE;
        }
        if (sqlStatement instanceof SQLDeleteStatement) {
            return SQLType.DELETE;
        }
        if (sqlStatement instanceof SQLAlterStatement) {
            return SQLType.ALTER;
        }
        return SQLType.UNKNOWN;
    }

//    public static void main(String[] args) {
//        String sql = "INSERT INTO HIS_CLOUD.MET_COM_DIAGNOSE\n" +
//                "        (HOSPITAL_CODE, INPATIENT_NO, HAPPEN_NO, CARD_NO, DIAG_KIND\n" +
//                "        , ICD_CODE, DIAG_NAME, DIAG_DATE, DOCT_CODE, DIAG_DOC_NAME\n" +
//                "        , DEPT_CODE, MAIN_FLAG, OPERATIONNO, MARK, OPER_CODE\n" +
//                "        , OPER_DATE, DUBDIAG_FLAG, DIAG_DESCRIBE, SCID, SCID_NAME\n" +
//                "        , MEDICAL_INSURANCE, DISEASE_CODE)\n" +
//                "VALUES ('420922', '1536818', '123456', '0000000256', 'D'\n" +
//                "        , 'A01.01.01', '感冒', SYSDATE, '1739586628553523201', '秦风'\n" +
//                "        , '1000000013', '1', 'null', 'null', '1739586628553523201'\n" +
//                "        , SYSDATE, '0', 'null', null, null" +
//                "        , 'null', 'B')";
//
//        SQLStatement sqlStatement = SQLUtils.parseSingleStatement(sql, DbType.oracle);
//        if (sqlStatement instanceof SQLInsertStatement) {
//            OracleInsertStatement oracleInsertStatement = (OracleInsertStatement) sqlStatement;
//            List<SQLInsertStatement.ValuesClause> valuesList = oracleInsertStatement.getValuesList();
//            for (SQLInsertStatement.ValuesClause valuesClause : valuesList) {
//                List<SQLExpr> values = valuesClause.getValues();
//                for (int i = 0; i < values.size(); i++) {
//                    SQLExpr expr = values.get(i);
//                    if (expr instanceof SQLCharExpr) {
//                        SQLCharExpr charExpr = (SQLCharExpr) expr;
//                        String value = StrUtil.toString(charExpr.getValue());
//                        if (StrUtil.isNullOrUndefined(value)) {
//                            SQLExpr ex = new SQLNullExpr();
//                            values.set(i, ex);
//                        }
//                    }
//                }
//            }
//
//        }
//        System.out.println(sqlStatement);
//    }
}

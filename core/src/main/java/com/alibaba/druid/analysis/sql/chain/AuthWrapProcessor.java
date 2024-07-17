package com.alibaba.druid.analysis.sql.chain;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.analysis.spi.EnhancedServiceLoader;
import com.alibaba.druid.analysis.sql.AbstractSQLExecutor;
import com.alibaba.druid.analysis.sql.SQLExecutor;
import com.alibaba.druid.analysis.sql.constants.DataScopeEnum;
import com.alibaba.druid.analysis.sql.constants.ManageConstant;
import com.alibaba.druid.analysis.sql.model.ChainContext;
import com.alibaba.druid.analysis.sql.model.ColumnParam;
import com.alibaba.druid.analysis.sql.model.TableModel;
import com.alibaba.druid.analysis.sql.utils.PreStatementSetParamUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.hutool.core.util.RandomUtil.BASE_CHAR;
import static com.alibaba.druid.analysis.sql.Constants.ALL_COLUMN;


@Slf4j
public class AuthWrapProcessor extends AbstractProcessor {

    private AbstractSQLExecutor sqlExecutor;

    private JdbcTemplate jdbcTemplate;

    private final ConversionService conversionService;

    private final String DUAL = "dual";

    private final String ALIAS = "TEMP";

    public AuthWrapProcessor(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    protected void before(ChainContext context) {
        jdbcTemplate = context.getJdbcTemplate();
        sqlExecutor = (AbstractSQLExecutor) EnhancedServiceLoader.load(SQLExecutor.class, context.getDbType().name());
    }

    @Override
    protected void execute(ChainContext context) {
        if (!context.isAuthWrap() || context.getSqlStatement() == null || !(context.getSqlStatement() instanceof SQLSelectStatement)) {
            return;
        }
        if (StrUtil.isBlank(context.getDeptId()) && StrUtil.isBlank(context.getUserId())) {
            return;
        }
        checkArg(sqlExecutor);
        sqlExecutor.auth(context.getSqlStatement(), context.getContext(), context.getVisitor());

        Set<TableModel> tableNames = context.getVisitor().getTableNames();
        Set<String> tableSets = tableNames.stream().filter(table -> !table.isSubQuery())
                .map(TableModel::getTableName).collect(Collectors.toSet());
        for (String tableName : tableSets) {
            if (StrUtil.equalsIgnoreCase(tableName, DUAL)) {
                return;
            }
        }

        SQLStatement cloneSQLStatement = context.getSqlStatement().clone();
        if (cloneSQLStatement instanceof SQLSelectStatement) {
            SQLSelectStatement cloneSelectStatement = (SQLSelectStatement) cloneSQLStatement;
            sqlExecutor.clearOrderBy(cloneSelectStatement.getSelect().getQuery());
        }
        SQLStatement fieldCheckSelect = authCheckWrap(cloneSQLStatement);
        List<ColumnParam> columnInfos = getSqlWrapColumns(fieldCheckSelect, context.getContext().getParam().getParams(),
                context.getParamKeys(), context.getDbType().name());
        String tableAlias = RandomUtil.randomString(BASE_CHAR, 3);
        SQLExpr expr = wrapSqlExpr(columnInfos, tableAlias, context);
        if (expr == null) {
            return;
        }

        SQLSelectStatement selectStatement = (SQLSelectStatement) context.getSqlStatement();
        SQLSelect select = selectStatement.getSelect();
        SQLSelectQueryBlock queryBlock = sqlExecutor.buildQuery(tableAlias, ALL_COLUMN, select, expr);
        select.setQuery(queryBlock);
    }

    @Override
    protected void after(ChainContext context) {
    }

    private SQLStatement authCheckWrap(SQLStatement cloneStatement) {
        if (!(cloneStatement instanceof SQLSelectStatement)) {
            return null;
        }

        SQLSelectStatement selectStatement = (SQLSelectStatement) cloneStatement;
        SQLSelect select = selectStatement.getSelect();

        SQLSelectQueryBlock newBlock = sqlExecutor.buildQuery(ALIAS, ALL_COLUMN, select,
                new SQLBinaryOpExpr(new SQLIntegerExpr(1),
                        SQLBinaryOperator.GreaterThan,
                        new SQLIntegerExpr(2)));

        select.setQuery(newBlock);

        return selectStatement;
    }

    protected List<ColumnParam> getSqlWrapColumns(SQLStatement fieldCheckSelect, Map<String, Object> params,
                                                  List<String> keys, String dbType) {
        String cloneSql = SQLUtils.toSQLString(fieldCheckSelect, dbType);
        List<ColumnParam> columns = new ArrayList<>();

        return jdbcTemplate.execute(PreStatementSetParamUtils.setArrayParams(cloneSql, params, keys),
                (PreparedStatementCallback<List<ColumnParam>>) ps -> {
                    PreStatementSetParamUtils.setParams(ps, conversionService, params, keys);
                    ps.execute();
                    ResultSet rs = ps.getResultSet();
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        Integer columnType = metaData.getColumnType(i);
                        columns.add(new ColumnParam(columnName.toUpperCase(), columnType));
                    }
                    return columns;
                });
    }

    protected SQLExpr wrapSqlExpr(List<ColumnParam> columnInfos, String tableAlias, ChainContext context) {
        if (CollUtil.isEmpty(columnInfos)) {
            return null;
        }
        // 字段是否存在
        boolean empExist = false;
        boolean deptExist = false;

        // 是否拼接参数的时候加上'单引号
        boolean empQuoMark = false;
        boolean deptQuoMark = false;

        for (ColumnParam column : columnInfos) {
            String columnName = column.getColumnName();

            if (StrUtil.equals(columnName, ManageConstant.USER_ID)) {
                empExist = true;
                empQuoMark = mark(column.getColumnType());
            }
            if (StrUtil.equals(columnName, ManageConstant.DEPT_FIELD)) {
                deptExist = true;
                deptQuoMark = mark(column.getColumnType());
            }

            if (empExist && deptExist) {
                break;
            }
        }
        if (!empExist && !deptExist) {
            return null;
        }
        // 过滤科室权限
        Integer roleRange = context.getRoleRange();
        List<String> deptIds = new ArrayList<>();
        genDeptIds(roleRange, deptIds, deptExist, context);


        SQLExpr resultExpr = null;
        if (CollUtil.isNotEmpty(deptIds)) {
            SQLExpr expr = buildInCondition(tableAlias, ManageConstant.DEPT_FIELD, deptIds, false, deptQuoMark);
            resultExpr = SQLBinaryOpExpr.and(resultExpr, expr);
        }
        // 职工ID
        String userId = context.getUserId();
        if ("1".equals(userId)) {
            return null;
        }
        if ((empExist && StrUtil.isNotBlank(userId)) ||
                (DataScopeEnum.MY_LEVEL.getRage().equals(roleRange)
                        || DataScopeEnum.MY_CHILD_LEVEL.getRage().equals(roleRange))) {
            SQLExpr expr = buildEqCondition(tableAlias, ManageConstant.USER_ID, userId, empQuoMark);
            resultExpr = SQLBinaryOpExpr.and(resultExpr, expr);
        }
        return resultExpr;
    }


    private void genDeptIds(Integer roleRange, List<String> deptIds, boolean deptExist, ChainContext context) {
        if (StrUtil.isBlank(context.getDeptId()) || !deptExist) {
            return;
        }
        String deptId = context.getDeptId();
        if (roleRange == null || ManageConstant.DEFAULT_DEPT_ID.equals(deptId) ||
                DataScopeEnum.ALL.getRage().equals(roleRange)) {
            return;
        }
        if (DataScopeEnum.OWN_CHILD_LEVEL.getRage().equals(roleRange)) {
            // 查看本部门和下级部门数据
            List<String> childDeptIds = context.getChildDeptIds();
            deptIds.addAll(childDeptIds);
        } else if (DataScopeEnum.OWN_LEVEL.getRage().equals(roleRange)) {
            // 查看本部门数据
            deptIds.add(deptId);
        } else if (DataScopeEnum.MY_CHILD_LEVEL.getRage().equals(roleRange)) {
            //查看本部门及下级部门中个人数据
            List<String> childDeptIds = context.getChildDeptIds();
            deptIds.addAll(childDeptIds);
        } else if (DataScopeEnum.MY_LEVEL.getRage().equals(roleRange)) {
            //查看本部门中个人数据
            deptIds.add(deptId);
        }
    }

    private SQLExpr buildEqCondition(String tableNameAlias, String columnName, String value, Boolean mark) {
        SQLExpr leftExpr;
        SQLBinaryOpExpr condition = new SQLBinaryOpExpr();
        if (StrUtil.isBlank(tableNameAlias)) {
            leftExpr = new SQLIdentifierExpr(columnName);
        } else {
            leftExpr = new SQLPropertyExpr(new SQLIdentifierExpr(tableNameAlias), columnName);
        }
        condition.setLeft(leftExpr);
        condition.setOperator(SQLBinaryOperator.Equality);
        SQLExpr rightExpr;
        if (mark) {
            rightExpr = new SQLCharExpr(value);
        } else {
            rightExpr = new SQLNumberExpr(Long.valueOf(value));
        }
        condition.setRight(rightExpr);
        return condition;
    }

    private SQLExpr buildInCondition(String tableNameAlias, String columnName, List<String> arrayInValue,
                                     boolean not, Boolean mark) {
        SQLExpr targetExpr = new SQLIdentifierExpr(tableNameAlias + "." + columnName);
        SQLInListExpr condition = new SQLInListExpr();
        condition.setExpr(targetExpr);
        List<SQLExpr> sqlExprList = new ArrayList<>();
        for (String v : arrayInValue) {
            SQLExpr se;
            if (mark) {
                se = new SQLCharExpr(v);
            } else {
                se = new SQLIntegerExpr(Integer.valueOf(v));
            }
            sqlExprList.add(se);
        }
        condition.setTargetList(sqlExprList);
        condition.setNot(not);
        return condition;
    }

    private boolean mark(int columnType) {
        switch (columnType) {
            case 12:
            case -9:
                return true;
        }
        return false;
    }

}

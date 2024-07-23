package com.alibaba.druid.analysis.sql.chain;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLNullExpr;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.analysis.sql.model.ChainContext;
import com.alibaba.druid.analysis.sql.model.VariantModel;

import java.util.List;
import java.util.Map;

public class ParamProcessor extends AbstractProcessor {

    private final Map<String, Object> params;

    public ParamProcessor(Map<String, Object> params) {
        this.params = params;
    }

    @Override
    protected void before(ChainContext context) {
    }

    @Override
    protected void execute(ChainContext context) {
        SQLStatement sqlStatement = context.getSqlStatement();
        if (sqlStatement instanceof SQLInsertStatement) {
            transformStrNullToNull((SQLInsertStatement) sqlStatement);
            return;
        }
        List<VariantModel> variantModels = context.getVisitor().getVariantModels();
        if (CollUtil.isEmpty(variantModels)) {
            return;
        }
        List<String> paramKeys = context.getParamKeys();
        int vector = 0;
        for (VariantModel variantModel : variantModels) {
            SQLObject sqlObject = variantModel.getSqlObject();
            if (sqlObject == null) {
                continue;
            }
            SQLObject parent = variantModel.getParent();
            if (parent == null || !(parent instanceof SQLBinaryOpExpr)) {
                continue;
            }
            int index = variantModel.getOwnerIndex() - vector;
            String paramKey = paramKeys.get(index);
            if (StrUtil.isBlank(paramKey)) {
                continue;
            }
            Object value = params.get(paramKey);
            String valueStr = StrUtil.toStringOrNull(value);
            if (valueStr != null && !"null".equals(valueStr)) {
                continue;
            }
            // 把参数?为null的条件去掉
            SQLBinaryOperator operator = variantModel.getOperator();
            SQLBinaryOpExpr expr = (SQLBinaryOpExpr) parent;
            // 上级还是SQLBinaryOpExpr eg: where (r.name = ? or r.email = ?) and u.username = ? ;
            // Druid会把(r.name = ? or r.email = ?)当成一个SQLBinaryOpExpr 如果r.email = ?中参数为null，要把r.email = ?去掉，
            // 必须要找到上级(r.name = ? or r.email = ?)再移除r.email = ?
            if (expr.getParent() instanceof SQLBinaryOpExpr) {
                SQLBinaryOpExpr parentExpr = (SQLBinaryOpExpr) expr.getParent();
                SQLExpr left = parentExpr.getLeft();
                SQLExpr right = parentExpr.getRight();
                boolean replace = false;
                if (expr.equals(left)) {
                    replace = SQLUtils.replaceInParent(parentExpr, right);
                } else if (expr.equals(right)) {
                    replace = SQLUtils.replaceInParent(parentExpr, left);
                }
                if (replace) {
                    paramKeys.remove(index);
                    vector++;
                }
            } else if (sqlObject instanceof SQLSelectQueryBlock) {
                SQLSelectQueryBlock queryBlock = (SQLSelectQueryBlock) sqlObject;
                boolean success = queryBlock.removeCondition(expr);
                if (success) {
                    paramKeys.remove(index);
                    vector++;
                }
            } else if (sqlObject instanceof SQLUpdateStatement) {
                SQLUpdateStatement sqlUpdateStatement = (SQLUpdateStatement) sqlObject;
                boolean success = sqlUpdateStatement.removeCondition(expr);
                if (success) {
                    paramKeys.remove(index);
                    vector++;
                    assertWhere(sqlUpdateStatement.getWhere());
                }
            } else if (sqlObject instanceof SQLDeleteStatement) {
                SQLDeleteStatement sqlDeleteStatement = (SQLDeleteStatement) sqlObject;
                boolean success = sqlDeleteStatement.removeCondition(expr);
                if (success) {
                    paramKeys.remove(index);
                    vector++;
                    assertWhere(sqlDeleteStatement.getWhere());
                }
            }
            if (operator == SQLBinaryOperator.Like) {
                SQLCharExpr rightExpr = new SQLCharExpr("%" + value + "%");
                expr.setRight(rightExpr);
                paramKeys.remove(index);
                vector++;
            }
        }
    }

    private void transformStrNullToNull(SQLInsertStatement insertStatement) {
        List<SQLInsertStatement.ValuesClause> valuesList = insertStatement.getValuesList();
        for (SQLInsertStatement.ValuesClause valuesClause : valuesList) {
            List<SQLExpr> values = valuesClause.getValues();
            for (int i = 0; i < values.size(); i++) {
                SQLExpr expr = values.get(i);
                if (expr instanceof SQLCharExpr) {
                    SQLCharExpr charExpr = (SQLCharExpr) expr;
                    String value = StrUtil.toString(charExpr.getValue());
                    if (StrUtil.isNullOrUndefined(value)) {
                        SQLExpr nullExpr = new SQLNullExpr();
                        values.set(i, nullExpr);
                    }
                }
            }
        }
    }

    @Override
    protected void after(ChainContext context) {
    }

    private void assertWhere(SQLExpr expr) {
        if (expr == null) {
            throw new IllegalArgumentException("请传入有效的更新或删除条件哦!");
        }
    }
}

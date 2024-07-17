package com.alibaba.druid.analysis.sql.visitor;

import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import com.alibaba.druid.analysis.sql.model.TableModel;
import com.alibaba.druid.analysis.sql.model.VariantModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface CustomVisitor extends SQLASTVisitor {

    String EMPTY_TABLE_KEY = StrUtil.EMPTY;

    @Override
    default boolean visit(SQLVariantRefExpr x) {
        if (initStats()) {
            return false;
        }
        SQLObject parent = x.getParent();
        int index = x.getIndex();
        SQLExpr targetExpr = null;
        SQLBinaryOperator operator = null;
        if (parent instanceof SQLBinaryOpExpr) {
            SQLBinaryOpExpr opExpr = (SQLBinaryOpExpr) parent;
            targetExpr = opExpr.getLeft();
            operator = opExpr.getOperator();
        } else if (parent instanceof SQLBetweenExpr) {
            SQLBetweenExpr betweenExpr = (SQLBetweenExpr) parent;
            targetExpr = betweenExpr.getTestExpr();
        } else if (parent instanceof SQLUpdateSetItem) {
            SQLUpdateSetItem updateSetItem = (SQLUpdateSetItem) parent;
            targetExpr = updateSetItem.getColumn();
        } else if (parent instanceof SQLInsertStatement) {
//            SQLInsertStatement insertStatement = (SQLInsertStatement) parent;
        }
        explainExpr(parent, targetExpr, index, operator);
        return true;
    }

    default List<VariantModel> getVariantModels() {
        return new ArrayList<>();
    }

    default Set<TableModel> getTableNames() {
        return new HashSet<>();
    }

    void init();

    boolean initStats();

    default void explainExpr(SQLObject parent, SQLExpr targetExpr, Integer index, SQLBinaryOperator operator) {
        String fieldName = null;
        String alias = null;
        if (targetExpr instanceof SQLIdentifierExpr) {
            SQLIdentifierExpr identifierExpr = (SQLIdentifierExpr) targetExpr;
            fieldName = identifierExpr.getName();
        } else if (targetExpr instanceof SQLPropertyExpr) {
            SQLPropertyExpr propertyExpr = (SQLPropertyExpr) targetExpr;
            fieldName = propertyExpr.getName();
            alias = propertyExpr.getOwnerName();
        }
        SQLObject queryBlock = getSqlObject(parent);
        VariantModel model = new VariantModel(index, fieldName, alias, targetExpr, parent, queryBlock, operator);
        getVariantModels().add(model);
    }

    default SQLObject getSqlObject(SQLObject sqlObject) {
        if (sqlObject == null) {
            return null;
        }
        if ((sqlObject instanceof SQLSelectQueryBlock)
                || (sqlObject instanceof SQLUpdateStatement)
                || (sqlObject instanceof SQLInsertStatement)
                || (sqlObject instanceof SQLDeleteStatement)) {
            return sqlObject;
        }
        return getSqlObject(sqlObject.getParent());
    }

}

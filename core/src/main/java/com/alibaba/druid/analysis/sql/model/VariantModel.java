package com.alibaba.druid.analysis.sql.model;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class VariantModel {

    private int index;

    private String name;

    private String alias;

    private SQLExpr owner;

    private SQLObject parent;

    private SQLObject sqlObject;

    private SQLBinaryOperator operator;

    private int ownerIndex;

    public VariantModel() {
    }

    public VariantModel(int index, String name, String alias, SQLExpr owner, SQLObject parent,
                        SQLObject sqlObject, SQLBinaryOperator operator) {
        this.index = index;
        this.name = name;
        this.alias = alias;
        this.owner = owner;
        this.parent = parent;
        this.sqlObject = sqlObject;
        this.operator = operator;
    }
}

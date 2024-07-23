package com.alibaba.druid.sql.dialect.oracle.ast.expr;

import com.alibaba.druid.sql.ast.SQLExpr;

/**
 * @author LENOVO
 * @date 2024/7/16 16:10
 */
public abstract class OracleExprImpl extends OracleObjectImpl implements SQLExpr {

    @Override
    public SQLExpr clone() {
        throw new UnsupportedOperationException();
    }
}

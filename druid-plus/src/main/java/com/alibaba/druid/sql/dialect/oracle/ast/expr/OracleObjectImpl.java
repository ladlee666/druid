package com.alibaba.druid.sql.dialect.oracle.ast.expr;

import com.alibaba.druid.sql.ast.SQLObjectImpl;
import com.alibaba.druid.sql.dialect.oracle.ast.OracleSQLObject;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleASTVisitor;
import com.alibaba.druid.sql.visitor.SQLASTOutputVisitor;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;

/**
 * @author LENOVO
 * @date 2024/7/16 17:01
 */
public abstract class OracleObjectImpl extends SQLObjectImpl implements OracleSQLObject {

    @Override
    protected void accept0(SQLASTVisitor v) {
        if (v instanceof OracleASTVisitor) {
            this.accept0((OracleASTVisitor) v);
            return;
        }

        if (v instanceof SQLASTOutputVisitor) {
            ((SQLASTOutputVisitor) v).print(this.toString());
        }
    }

    public abstract void accept0(OracleASTVisitor visitor);
}

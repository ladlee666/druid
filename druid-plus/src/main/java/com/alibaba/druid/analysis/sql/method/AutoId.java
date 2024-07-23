package com.alibaba.druid.analysis.sql.method;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.expr.SQLNumberExpr;
import com.alibaba.druid.sql.visitor.SQLEvalVisitor;
import com.alibaba.druid.analysis.seq.SeqContext;
import com.alibaba.druid.analysis.seq.builder.DbSeqBuilder;
import com.alibaba.druid.analysis.seq.sequence.Sequence;
import org.apache.commons.lang3.StringUtils;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * auto('test',str)
 */
public class AutoId implements MethodFunction {

    private final String STR_CLASS = "str";

    private final String INT_CLASS = "int";

    private final DataSource dataSource;

    private final String dbType;

    private List<Object> result = new ArrayList<>();

    public AutoId(DataSource dataSource, String dbType) {
        this.dataSource = dataSource;
        this.dbType = dbType;
    }

    @Override
    public Object eval(SQLEvalVisitor visitor, SQLMethodInvokeExpr x) {
        String methodName = x.getMethodName();
        if (StringUtils.isBlank(methodName) || !StringUtils.equals(methodName, functionName())) {
            return null;
        }
        List<SQLExpr> arguments = x.getArguments();
        if (arguments == null || arguments.size() < 2) {
            throw new IllegalArgumentException("[auto]方法参数错误!");
        }
        SQLCharExpr charExpr = (SQLCharExpr) arguments.get(0);
        String name = charExpr.getValue().toString();

        SQLIdentifierExpr typeExpr = (SQLIdentifierExpr) arguments.get(1);
        String type = typeExpr.getName();

        Sequence sequence = SeqContext.getSequence(dataSource, name);
        if (sequence == null) {
            sequence = DbSeqBuilder
                    .create()
                    .dataSource(dataSource)
                    .dbType(dbType)
                    .stepStart(0)
                    .bizName(() -> name)
                    .build();
            SeqContext.setSequence(dataSource, name, sequence);
        }
        long newValue = sequence.nextValue(null);
        SQLExpr expr;
        if (INT_CLASS.equals(type)) {
            expr = new SQLNumberExpr(newValue);
            result.add(newValue);
        } else {
            String text = newValue + "";
            expr = new SQLCharExpr(text);
            result.add(text);
        }
        SQLUtils.replaceInParent(x, expr);
        return newValue;
    }

    @Override
    public String functionName() {
        return "auto";
    }

    @Override
    public List<Object> getResult() {
        return this.result;
    }
}
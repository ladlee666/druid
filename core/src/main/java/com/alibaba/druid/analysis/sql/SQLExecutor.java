package com.alibaba.druid.analysis.sql;

import com.alibaba.druid.sql.visitor.SQLASTVisitor;


public interface SQLExecutor<T, P> {

    String START = "start";

    String END = "end";

    void where(P target, T context);

    void parse(P target, T context, SQLASTVisitor visitor);

    void page(P target, T context);

    P count(P target, T context);

    void auth(P target, T context, SQLASTVisitor visitor);
}

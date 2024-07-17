package com.alibaba.druid.analysis.sql.method;

import com.alibaba.druid.sql.visitor.functions.Function;

public interface MethodFunction extends Function {

    String functionName();

    Object getResult();
}

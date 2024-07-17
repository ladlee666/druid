package com.alibaba.druid.analysis.sql.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ExecuteContext {

    /**
     * 请求参数
     */
    private ExecuteActionParam param;

    private Action action;

    private Boolean prepared;

    private String platform;

    private List<String> selectField = new ArrayList<>();

    private List<SQLStatementModel> sqlModels;

    public ExecuteContext(ExecuteActionParam param) {
        this.param = param;
    }

    public ExecuteContext() {
    }
}

package com.alibaba.druid.analysis.sql.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class ActionParam implements Serializable {

    private String name;

    private String rawSql;

    private String paramsJson;

    private String databaseId;

    private String type;

    private Boolean excuProcedure;

    private String outMap;

}

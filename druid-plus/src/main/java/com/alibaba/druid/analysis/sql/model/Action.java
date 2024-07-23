package com.alibaba.druid.analysis.sql.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@NoArgsConstructor
public class Action implements Serializable {

    private static final long serialVersionUID = 1L;

    public Action(String id, String name, String rawSql, String paramsJson,
                  String type, Boolean excuProcedure, String outMap, String databaseId) {
        this.id = id;
        this.name = name;
        this.rawSql = rawSql;
        this.paramsJson = paramsJson;
        this.type = type;
        this.excuProcedure = excuProcedure;
        this.outMap = outMap;
        this.databaseId = databaseId;
    }

    public Action(String id, String name, String rawSql, String paramsJson,
                  String type, Boolean excuProcedure, String outMap, String databaseId,
                  Boolean prepared) {
        this.id = id;
        this.name = name;
        this.rawSql = rawSql;
        this.paramsJson = paramsJson;
        this.type = type;
        this.excuProcedure = excuProcedure;
        this.outMap = outMap;
        this.databaseId = databaseId;
        this.prepared = prepared;
    }

    /**
     * 主键
     */
    private String id;

    /**
     * 名称
     */
    private String name;

    /**
     * 原始SQL
     */
    private String rawSql;

    /**
     * SQL参数名
     */
    private String paramsJson;

    private String databaseId;

    /**
     * db js
     */
    private String type;

    /**
     * 是否存储过程
     */
    private Boolean excuProcedure;

    /**
     * 存储过程出参
     */
    private String outMap;

    /**
     * sql是否预处理(是:true,否:false)
     */
    private Boolean prepared;

    /**
     * 删除标记 0正常 1删除
     */
    private Integer rmTag;

}


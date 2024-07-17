package com.alibaba.druid.analysis.sql.model;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ExecuteActionParam implements Serializable {

    private String xid;

    private String actionId;

    private Boolean viewMode;

    private Integer page;

    private Integer size;

    private Boolean enablePage;

    private Map<String, Object> params = new HashMap<>();

    /**
     * ActionParam
     */
    private ActionParam action;

    /**
     * 拼接的where条件
     */
    Map<String,Object> where;

    /**
     * 拼接范围查询条件
     */
    Map<String, Map<String,Object>> between;

    /**
     * 拼接时间范围查询条件
     */
    Map<String, List<Map<String,Object>>> dateBetween;
}

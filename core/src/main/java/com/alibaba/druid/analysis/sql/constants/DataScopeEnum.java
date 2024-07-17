package com.alibaba.druid.analysis.sql.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * 权限范围(1:查看所有数据;2:查看本部门和下级部门数据;3:查看本部门数据)
 * 1 > 2 > 3
 */
@Getter
@AllArgsConstructor
public enum DataScopeEnum {

    ALL(1, "全部"),

    OWN_CHILD_LEVEL(2, "查看本部门和下级部门数据"),

    OWN_LEVEL(3, "查看本部门数据"),

    MY_CHILD_LEVEL(4, "查看本部门及下级部门中个人数据"),

    MY_LEVEL(5, "查看本部门中个人数据");

    /**
     * 范围
     */
    private final Integer rage;

    /**
     * 描述
     */
    private final String describe;
}

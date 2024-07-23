package com.alibaba.druid.analysis.meta.bean;

import java.util.Map;

/**
 * @author LENOVO
 * @date 2024/7/2 10:45
 */
public interface FieldMapping {
    Map<String, String> mapping(Map<String,String> map);
}

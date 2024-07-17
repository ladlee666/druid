package com.alibaba.druid.analysis.meta.bean;

/**
 * @author LENOVO
 * @date 2024/7/2 10:45
 */
public class BeanCompareResult {
    private String code;
    private String name;
    private String oldValue;
    private String newValue;
    private String extendRemark;

    public String getExtendRemark() {
        return extendRemark;
    }

    public void setExtendRemark(String extendRemark) {
        this.extendRemark = extendRemark;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    @Override
    public String toString() {
        return "BeanCompareResult{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", oldValue='" + oldValue + '\'' +
                ", newValue='" + newValue + '\'' +
                ", extendRemark='" + extendRemark + '\'' +
                '}';
    }
}

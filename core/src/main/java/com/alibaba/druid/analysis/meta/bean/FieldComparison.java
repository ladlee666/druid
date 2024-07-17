package com.alibaba.druid.analysis.meta.bean;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author LENOVO
 * @date 2024/7/2 10:44
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FieldComparison {

    /**
     * 字段对应的中文名从
     *
     * @return
     */
    String codeName() default "";

    /**
     * 字段对应关系，如 1->下线 0->上线
     *
     * @return
     */
    Class<? extends FieldMapping> mapping() default DefaultFieldMapping.class;

}

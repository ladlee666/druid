package com.alibaba.druid.analysis.spi;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Service {

    String name();

    int order() default 0;

    Scope scope() default Scope.SINGLETON;
}

package com.alibaba.druid.analysis.meta.alter;

import com.alibaba.druid.DbType;


/**
 * @author LENOVO
 * @date 2024/6/24 15:02
 */
public interface CreatorFactoryProvider {

    boolean support(DbType dbType);

    CreatorFactory create();

}

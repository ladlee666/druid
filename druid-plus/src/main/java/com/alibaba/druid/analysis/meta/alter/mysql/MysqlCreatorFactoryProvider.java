package com.alibaba.druid.analysis.meta.alter.mysql;

import com.alibaba.druid.DbType;
import com.alibaba.druid.analysis.meta.alter.CreatorFactory;
import com.alibaba.druid.analysis.meta.alter.CreatorFactoryProvider;

/**
 * @author LENOVO
 * @date 2024/6/24 15:20
 */
public class MysqlCreatorFactoryProvider implements CreatorFactoryProvider {

    @Override
    public boolean support(DbType dbType) {
        return dbType == DbType.mysql;
    }

    @Override
    public CreatorFactory create() {
        return new MysqlCreatorFactory();
    }
}

package com.alibaba.druid.analysis.meta.alter.oracle;

import com.alibaba.druid.DbType;
import com.alibaba.druid.analysis.meta.alter.CreatorFactory;
import com.alibaba.druid.analysis.meta.alter.CreatorFactoryProvider;

/**
 * @author LENOVO
 * @date 2024/6/24 15:17
 */
public class OracleCreatorFactoryProvider implements CreatorFactoryProvider {

    @Override
    public boolean support(DbType dbType) {
        return dbType == DbType.oracle;
    }

    @Override
    public CreatorFactory create() {
        return new OracleCreatorFactory();
    }
}

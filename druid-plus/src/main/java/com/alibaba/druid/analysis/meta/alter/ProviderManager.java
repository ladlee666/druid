package com.alibaba.druid.analysis.meta.alter;

import com.alibaba.druid.DbType;
import com.alibaba.druid.analysis.meta.alter.mysql.MysqlCreatorFactoryProvider;
import com.alibaba.druid.analysis.meta.alter.oracle.OracleCreatorFactoryProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LENOVO
 * @date 2024/6/24 15:22
 */
public class ProviderManager {

    private static final ProviderManager INSTANCE = new ProviderManager();

    private final List<CreatorFactoryProvider> providers = new ArrayList<>();

    public static ProviderManager getInstance() {
        return INSTANCE;
    }

    private ProviderManager() {
        this.providers.add(new OracleCreatorFactoryProvider());
        this.providers.add(new MysqlCreatorFactoryProvider());
    }

    public CreatorFactory getFactoryProvider(DbType dbType) {
        for (CreatorFactoryProvider provider : providers) {
            if (provider.support(dbType)) {
                return provider.create();
            }
        }
        throw new IllegalArgumentException(dbType + "is not support.");
    }
}

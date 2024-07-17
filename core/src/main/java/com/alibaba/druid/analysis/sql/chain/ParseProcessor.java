package com.alibaba.druid.analysis.sql.chain;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.druid.analysis.spi.EnhancedServiceLoader;
import com.alibaba.druid.analysis.sql.AbstractSQLExecutor;
import com.alibaba.druid.analysis.sql.SQLExecutor;
import com.alibaba.druid.analysis.sql.model.ChainContext;
import com.alibaba.druid.analysis.sql.model.VariantModel;
import com.alibaba.druid.analysis.sql.visitor.CustomVisitor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

@Slf4j
public class ParseProcessor extends AbstractProcessor {

    private AbstractSQLExecutor sqlExecutor;

    private final Queue<String> mustacheKeysInOrder;

    private final List<String> keys = new ArrayList<>();

    public ParseProcessor(Queue<String> mustacheKeysInOrder) {
        this.mustacheKeysInOrder = mustacheKeysInOrder;
    }

    @Override
    protected void before(ChainContext context) {
        sqlExecutor = (AbstractSQLExecutor) EnhancedServiceLoader.load(SQLExecutor.class, context.getDbType().name());
    }

    @Override
    protected void execute(ChainContext context) {
        checkArg(sqlExecutor);
        CustomVisitor visitor = context.getVisitor();
        if (null == visitor) {
            return;
        }
        sqlExecutor.parse(context.getSqlStatement(), context.getContext(), visitor);
        if (CollUtil.isEmpty(visitor.getVariantModels())) {
            return;
        }
        // 解析?对应的key，后面用来解析实际需要填充的值
        List<VariantModel> variantModels = visitor.getVariantModels();
        for (int i = 0; i < variantModels.size(); i++) {
            String key = mustacheKeysInOrder.poll();
            keys.add(key);
            VariantModel variantModel = variantModels.get(i);
            variantModel.setOwnerIndex(i);
        }
    }

    @Override
    protected void after(ChainContext context) {
        context.setParamKeys(getKeys());
    }

    public List<String> getKeys() {
        return keys;
    }
}

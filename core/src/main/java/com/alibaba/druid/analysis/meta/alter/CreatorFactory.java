package com.alibaba.druid.analysis.meta.alter;

import com.alibaba.druid.analysis.meta.model.MetaDBColumn;

import java.util.List;

/**
 * @author LENOVO
 * @date 2024/6/24 15:09
 */
public interface CreatorFactory {

    AlterCreator create(AlterMode mode, List<MetaDBColumn> columns);

    /**
     * 字段类型列表
     *
     * @return
     */
    List<String> columnTypeNames();
}

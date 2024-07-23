package com.alibaba.druid.analysis.meta.model.extend;

import com.alibaba.druid.analysis.meta.model.MetaDBColumn;
import lombok.Data;

/**
 * @author LENOVO
 * @date 2024/6/26 15:10
 */
@Data
public class DbTable extends MetaDBColumn {

    /**
     * 原始的表名
     */
    private String originalTableName;
}

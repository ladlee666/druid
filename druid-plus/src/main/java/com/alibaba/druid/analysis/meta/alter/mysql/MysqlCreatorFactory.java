package com.alibaba.druid.analysis.meta.alter.mysql;

import com.alibaba.druid.analysis.meta.alter.AlterCreator;
import com.alibaba.druid.analysis.meta.alter.AlterMode;
import com.alibaba.druid.analysis.meta.alter.CreatorFactory;
import com.alibaba.druid.analysis.meta.model.MetaDBColumn;
import com.mysql.cj.MysqlType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author LENOVO
 * @date 2024/6/24 15:21
 */
public class MysqlCreatorFactory implements CreatorFactory {

    @Override
    public AlterCreator create(AlterMode mode, List<MetaDBColumn> columns) {
        AlterCreator creator = null;
        if (mode == AlterMode.MODIFY) {
            creator = new MysqlModifyAlterCreator(columns);
        } else if (mode == AlterMode.ADD) {
            creator = new MysqlAddAlterCreator(columns);
        } else if (mode == AlterMode.DROP) {
            creator = new MysqlDropAlterCreator(columns);
        } else if (mode == AlterMode.CREATE) {
            creator = new MysqlCreateTableCreator(null);
        } else if (mode == AlterMode.RENAME) {
            creator = new MysqlRenameTableAlterCreator(columns);
        }
        return creator;
    }

    @Override
    public List<String> columnTypeNames() {
        return Arrays.stream(MysqlType.values()).map(MysqlType::getName).collect(Collectors.toList());
    }
}

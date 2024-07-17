package com.alibaba.druid.analysis.meta.alter.mysql;

import com.alibaba.druid.sql.ast.statement.SQLAlterTableAddColumn;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableItem;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.analysis.meta.alter.AlterMode;
import com.alibaba.druid.analysis.meta.model.MetaDBColumn;
import com.alibaba.druid.analysis.spi.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LENOVO
 * @date 2024/6/21 16:38
 */
@Service(name = "MYSQL_RENAME")
public class MysqlAddAlterCreator extends MysqlModifyAlterCreator {

    public MysqlAddAlterCreator(List<MetaDBColumn> columns) {
        super(columns);
    }

    @Override
    protected List<SQLAlterTableItem> getAlterItems(List<MetaDBColumn> metaDBColumns) {
        List<SQLAlterTableItem> items = new ArrayList<>();
        for (MetaDBColumn metaDBColumn : metaDBColumns) {
            SQLAlterTableAddColumn addItem = new SQLAlterTableAddColumn();
            SQLColumnDefinition columnDefinition = createColumnDefinition(metaDBColumn);
            addItem.addColumn(columnDefinition);
            items.add(addItem);
        }
        return items;
    }

    @Override
    protected AlterMode getMode() {
        return AlterMode.ADD;
    }
}

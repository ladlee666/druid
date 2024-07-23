package com.alibaba.druid.analysis.meta.alter.mysql;

import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableDropColumnItem;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableItem;
import com.alibaba.druid.analysis.meta.alter.AlterMode;
import com.alibaba.druid.analysis.meta.model.MetaDBColumn;
import com.alibaba.druid.analysis.spi.Service;

import java.util.Collections;
import java.util.List;

/**
 * @author LENOVO
 * @date 2024/6/21 17:06
 */
@Service(name = "MYSQL_DROP")
public class MysqlDropAlterCreator extends MysqlModifyAlterCreator {

    public MysqlDropAlterCreator(List<MetaDBColumn> columns) {
        super(columns);
    }

    @Override
    protected List<SQLAlterTableItem> getAlterItems(List<MetaDBColumn> metaDBColumns) {
        SQLAlterTableDropColumnItem dropItem = new SQLAlterTableDropColumnItem();
        for (MetaDBColumn metaDBColumn : metaDBColumns) {
            dropItem.addColumn(new SQLIdentifierExpr(metaDBColumn.getColumnName().toUpperCase()));
        }
        List<SQLAlterTableItem> dropTables = Collections.singletonList(dropItem);
        return dropTables;
    }

    @Override
    protected AlterMode getMode() {
        return AlterMode.DROP;
    }
}

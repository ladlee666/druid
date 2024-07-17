package com.alibaba.druid.analysis.meta.alter.oracle;

import com.alibaba.druid.sql.ast.statement.SQLAlterTableAddColumn;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableItem;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.analysis.meta.alter.AlterMode;
import com.alibaba.druid.analysis.meta.alter.chain.AlterContext;
import com.alibaba.druid.analysis.meta.model.MetaDBColumn;
import com.alibaba.druid.analysis.spi.Service;

import java.util.Collections;
import java.util.List;

/**
 * @author LENOVO
 * @date 2024/6/21 11:02
 */
@Service(name = "ORACLE_ADD")
public class OracleAddAlterCreator extends OracleModifyAlterCreator {

    public OracleAddAlterCreator(List<MetaDBColumn> columns) {
        super(columns);
    }

    public OracleAddAlterCreator(AlterContext alterContext) {
        super(alterContext);
    }

    public OracleAddAlterCreator(List<MetaDBColumn> columns, boolean ignoreOldName) {
        super(columns, ignoreOldName);
    }

    @Override
    protected List<SQLAlterTableItem> getAlterItems(List<MetaDBColumn> metaDBColumns) {
        SQLAlterTableAddColumn addItem = new SQLAlterTableAddColumn();
        for (MetaDBColumn metaDBColumn : metaDBColumns) {
            SQLColumnDefinition columnDefinition = createColumnDefinition(metaDBColumn);
            addItem.addColumn(columnDefinition);
        }
        List<SQLAlterTableItem> addList = Collections.singletonList(addItem);
        return addList;
    }

    @Override
    protected AlterMode getMode() {
        return AlterMode.ADD;
    }
}

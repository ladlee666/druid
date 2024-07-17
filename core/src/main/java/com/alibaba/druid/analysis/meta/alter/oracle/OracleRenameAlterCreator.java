package com.alibaba.druid.analysis.meta.alter.oracle;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.analysis.meta.alter.AlterMode;
import com.alibaba.druid.analysis.meta.alter.chain.AlterContext;
import com.alibaba.druid.analysis.meta.model.MetaDBColumn;
import com.alibaba.druid.analysis.meta.model.extend.DbTable;
import com.alibaba.druid.analysis.spi.Service;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * oracle重命名
 * 1.首先修改字段的数据类型
 * ALTER TABLE employees MODIFY (employee_id VARCHAR2(20));
 * <p>
 * 2.然后重命名字段
 * ALTER TABLE employees RENAME COLUMN employee_id TO staff_id;
 *
 * @author LENOVO
 * @date 2024/6/24 10:25
 */
@Service(name = "ORACLE_RENAME")
public class OracleRenameAlterCreator extends OracleModifyAlterCreator {

    public OracleRenameAlterCreator(List<MetaDBColumn> columns) {
        super(columns);
    }

    public OracleRenameAlterCreator(AlterContext context) {
        super(context);
    }

    @Override
    public List<SQLStatement> createStatement(List<MetaDBColumn> columns, String tableName, DbType dbType) {
        if (CollectionUtils.isEmpty(columns)) {
            throw new IllegalArgumentException("[ORACLE_RENAME]:Columns is empty.");
        }
        List<SQLStatement> stmts = new ArrayList<>();
        for (MetaDBColumn metaDBColumn : columns) {
            SQLAlterTableStatement statement = new SQLAlterTableStatement();
            if (metaDBColumn instanceof DbTable) {
                DbTable dbTable = (DbTable) metaDBColumn;
                if (StringUtils.equalsIgnoreCase(dbTable.getOriginalTableName(), dbTable.getTableName())) {
                    continue;
                }
                statement.setTableSource(new SQLExprTableSource(dbTable.getOriginalTableName()));
                statement.setItems(Collections.singletonList(getTableItem(dbTable)));
            } else {
                if (StringUtils.isBlank(metaDBColumn.getOriginalColumnName())
                        || StringUtils.equalsIgnoreCase(metaDBColumn.getOriginalColumnName(), metaDBColumn.getColumnName())) {
                    continue;
                }
                statement.setTableSource(new SQLExprTableSource(tableName));
                statement.setItems(Collections.singletonList(getTableItem(metaDBColumn)));
            }
            statement.setDbType(dbType);
            statement.setAfterSemi(true);
            stmts.add(statement);
        }
        return stmts;
    }

    /**
     * SQLAlterTableRename 修改表
     *
     * @param dbTable
     * @return
     */
    public SQLAlterTableItem getTableItem(DbTable dbTable) {
        if (StringUtils.isBlank(dbTable.getOriginalTableName()) || StringUtils.isBlank(dbTable.getTableName())) {
            throw new IllegalArgumentException("Modifying table names requires passing parameters for table names(originalTableName and tableName).");
        }
        SQLAlterTableRename renameTableRename = new SQLAlterTableRename();
        renameTableRename.setTo(new SQLExprTableSource(dbTable.getTableName()));
        return renameTableRename;
    }

    /**
     * SQLAlterTableRenameColumn 修改字段
     *
     * @param metaDBColumn
     * @return
     */
    public SQLAlterTableItem getTableItem(MetaDBColumn metaDBColumn) {
        SQLAlterTableRenameColumn renameColumn = new SQLAlterTableRenameColumn();
        renameColumn.setColumn(new SQLIdentifierExpr(metaDBColumn.getOriginalColumnName().toUpperCase()));
        renameColumn.setTo(new SQLIdentifierExpr(metaDBColumn.getColumnName().toUpperCase()));
        return renameColumn;
    }

    @Override
    protected DbType getDbType() {
        return DbType.oracle;
    }

    @Override
    protected AlterMode getMode() {
        return AlterMode.RENAME;
    }
}

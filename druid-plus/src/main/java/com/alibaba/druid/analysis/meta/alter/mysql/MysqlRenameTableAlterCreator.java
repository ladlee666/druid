package com.alibaba.druid.analysis.meta.alter.mysql;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLDataType;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableItem;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlRenameTableStatement;
import com.alibaba.druid.analysis.meta.alter.AlterCreator;
import com.alibaba.druid.analysis.meta.alter.AlterMode;
import com.alibaba.druid.analysis.meta.model.MetaDBColumn;
import com.alibaba.druid.analysis.meta.model.extend.DbTable;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LENOVO
 * @date 2024/6/26 15:32
 */
public class MysqlRenameTableAlterCreator extends AlterCreator {

    public MysqlRenameTableAlterCreator(List<MetaDBColumn> columns) {
        super(columns);
    }

    @Override
    protected SQLDataType createDataType(MetaDBColumn metaDBColumn) {
        throw new UnsupportedOperationException("Operation not allowed.");
    }

    @Override
    protected SQLColumnDefinition createColumnDefinition(MetaDBColumn metaDBColumn) {
        throw new UnsupportedOperationException("Operation not allowed.");
    }

    @Override
    protected List<SQLAlterTableItem> getAlterItems(List<MetaDBColumn> metaDBColumns) {
        throw new UnsupportedOperationException("Operation not allowed.");
    }

    @Override
    public List<SQLStatement> createStatement(List<MetaDBColumn> columns, String tableName, DbType dbType) {
        if (CollectionUtils.isEmpty(columns)) {
            throw new IllegalArgumentException("[MYSQL_TABLE_RENAME]:Columns is empty.");
        }
        List<SQLStatement> stmts = new ArrayList<>();
        for (MetaDBColumn column : columns) {
            if (!(column instanceof DbTable)) {
                continue;
            }
            DbTable dbTable = (DbTable) column;
            if (StringUtils.isBlank(dbTable.getOriginalTableName())
                    || StringUtils.isBlank(dbTable.getTableName())
                    || StringUtils.equalsIgnoreCase(dbTable.getOriginalTableName(), dbTable.getTableName())) {
                continue;
            }
            MySqlRenameTableStatement renameTableStatement = new MySqlRenameTableStatement();
            MySqlRenameTableStatement.Item item = new MySqlRenameTableStatement.Item();
            item.setName(new SQLIdentifierExpr(dbTable.getOriginalTableName()));
            item.setTo(new SQLIdentifierExpr(dbTable.getTableName()));
            renameTableStatement.addItem(item);
            stmts.add(renameTableStatement);
        }
        return stmts;
    }

    @Override
    protected DbType getDbType() {
        return DbType.mysql;
    }

    @Override
    protected AlterMode getMode() {
        return AlterMode.RENAME;
    }
}

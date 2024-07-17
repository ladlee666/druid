package com.alibaba.druid.analysis.meta.alter;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLDataType;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableItem;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.analysis.meta.alter.chain.AlterContext;
import com.alibaba.druid.analysis.meta.model.MetaDBColumn;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author LENOVO
 * @date 2024/6/20 17:25
 */
public abstract class AlterCreator {

    private AlterCreator nextCreator;

    public List<MetaDBColumn> columns;

    public AlterCreator(List<MetaDBColumn> columns) {
        this.columns = columns;
    }

    public void setColumns(List<MetaDBColumn> columns) {
        this.columns = columns;
    }

    /**
     * 获取字段类型
     *
     * @return
     */
    protected abstract SQLDataType createDataType(MetaDBColumn metaDBColumn);

    protected abstract SQLColumnDefinition createColumnDefinition(MetaDBColumn metaDBColumn);

    /**
     * SQLAlterTableStatement 中的items属性的class类型
     * oracle:
     * mysql: MySqlAlterTableModifyColumn
     *
     * @return
     */
    protected abstract List<SQLAlterTableItem> getAlterItems(List<MetaDBColumn> metaDBColumns);

    protected String getTableName() {
        List<String> tables = columns.stream()
                .map(MetaDBColumn::getTableName)
                .distinct()
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        if (tables.size() > 1) {
            throw new IllegalArgumentException("The column which need to be modified obtain two table.");
        }
        if (tables.size() == 0) {
            throw new IllegalArgumentException("We need at least one table.");
        }
        return tables.get(0);
    }

    protected abstract DbType getDbType();

    protected abstract AlterMode getMode();

    protected String getCreateTableScript(Connection connection, List<String> tables) {
        throw new UnsupportedOperationException("Operation not allowed.");
    }

    public List<SQLStatement> createAlterStatement() {
        return createStatement(columns, getTableName(), getDbType());
    }

    public void createChain(AlterContext context) {
        List<SQLStatement> stmts = createStatement(context.getMetaDBColumns(), context.getTableName(), context.getDbType());
        if (CollectionUtils.isNotEmpty(stmts)) {
            context.getStmts().addAll(stmts);
        }
        createNext(context);
    }

    public List<SQLStatement> createStatement(List<MetaDBColumn> columns, String tableName, DbType dbType) {
        if (CollectionUtils.isEmpty(columns)) {
            throw new IllegalArgumentException("column is empty.");
        }
        List<SQLAlterTableItem> alterItems = getAlterItems(columns);
        if (CollectionUtils.isEmpty(alterItems)) {
            return Collections.emptyList();
        }
        SQLAlterTableStatement statement = new SQLAlterTableStatement();
        statement.setItems(alterItems);
        statement.setTableSource(new SQLExprTableSource(tableName));
        statement.setDbType(dbType);
        statement.setAfterSemi(true);
        return Collections.singletonList(statement);
    }

    public static AlterCreator link(AlterCreator first, AlterCreator... chains) {
        AlterCreator head = first;
        for (AlterCreator chain : chains) {
            head.nextCreator = chain;
            head = chain;
        }
        return first;
    }

    protected void createNext(AlterContext context) {
        if (nextCreator == null) {
            return;
        }
        nextCreator.createChain(context);
    }

}
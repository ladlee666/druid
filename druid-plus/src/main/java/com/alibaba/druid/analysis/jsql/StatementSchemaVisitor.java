package com.alibaba.druid.analysis.jsql;

import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.*;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterSession;
import net.sf.jsqlparser.statement.alter.AlterSystemStatement;
import net.sf.jsqlparser.statement.alter.RenameTableStatement;
import net.sf.jsqlparser.statement.alter.sequence.AlterSequence;
import net.sf.jsqlparser.statement.analyze.Analyze;
import net.sf.jsqlparser.statement.comment.Comment;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.schema.CreateSchema;
import net.sf.jsqlparser.statement.create.sequence.CreateSequence;
import net.sf.jsqlparser.statement.create.synonym.CreateSynonym;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.AlterView;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.grant.Grant;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.refresh.RefreshMaterializedViewStatement;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.show.ShowIndexStatement;
import net.sf.jsqlparser.statement.show.ShowTablesStatement;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author LENOVO
 * @date 2024/6/3 9:58
 */
public class StatementSchemaVisitor implements StatementVisitor, SelectVisitor, FromItemVisitor {

    private final Set<String> tableNames = new HashSet<>();

    public Set<String> getTableNames() {
        return tableNames;
    }

    @Override
    public void visit(Select select) {
        select.accept((SelectVisitor) this);
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        FromItem fromItem = plainSelect.getFromItem();
        fromItem.accept(this);
        List<Join> joins = plainSelect.getJoins();
        if (CollectionUtils.isNotEmpty(joins)) {
            for (Join join : joins) {
                FromItem joinFromItem = join.getFromItem();
                joinFromItem.accept(this);
                FromItem rightItem = join.getRightItem();
                rightItem.accept(this);
            }
        }
    }

    /**
     * union all
     *
     * @param setOperationList
     */
    @Override
    public void visit(SetOperationList setOperationList) {
        List<Select> selects = setOperationList.getSelects();
        if (CollectionUtils.isNotEmpty(selects)) {
            for (Select select : selects) {
                select.accept((SelectVisitor) this);
            }
        }
    }

    @Override
    public void visit(WithItem withItem) {

    }

    @Override
    public void visit(Values values) {

    }

    @Override
    public void visit(TableStatement tableStatement) {
        Table table = tableStatement.getTable();
        visitorTable(table);
    }

    @Override
    public void visit(Analyze analyze) {

    }

    @Override
    public void visit(SavepointStatement savepointStatement) {

    }

    @Override
    public void visit(RollbackStatement rollbackStatement) {

    }

    @Override
    public void visit(Comment comment) {
        visitorTable(comment.getTable());
        visitorColumn(comment.getColumn());
    }

    @Override
    public void visit(Commit commit) {

    }

    @Override
    public void visit(Delete delete) {
        List<Table> tables = delete.getTables();
        if (CollectionUtils.isNotEmpty(tables)) {
            for (Table table : tables) {
                visitorTable(table);
            }
        }
        List<Join> joins = delete.getJoins();
        for (Join join : joins) {
            FromItem joinFromItem = join.getFromItem();
            joinFromItem.accept(this);
            FromItem rightItem = join.getRightItem();
            rightItem.accept(this);
        }
    }

    @Override
    public void visit(Update update) {
        Table table = update.getTable();
        visitorTable(table);
        FromItem fromItem = update.getFromItem();
        if (fromItem != null) {
            fromItem.accept(this);
        }
    }

    @Override
    public void visit(Insert insert) {

    }

    @Override
    public void visit(Drop drop) {

    }

    @Override
    public void visit(Truncate truncate) {

    }

    @Override
    public void visit(CreateIndex createIndex) {

    }

    @Override
    public void visit(CreateSchema createSchema) {

    }

    @Override
    public void visit(CreateTable createTable) {
        Table table = createTable.getTable();
        tableNames.add(table.getName());
    }

    @Override
    public void visit(CreateView createView) {

    }

    @Override
    public void visit(AlterView alterView) {

    }

    @Override
    public void visit(RefreshMaterializedViewStatement refreshMaterializedViewStatement) {

    }

    @Override
    public void visit(Alter alter) {
        Table table = alter.getTable();
        visitorTable(table);
    }

    @Override
    public void visit(Statements statements) {
        for (Statement statement : statements) {
            statement.accept(this);
        }
    }

    @Override
    public void visit(Execute execute) {

    }

    @Override
    public void visit(SetStatement setStatement) {

    }

    @Override
    public void visit(ResetStatement resetStatement) {

    }

    @Override
    public void visit(ShowColumnsStatement showColumnsStatement) {

    }

    @Override
    public void visit(ShowIndexStatement showIndexStatement) {

    }

    @Override
    public void visit(ShowTablesStatement showTablesStatement) {

    }

    @Override
    public void visit(Merge merge) {

    }

    @Override
    public void visit(Upsert upsert) {

    }

    @Override
    public void visit(UseStatement useStatement) {

    }

    @Override
    public void visit(Block block) {

    }

    @Override
    public void visit(DescribeStatement describeStatement) {

    }

    @Override
    public void visit(ExplainStatement explainStatement) {

    }

    @Override
    public void visit(ShowStatement showStatement) {

    }

    @Override
    public void visit(DeclareStatement declareStatement) {

    }

    @Override
    public void visit(Grant grant) {

    }

    @Override
    public void visit(CreateSequence createSequence) {

    }

    @Override
    public void visit(AlterSequence alterSequence) {

    }

    @Override
    public void visit(CreateFunctionalStatement createFunctionalStatement) {

    }

    @Override
    public void visit(CreateSynonym createSynonym) {

    }

    @Override
    public void visit(AlterSession alterSession) {

    }

    @Override
    public void visit(IfElseStatement ifElseStatement) {

    }

    @Override
    public void visit(RenameTableStatement renameTableStatement) {

    }

    @Override
    public void visit(PurgeStatement purgeStatement) {

    }

    @Override
    public void visit(AlterSystemStatement alterSystemStatement) {

    }

    @Override
    public void visit(UnsupportedStatement unsupportedStatement) {
        //
    }

    @Override
    public void visit(Table table) {
        visitorTable(table);
    }

    @Override
    public void visit(ParenthesedSelect parenthesedSelect) {
        Select select = parenthesedSelect.getSelect();
        select.accept((SelectVisitor) this);
    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect) {

    }

    @Override
    public void visit(TableFunction tableFunction) {

    }

    @Override
    public void visit(ParenthesedFromItem parenthesedFromItem) {

    }

    public void visitorTable(Table table) {
        if (table != null && StringUtils.isNotBlank(table.getName())) {
            tableNames.add(table.getName());
        }
    }

    public void visitorColumn(Column column) {
        if (column != null) {
            Table table = column.getTable();
            if (table != null && StringUtils.isNotBlank(table.getName())) {
                tableNames.add(table.getName());
            }
        }
    }
}

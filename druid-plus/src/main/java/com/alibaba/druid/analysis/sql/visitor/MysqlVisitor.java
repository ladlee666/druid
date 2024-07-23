package com.alibaba.druid.analysis.sql.visitor;

import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;
import com.alibaba.druid.analysis.spi.Scope;
import com.alibaba.druid.analysis.spi.Service;
import com.alibaba.druid.analysis.sql.model.TableModel;
import com.alibaba.druid.analysis.sql.model.VariantModel;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service(name = "mysql", scope = Scope.PROTOTYPE)
public class MysqlVisitor extends MySqlASTVisitorAdapter implements CustomVisitor {

    private final List<VariantModel> variantModels = new ArrayList<>();
    private final Set<TableModel> tableNames = new HashSet<>();
    private boolean initialized = false;

    @Override
    public boolean visit(MySqlSelectQueryBlock x) {
        if (initialized) {
            return false;
        }
        getTableInfoList(x.getFrom());
        return true;
    }

    @Override
    public List<VariantModel> getVariantModels() {
        return variantModels;
    }

    @Override
    public Set<TableModel> getTableNames() {
        return tableNames;
    }

    protected void getTableInfoList(SQLTableSource tableSource) {
        getTableInfoList(tableSource, Boolean.TRUE);
    }

    private void getTableInfoList(SQLTableSource tableSource, Boolean isGetRight) {
        if (tableSource instanceof SQLSubqueryTableSource) {
            tableNames.add(new TableModel(EMPTY_TABLE_KEY, true, tableSource.getAlias()));
        }

        if (tableSource instanceof SQLJoinTableSource) {
            SQLJoinTableSource joinSource = (SQLJoinTableSource) tableSource;
            getTableInfoList(joinSource.getLeft(), isGetRight);
            if (isGetRight) {
                getTableInfoList(joinSource.getRight(), true);
            }
        }
        if (tableSource instanceof SQLExprTableSource) {
            SQLExprTableSource tableReference = (SQLExprTableSource) tableSource;
            tableNames.add(new TableModel(tableReference.getTableName(), false, tableReference.getAlias()));
        }
    }

    @Override
    public void init() {
        initialized = true;
    }

    @Override
    public boolean initStats() {
        return initialized;
    }
}

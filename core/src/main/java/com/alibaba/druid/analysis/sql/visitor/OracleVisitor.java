package com.alibaba.druid.analysis.sql.visitor;

import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleSelectJoin;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleSelectQueryBlock;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleSelectSubqueryTableSource;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleSelectTableReference;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleASTVisitorAdapter;
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
@Service(name = "oracle", scope = Scope.PROTOTYPE)
public class OracleVisitor extends OracleASTVisitorAdapter implements CustomVisitor {

    private final List<VariantModel> variantModels = new ArrayList<>();
    private final Set<TableModel> tableNames = new HashSet<>();
    private boolean initialized = false;

    @Override
    public boolean visit(OracleSelectQueryBlock x) {
        if (initialized) {
            return true;
        }
        getTableInfoList(x.getFrom());
        return true;
    }

    protected void getTableInfoList(SQLTableSource tableSource) {
        getTableInfoList(tableSource, Boolean.TRUE);
    }

    private void getTableInfoList(SQLTableSource tableSource, Boolean isGetRight) {
        if (tableSource instanceof OracleSelectSubqueryTableSource) {
            tableNames.add(new TableModel(EMPTY_TABLE_KEY, true, tableSource.getAlias()));
        }

        if (tableSource instanceof OracleSelectJoin) {
            OracleSelectJoin joinSource = (OracleSelectJoin) tableSource;
            getTableInfoList(joinSource.getLeft(), isGetRight);
            if (isGetRight) {
                getTableInfoList(joinSource.getRight(), true);
            }
        }
        if (tableSource instanceof OracleSelectTableReference) {
            OracleSelectTableReference tableReference = (OracleSelectTableReference) tableSource;
            tableNames.add(new TableModel(tableReference.getTableName(), false, tableReference.getAlias()));
        }
    }

    @Override
    public List<VariantModel> getVariantModels() {
        return variantModels;
    }

    @Override
    public Set<TableModel> getTableNames() {
        return tableNames;
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

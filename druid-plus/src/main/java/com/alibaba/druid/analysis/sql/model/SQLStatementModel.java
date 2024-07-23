package com.alibaba.druid.analysis.sql.model;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLCallStatement;
import com.alibaba.druid.sql.parser.SQLType;
import lombok.Data;

import java.util.List;

@Data
public class SQLStatementModel {

    private SQLStatement sqlStatement;

    private SQLStatement countSqlStatement;

    private List<VariantModel> variantModels;

    private List<String> keys;

    private SQLType sqlType;

    private boolean isLastSql;

    private boolean executed;

    private boolean isProcedure;

    /**
     * oracle PL/SQL 输出
     */
    private boolean dbmsOutput;

    private List<Object> autoIds;

    public SQLStatementModel() {
    }

    public SQLStatementModel(SQLStatement sqlStatement, SQLStatement countSqlStatement,
                             List<VariantModel> variantModels, List<String> keys, SQLType sqlType,
                             boolean isLastSql, List<Object> autoIds,boolean dbmsOutput) {
        this.sqlStatement = sqlStatement;
        this.countSqlStatement = countSqlStatement;
        this.variantModels = variantModels;
        this.keys = keys;
        this.sqlType = sqlType;
        this.isLastSql = isLastSql;
        this.isProcedure = this.sqlStatement instanceof SQLCallStatement;
        this.autoIds = autoIds;
        this.dbmsOutput = dbmsOutput;
    }
}

package com.alibaba.druid.analysis.meta.alter.oracle;

import cn.hutool.core.util.NumberUtil;
import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLDataType;
import com.alibaba.druid.sql.ast.SQLDataTypeImpl;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.expr.SQLNumberExpr;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.dialect.oracle.ast.expr.OracleSysdateExpr;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleAlterTableModify;
import com.alibaba.druid.analysis.meta.alter.AlterCreator;
import com.alibaba.druid.analysis.meta.alter.AlterMode;
import com.alibaba.druid.analysis.meta.alter.chain.AlterContext;
import com.alibaba.druid.analysis.meta.model.MetaDBColumn;
import com.alibaba.druid.analysis.spi.Service;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author LENOVO
 * @date 2024/6/20 18:07
 */
@Service(name = "ORACLE_MODIFY")
public class OracleModifyAlterCreator extends AlterCreator {

    private boolean ignoreOldName = false;

    public OracleModifyAlterCreator(List<MetaDBColumn> columns) {
        super(columns);
    }

    public OracleModifyAlterCreator(List<MetaDBColumn> columns, boolean ignoreOldName) {
        super(columns);
        this.ignoreOldName = ignoreOldName;
    }

    public OracleModifyAlterCreator(AlterContext alterContext) {
        super(alterContext.getMetaDBColumns());
    }

    /**
     * @return
     */
    @Override
    protected SQLDataType createDataType(MetaDBColumn metaDBColumn) {
        OracleCreatorFactory.OracleDataType of = OracleCreatorFactory.OracleDataType.of(metaDBColumn.getTypeName().toUpperCase());
        // 长度
        int len = metaDBColumn.getColumnSize();
        if (of == OracleCreatorFactory.OracleDataType.VARCHAR2
                || of == OracleCreatorFactory.OracleDataType.NVARCHAR
                || of == OracleCreatorFactory.OracleDataType.NCHAR
                || of == OracleCreatorFactory.OracleDataType.CHAR) {
            SQLCharacterDataType sqlCharacterDataType;
            if (len > 0) {
                if (len > 4000) {
                    len = 4000;
                }
                sqlCharacterDataType = new SQLCharacterDataType(metaDBColumn.getTypeName(), len);
            } else {
                sqlCharacterDataType = new SQLCharacterDataType(metaDBColumn.getTypeName(), 255);
            }
            return sqlCharacterDataType;
        } else {
            SQLDataTypeImpl sqlDataType;
            if (len > 0) {
                sqlDataType = new SQLDataTypeImpl(metaDBColumn.getTypeName(), len);
            } else {
                sqlDataType = new SQLDataTypeImpl(metaDBColumn.getTypeName());
            }
            // 小数点
            Integer digit = metaDBColumn.getDigit();
            if (digit != null && digit > 0) {
                sqlDataType.addArgument(new SQLIntegerExpr(digit));
            }
            return sqlDataType;
        }
    }

    @Override
    protected SQLColumnDefinition createColumnDefinition(MetaDBColumn metaDBColumn) {
        SQLColumnDefinition columnDefinition = new SQLColumnDefinition();
        columnDefinition.setDbType(getDbType());
        if (StringUtils.isNotBlank(metaDBColumn.getOriginalColumnName()) && !ignoreOldName) {
            columnDefinition.setName(metaDBColumn.getOriginalColumnName().toUpperCase());
        } else {
            columnDefinition.setName(metaDBColumn.getColumnName().toUpperCase());
        }
        columnDefinition.setDataType(createDataType(metaDBColumn));
        Boolean necessary = metaDBColumn.getNecessary();
        if (necessary != null) {
            if (necessary) {
                columnDefinition.addConstraint(new SQLNotNullConstraint());
            } else {
                columnDefinition.addConstraint(new SQLNullConstraint());
            }
        }
        if (StringUtils.isNotBlank(metaDBColumn.getColumnDef())) {
            if (columnDefinition.getDataType() instanceof SQLCharacterDataType) {
                columnDefinition.setDefaultExpr(new SQLCharExpr(metaDBColumn.getColumnDef()));
            } else {
                OracleCreatorFactory.OracleDataType of = OracleCreatorFactory.OracleDataType.of(metaDBColumn.getTypeName().toUpperCase());
                if (of == OracleCreatorFactory.OracleDataType.DATE || of == OracleCreatorFactory.OracleDataType.TIMESTAMP) {
                    columnDefinition.setDefaultExpr(new OracleSysdateExpr());
                } else {
                    boolean number = NumberUtil.isNumber(metaDBColumn.getColumnDef());
                    if (number) {
                        columnDefinition.setDefaultExpr(new SQLNumberExpr(NumberUtil.parseNumber(metaDBColumn.getColumnDef())));
                    } else {
                        columnDefinition.setDefaultExpr(new SQLCharExpr(metaDBColumn.getColumnDef()));
                    }
                }
            }
        }
        return columnDefinition;
    }

    @Override
    protected List<SQLAlterTableItem> getAlterItems(List<MetaDBColumn> metaDBColumns) {
        List<SQLColumnDefinition> columnDefinitions = new ArrayList<>();
        for (MetaDBColumn metaDBColumn : metaDBColumns) {
            SQLColumnDefinition columnDefinition = createColumnDefinition(metaDBColumn);
            if (columnDefinition == null) {
                continue;
            }
            columnDefinitions.add(columnDefinition);
        }
        if (CollectionUtils.isEmpty(columnDefinitions)) {
            return null;
        }
        OracleAlterTableModify modifyItem = new OracleAlterTableModify();
        for (SQLColumnDefinition columnDefinition : columnDefinitions) {
            modifyItem.addColumn(columnDefinition);
        }
        return Collections.singletonList(modifyItem);
    }

    @Override
    protected DbType getDbType() {
        return DbType.oracle;
    }

    @Override
    protected AlterMode getMode() {
        return AlterMode.MODIFY;
    }

}

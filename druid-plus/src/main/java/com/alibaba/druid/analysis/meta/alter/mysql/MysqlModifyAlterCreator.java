package com.alibaba.druid.analysis.meta.alter.mysql;

import cn.hutool.core.util.NumberUtil;
import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLCurrentTimeExpr;
import com.alibaba.druid.sql.ast.SQLDataType;
import com.alibaba.druid.sql.ast.SQLDataTypeImpl;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.expr.SQLNumberExpr;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlAlterTableChangeColumn;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlAlterTableModifyColumn;
import com.alibaba.druid.analysis.meta.alter.AlterCreator;
import com.alibaba.druid.analysis.meta.alter.AlterMode;
import com.alibaba.druid.analysis.meta.model.MetaDBColumn;
import com.alibaba.druid.analysis.spi.Service;
import com.mysql.cj.MysqlType;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * @author LENOVO
 * @date 2024/6/21 14:17
 */
@Service(name = "MYSQL_MODIFY")
public class MysqlModifyAlterCreator extends AlterCreator {

    public MysqlModifyAlterCreator(List<MetaDBColumn> columns) {
        super(columns);
    }

    @Override
    protected SQLDataType createDataType(MetaDBColumn metaDBColumn) {
        int len = metaDBColumn.getColumnSize();
        String typeName = metaDBColumn.getTypeName().toUpperCase();
        MysqlType mysqlType = MysqlType.valueOf(typeName);
        switch (mysqlType) {
            case VARCHAR:
            case ENUM:
            case TINYTEXT:
            case MEDIUMTEXT:
            case LONGTEXT:
            case TEXT:
            case CHAR:
                SQLCharacterDataType characterDataType;
                if (len > 0) {
                    characterDataType = new SQLCharacterDataType(typeName, len);
                } else {
                    characterDataType = new SQLCharacterDataType(typeName);
                }
                return characterDataType;
            default:
                SQLDataTypeImpl sqlDataType;
                if (len > 0) {
                    sqlDataType = new SQLDataTypeImpl(typeName, len);
                } else {
                    sqlDataType = new SQLDataTypeImpl(typeName);
                }
                // 如果有小数点
                Integer digit = metaDBColumn.getDigit();
                if (digit != null && digit > 0) {
                    sqlDataType.addArgument(new SQLIntegerExpr(digit));
                }
                return sqlDataType;
        }
    }

    @Override
    protected SQLColumnDefinition createColumnDefinition(MetaDBColumn metaDBColumn) {
        SQLColumnDefinition definition = new SQLColumnDefinition();
        definition.setDbType(getDbType());
        definition.setName(metaDBColumn.getColumnName().toLowerCase());
        definition.setDataType(createDataType(metaDBColumn));
        Boolean necessary = metaDBColumn.getNecessary();
        if (necessary != null) {
            if (necessary) {
                definition.addConstraint(new SQLNotNullConstraint());
            } else {
                definition.addConstraint(new SQLNullConstraint());
            }
        }
        if (StringUtils.isNotBlank(metaDBColumn.getRemarks())) {
            definition.setComment(metaDBColumn.getRemarks());
        }
        if (StringUtils.isNotBlank(metaDBColumn.getColumnDef())) {
            if (definition.getDataType() instanceof SQLCharacterDataType) {
                definition.setDefaultExpr(new SQLCharExpr(metaDBColumn.getColumnDef()));
            } else {
                MysqlType mysqlType = MysqlType.valueOf(metaDBColumn.getTypeName().toUpperCase());
                if (mysqlType == MysqlType.DATE || mysqlType == MysqlType.TIME) {
                    mysqlType = MysqlType.TIMESTAMP;
                }
                if (mysqlType == MysqlType.DATETIME || mysqlType == MysqlType.TIMESTAMP) {
                    SQLCurrentTimeExpr.Type timeType = SQLCurrentTimeExpr.Type.valueOf(metaDBColumn.getColumnDef().toUpperCase());
                    definition.setDefaultExpr(new SQLCurrentTimeExpr(timeType));
                } else {
                    boolean number = NumberUtil.isNumber(metaDBColumn.getColumnDef());
                    if (number) {
                        definition.setDefaultExpr(new SQLNumberExpr(NumberUtil.parseNumber(metaDBColumn.getColumnDef())));
                    } else {
                        definition.setDefaultExpr(new SQLCharExpr(metaDBColumn.getColumnDef()));
                    }
                }
            }
        }
        return definition;
    }

    @Override
    protected List<SQLAlterTableItem> getAlterItems(List<MetaDBColumn> metaDBColumns) {
        List<SQLAlterTableItem> items = new ArrayList<>();
        for (MetaDBColumn metaDBColumn : metaDBColumns) {
            if (StringUtils.isNotBlank(metaDBColumn.getOriginalColumnName())
                    && !StringUtils.equals(metaDBColumn.getOriginalColumnName(), metaDBColumn.getColumnName())) {
                MySqlAlterTableChangeColumn changeColumn = new MySqlAlterTableChangeColumn();
                changeColumn.setColumnName(new SQLIdentifierExpr(metaDBColumn.getOriginalColumnName().toLowerCase()));
                SQLColumnDefinition columnDefinition = createColumnDefinition(metaDBColumn);
                changeColumn.setNewColumnDefinition(columnDefinition);
                items.add(changeColumn);
            } else {
                MySqlAlterTableModifyColumn modifyItem = new MySqlAlterTableModifyColumn();
                SQLColumnDefinition columnDefinition = createColumnDefinition(metaDBColumn);
                modifyItem.setNewColumnDefinition(columnDefinition);
                items.add(modifyItem);
            }
        }
        return items;
    }

    @Override
    protected DbType getDbType() {
        return DbType.mysql;
    }

    @Override
    protected AlterMode getMode() {
        return AlterMode.MODIFY;
    }

}

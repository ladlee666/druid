package com.alibaba.druid.analysis.meta.model;

import cn.hutool.core.util.BooleanUtil;
import com.alibaba.druid.analysis.meta.bean.FieldComparison;
import com.alibaba.druid.analysis.meta.constants.ColumnLabel;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

import static com.alibaba.druid.analysis.meta.constants.ColumnLabel.EMPTY;

/**
 * @author LENOVO
 * @date 2024/6/5 16:39
 */
@Data
public class MetaDBColumn {

    @FieldComparison(codeName = "表名")
    private String tableName;

    @FieldComparison(codeName = "字段名")
    private String columnName;

    @FieldComparison(codeName = "字段类型名")
    private String typeName;

    @FieldComparison(codeName = "字段大小/长度")
    private int columnSize;
    /**
     * 小数位
     */
    @FieldComparison(codeName = "字段小数位")
    private Integer digit;
    /**
     * 是否可为空
     */
    @FieldComparison(codeName = "是否非空")
    private Boolean necessary;

    @FieldComparison(codeName = "备注")
    private String remarks;

    @FieldComparison(codeName = "字段默认值")
    private String columnDef;

    @FieldComparison(codeName = "是否主键")
    private boolean isPk;

    /**
     * 是否自增
     */
    @FieldComparison(codeName = "是否自增")
    private boolean autoIncrement;

    /**
     * 原始的字段名称
     */
    @FieldComparison(codeName = "原始的字段名称")
    private String originalColumnName;

    public MetaDBColumn() {
    }

    public MetaDBColumn(MetaDBTable table, String tableName, ResultSet columnMetaRs) {
        try {
            init(table, tableName, columnMetaRs);
        } catch (SQLException e) {
            throw new RuntimeException(String.format("Get table [%s] meta info error!", tableName));
        }
    }

    public MetaDBColumn(String tableName, String columnName, String typeName,
                        int columnSize, Integer digit, boolean necessary, String remarks,
                        String columnDef, boolean isPk, String originalColumnName) {
        this.tableName = tableName;
        this.columnName = columnName;
        this.typeName = typeName;
        this.columnSize = columnSize;
        this.digit = digit;
        this.necessary = necessary;
        this.remarks = remarks;
        this.columnDef = columnDef;
        this.isPk = isPk;
        this.originalColumnName = originalColumnName;
    }

    public void init(MetaDBTable table, String tableName, ResultSet columnMetaRs) throws SQLException {
        this.tableName = tableName;
        this.columnName = columnMetaRs.getString(ColumnLabel.COLUMN_NAME);
        this.typeName = columnMetaRs.getString(ColumnLabel.COLUMN_TYPE);
        this.columnSize = columnMetaRs.getInt(ColumnLabel.COLUMN_SIZE);
        // 是否必须的
        this.necessary = !columnMetaRs.getBoolean(ColumnLabel.NULLABLE);
        String remarks = columnMetaRs.getString(ColumnLabel.REMARKS);
        if (StringUtils.isNotBlank(remarks)) {
            this.remarks = remarks;
        } else {
            this.remarks = EMPTY;
        }
        String defaultValue = columnMetaRs.getString(ColumnLabel.COLUMN_DEF);
        this.columnDef = StringUtils.trim(StringUtils.remove(defaultValue, "'"));
        try {
            // 保留小数位数
            this.digit = columnMetaRs.getInt(ColumnLabel.DECIMAL_DIGITS);
        } catch (SQLException ignore) {
            //某些驱动可能不支持，跳过
        }
        try {
            // 是否自增
            String auto = columnMetaRs.getString(ColumnLabel.IS_AUTOINCREMENT);
            if (BooleanUtil.toBoolean(auto)) {
                this.autoIncrement = true;
            }
        } catch (SQLException ignore) {
            //某些驱动可能不支持，跳过
        }
        if (table != null) {
            this.isPk = table.isPk(columnName);
        }
    }

    public static MetaDBColumn create(MetaDBTable table, String tableName, ResultSet columnMetaRs) {
        return new MetaDBColumn(table, tableName, columnMetaRs);
    }
}

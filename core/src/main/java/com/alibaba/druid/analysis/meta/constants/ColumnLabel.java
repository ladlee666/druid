package com.alibaba.druid.analysis.meta.constants;

/**
 * @author LENOVO
 * @date 2024/6/14 17:43
 */
public interface ColumnLabel {

    /**
     * 主键名称
     */
    String PK_NAME = "PK_NAME";
    /**
     * 列在联合主键中的顺序
     */
    String KEY_SEQ = "KEY_SEQ";
    String TABLE_NAME = "TABLE_NAME";
    String COLUMN_NAME = "COLUMN_NAME";
    String COLUMN_TYPE = "TYPE_NAME";
    String COLUMN_SIZE = "COLUMN_SIZE";
    String DECIMAL_DIGITS = "DECIMAL_DIGITS";
    String NULLABLE = "NULLABLE";
    String REMARKS = "REMARKS";
    String COLUMN_DEF = "COLUMN_DEF";
    String IS_AUTOINCREMENT = "IS_AUTOINCREMENT";

    String EMPTY = "";
}

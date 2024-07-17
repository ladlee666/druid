package com.alibaba.druid.analysis.meta.model;

import com.alibaba.druid.analysis.meta.constants.ColumnLabel;
import lombok.Data;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 主键
 *
 * @author LENOVO
 * @date 2024/6/19 18:23
 */
@Data
public class Pk {

    private String pkName;

    private short keySeq;

    private String columnName;

    public static Pk create(ResultSet rs) throws SQLException {
        return new Pk(rs.getString(ColumnLabel.PK_NAME), rs.getShort(ColumnLabel.KEY_SEQ), rs.getString(ColumnLabel.COLUMN_NAME));
    }

    public Pk() {
    }

    public Pk(String pkName, short keySeq, String columnName) {
        this.pkName = pkName;
        this.keySeq = keySeq;
        this.columnName = columnName;
    }
}

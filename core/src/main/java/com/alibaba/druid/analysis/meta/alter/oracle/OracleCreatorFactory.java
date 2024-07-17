package com.alibaba.druid.analysis.meta.alter.oracle;

import com.alibaba.druid.analysis.meta.alter.AlterCreator;
import com.alibaba.druid.analysis.meta.alter.AlterMode;
import com.alibaba.druid.analysis.meta.alter.CreatorFactory;
import com.alibaba.druid.analysis.meta.model.MetaDBColumn;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author LENOVO
 * @date 2024/6/24 15:19
 */
public class OracleCreatorFactory implements CreatorFactory {

    @Override
    public AlterCreator create(AlterMode mode, List<MetaDBColumn> columns) {
        AlterCreator creator = null;
        if (mode == AlterMode.ADD) {
            creator = new OracleAddAlterCreator(columns, true);
        } else if (mode == AlterMode.MODIFY) {
            creator = new OracleModifyAlterCreator(columns);
        } else if (mode == AlterMode.DROP) {
            creator = new OracleDropAlterCreator(columns);
        } else if (mode == AlterMode.RENAME) {
            creator = new OracleRenameAlterCreator(columns);
        } else if (mode == AlterMode.CREATE) {
            creator = new OracleCreateTableCreator(null);
        }
        return creator;
    }

    @Override
    public List<String> columnTypeNames() {
        return Arrays.stream(OracleDataType.values()).map(OracleDataType::name).collect(Collectors.toList());
    }

    public enum OracleDataType {
        VARCHAR2,
        NVARCHAR,
        NUMBER,
        FLOAT,
        LONG,
        DATE,
        TIMESTAMP,
        CHAR,
        NCHAR,
        CLOB,
        NCLOB,
        BLOB;

        public static OracleDataType of(String name) {
            if (StringUtils.isBlank(name)) {
                return null;
            }
            try {
                return valueOf(name);
            } catch (Exception e) {
                return null;
            }
        }
    }
}

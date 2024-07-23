package com.alibaba.druid.analysis.meta.alter;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.analysis.meta.ConnectionHolder;
import com.alibaba.druid.analysis.meta.MetaDBHelper;
import com.alibaba.druid.analysis.meta.alter.chain.AlterContext;
import com.alibaba.druid.analysis.meta.alter.chain.Trigger;
import com.alibaba.druid.analysis.meta.alter.oracle.OracleAddAlterCreator;
import com.alibaba.druid.analysis.meta.alter.oracle.OracleCommentCreator;
import com.alibaba.druid.analysis.meta.alter.oracle.OracleModifyAlterCreator;
import com.alibaba.druid.analysis.meta.alter.oracle.OracleRenameAlterCreator;
import com.alibaba.druid.analysis.meta.model.MetaDBColumn;
import com.alibaba.druid.analysis.meta.model.MetaDBInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author LENOVO
 * @date 2024/6/21 11:43
 */
public class AlterLoader {

    /**
     * 有问题的写法
     * 为了oracle修改字段名称同时修改类型，现在集成到{@link OracleRenameAlterCreator}
     *
     * @param dbType
     * @param mode
     * @param columns
     * @return
     */
    @Deprecated
    public static String print2Sql(String dbType, AlterMode mode, List<MetaDBColumn> columns) {
        DbType type = DbType.valueOf(dbType);
        if (type == DbType.oracle) {
            Map<Boolean, List<MetaDBColumn>> collect = columns.stream()
                    .collect(Collectors.partitioningBy(column ->
                            StringUtils.isBlank(column.getOriginalColumnName()) && mode == AlterMode.MODIFY));
            List<MetaDBColumn> notRenameColumns = collect.get(Boolean.TRUE);
            if (CollectionUtils.isNotEmpty(notRenameColumns)) {
                List<SQLStatement> stmts = getStmts(type, mode, columns);
                return SQLUtils.toSQLString(stmts, DbType.oracle);
            }
            List<SQLStatement> stmts = new ArrayList<>();
            List<MetaDBColumn> renameColumns = collect.get(Boolean.FALSE);
            if (CollectionUtils.isNotEmpty(renameColumns)) {
                for (MetaDBColumn renameColumn : renameColumns) {
                    List<SQLStatement> stmtList = getStmts(type, mode, Collections.singletonList(renameColumn));
                    if (stmtList == null || stmtList.size() == 0) {
                        continue;
                    }
                    stmts.addAll(stmtList);
                }
                return SQLUtils.toSQLString(stmts, DbType.oracle);
            }
        }
        List<SQLStatement> stmts = getStmts(type, mode, columns);
        return SQLUtils.toSQLString(stmts, type);
    }

    /**
     * 获取table的create语句
     *
     * @param connection
     * @param dbType
     * @param tableName
     * @return
     */
    public static String printDDL(Connection connection, String dbType, String tableName) {
        AlterCreator creator = create(DbType.of(dbType), AlterMode.CREATE, null);
        return creator.getCreateTableScript(connection, Arrays.asList(tableName));
    }

    /**
     * 获取table的create语句
     *
     * @param metaDBInfo 数据库连接信息
     * @param tableName
     * @return
     * @throws SQLException
     */
    public static String printDDL(MetaDBInfo metaDBInfo, String tableName) throws SQLException {
        ConnectionHolder holder = MetaDBHelper.getConnection(metaDBInfo);
        AlterCreator creator = create(DbType.of(metaDBInfo.getDbType()), AlterMode.CREATE, null);
        return creator.getCreateTableScript(holder.getConnection(), Arrays.asList(tableName));
    }

    /**
     * 获取alter语句(字段的修改、删除、添加)(表名的修改)
     *
     * @param dbType
     * @param mode
     * @param columns
     * @return
     */
    public static String printSql(String dbType, AlterMode mode, List<MetaDBColumn> columns) {
        DbType type = DbType.of(dbType);
        List<SQLStatement> stmts = getStmts(type, mode, columns);
        return SQLUtils.toSQLString(stmts, type);
    }

    public static String printOracleCommentSql(List<MetaDBColumn> columns) {
        AlterContext alterContext = new AlterContext(columns, DbType.oracle, getTableNameFromColumn(columns));
        OracleCommentCreator oracleCommentCreator = new OracleCommentCreator(alterContext);
        List<SQLStatement> stmts = oracleCommentCreator.createAlterStatement();
        return SQLUtils.toSQLString(stmts, DbType.oracle);
    }

    private static List<SQLStatement> getStmts(DbType dbType, AlterMode mode, List<MetaDBColumn> columns) {
        if (dbType == DbType.oracle) {
            AlterContext alterContext = new AlterContext(columns, DbType.oracle, getTableNameFromColumn(columns));
            Trigger trigger = null;
            if (mode == AlterMode.MODIFY) {
                trigger = new Trigger(AlterCreator.link(new OracleModifyAlterCreator(alterContext),
                        new OracleRenameAlterCreator(alterContext), new OracleCommentCreator(alterContext)))
                        .context(alterContext);
            } else if (mode == AlterMode.ADD) {
                trigger = new Trigger(AlterCreator.link(new OracleAddAlterCreator(alterContext),
                        new OracleCommentCreator(alterContext))).context(alterContext);
            }
            if (trigger != null) {
                return trigger.createAlterSQL();
            }
        }
        AlterCreator creator = create(dbType, mode, columns);
        return creator.createAlterStatement();
    }

    private static String getTableNameFromColumn(List<MetaDBColumn> columns) {
        List<String> tables = columns.stream().map(MetaDBColumn::getTableName).distinct().collect(Collectors.toList());
        return tables.get(0);
    }

    private static AlterCreator create(DbType dbType, AlterMode mode, List<MetaDBColumn> columns) {
        CreatorFactory provider = ProviderManager.getInstance().getFactoryProvider(dbType);
        AlterCreator creator = provider.create(mode, columns);
        if (creator == null) {
            throw new RuntimeException("creator is not exist");
        }
        return creator;
    }

}

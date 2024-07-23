package com.alibaba.druid.analysis.meta.alter.chain;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.analysis.meta.alter.AlterCreator;
import java.util.List;

/**
 * @author LENOVO
 * @date 2024/6/24 11:03
 */
public class Trigger {

    private final AlterCreator creator;
    private AlterContext context;

    public Trigger(AlterCreator creator, AlterContext context) {
        this.creator = creator;
        this.context = context;
    }

    public Trigger(AlterCreator creator) {
        this.creator = creator;
    }

    public Trigger context(AlterContext context) {
        this.context = context;
        return this;
    }

    public List<SQLStatement> createAlterSQL() {
        creator.createChain(context);
        return context.getStmts();
    }

//    public static void main(String[] args) {
//
//        List<MetaDBColumn> columns = new ArrayList<>();
//        String tableName = "USER";
//        MetaDBColumn column1 = new MetaDBColumn(tableName, "username", "VARCHAR", 200, null, true, "测试备注", "shit", false, "old_username");
//        MetaDBColumn column2 = new MetaDBColumn(tableName, "MONEY", "DECIMAL", 8, 0, true, "金额", "10000.00", false, "OLD_MONEY");
//        MetaDBColumn column3 = new MetaDBColumn(tableName, "create_time", "DATE", 0, null, false, "创建时间", "sysdate", false, null);
//        columns.add(column1);
//        columns.add(column2);
//        columns.add(column3);
//
//        String sql = AlterLoader.printSql("mysql", AlterMode.MODIFY, columns);
//        System.out.println(sql);
//    }

}

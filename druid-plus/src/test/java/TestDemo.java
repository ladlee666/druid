import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.dialect.oracle.visitor.MagicOracleSchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author LENOVO
 * @date 2024/7/25 15:28
 */
public class TestDemo {

    @Test
    public void test() {
        String sql = "select t.patient_name, t.phone, t.relationship, t.h_name as name, t.qrcode, r.relation_name,t.stat from (\n" +
                "                  select p.patient_name, p.phone, p.relationship, c.hospital_name as h_name, c.qrcode,\n" +
                "                         case c.status when 1 then '正常' when 2 then '删除' end as stat\n" +
                "                  from tb_patient_card c\n" +
                "                           left join tb_patient p on c.patient_id = p.id\n" +
                "                  where c.card_no = '00909'\n" +
                "                    and c.cert_no = '4012'\n" +
                "              ) t left join tb_relation r on t.relationship = r.id;";

        Map<String, Map<String, String>> columnMapping = new HashMap<>();
        Map<String, String> item1 = new HashMap<>();
        item1.put("PATIENT_ID", "ppp_id");
        item1.put("HOSPITAL_NAME", "hosss_name");
        columnMapping.put("TB_PATIENT_CARD", item1);

        Map<String, String> item2 = new HashMap<>();
        item2.put("RELATION_NAME", "giao_name");
        columnMapping.put("TB_RELATION", item2);

        SQLStatement stmt = SQLUtils.parseSingleStatement(sql, DbType.oracle);
        MagicOracleSchemaStatVisitor visitor = new MagicOracleSchemaStatVisitor();
        stmt.accept(visitor);
        Collection<TableStat.Column> columns = visitor.getColumns();
        for (TableStat.Column column : columns) {
            String tableName = column.getTable().toUpperCase();
            if (columnMapping.containsKey(tableName)) {
                Map<String, String> columnPair = columnMapping.get(tableName);
                String columnName = column.getName().toUpperCase();
                if (columnPair.containsKey(columnName)) {
                    String newName = columnPair.get(columnName);
                    SQLExpr columnExpr = column.getColumnExpr();
                    boolean update = false;
                    if (columnExpr instanceof SQLIdentifierExpr) {
                        SQLIdentifierExpr expr = (SQLIdentifierExpr) columnExpr;
                        expr.setName(newName);
                        update = true;
                    } else if (columnExpr instanceof SQLPropertyExpr) {
                        SQLPropertyExpr expr = (SQLPropertyExpr) columnExpr;
                        expr.setName(newName);
                        update = true;
                    } else {
                        System.out.println(columnExpr.getClass().getName());
                    }
                    SQLObject parent = columnExpr.getParent();
                    if (update && parent instanceof SQLSelectItem) {
                        SQLSelectItem selectItem = (SQLSelectItem) parent;
                        if (selectItem.getAlias() == null) {
                            selectItem.setAlias(columnName);
                        }
                    }
                }
            }
            System.out.println(column.getTable() + ":" + column.getName());
        }
        System.out.println(SQLUtils.toSQLString(stmt));
    }
}

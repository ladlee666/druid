package com.alibaba.druid.demo.sql;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import junit.framework.TestCase;

/**
 * @author LENOVO
 * @date 2024/7/17 10:56
 */
public class OracleCursorParamTest extends TestCase {

    public void test_cursor_parameters() {
        String sql = "DECLARE\n" +
                "CURSOR cur_param(name_str VARCHAR2) IS SELECT u.USERNAME, u.PASSWORD FROM MG_USER u WHERE ID = 1;\n" +
                "op_name VARCHAR2(100);\n" +
                "BEGIN\n" +
                "SELECT NVL(EMP_NAME, USERNAME) INTO op_name FROM MG_USER WHERE ID = 1;\n" +
                "END;";

        SQLStatement stmt = SQLUtils.parseSingleStatement(sql, DbType.oracle);

        System.out.println(stmt);
    }

    public void test_1() {
        String sql = "DECLARE\n" +
                "CURSOR c_datas IS SELECT t.* FROM JSON_TABLE('[{ \"applename\": \"redapple \", \"applecode\": \"111000\", \"price\": \"10\" }, { \"applename\": \"greenapple \", \"applecode\": \"111111\", \"price\": \"12\" }, { \"applename\": \"yellowapple \", \"applecode\": \"111222\", \"price\": \"8\" }]','$[*]' COLUMNS(applename VARCHAR2(20) PATH '$.applename',redapple VARCHAR2(20) PATH '$.redapple',applecode VARCHAR2(20) PATH '$.applecode')) t;\n" +
                "c_data c_datas%rowtype;\n" +
                "BEGIN\n" +
                "for c_data in c_datas loop\n" +
                "dbms_output.put_line('{\"num1\":' || TO_CHAR(c_data.applename) || ',\"num2\":' || TO_CHAR(c_data.redapple) || '\"}');\n" +
                "end loop;\n" +
                "-- dbms_output.put_line('{\"num1\":' || TO_CHAR(num1) || ',\"num2\":' || TO_CHAR(num2) || '\"}');\n" +
                "END;";

        SQLStatement stmt = SQLUtils.parseSingleStatement(sql, DbType.oracle);

        System.out.println(stmt);
    }
}

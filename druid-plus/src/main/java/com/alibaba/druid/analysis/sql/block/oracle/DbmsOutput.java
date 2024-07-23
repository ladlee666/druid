package com.alibaba.druid.analysis.sql.block.oracle;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DbmsOutput {

    private final CallableStatement enable_stmt;
    private final CallableStatement disable_stmt;
    private final CallableStatement show_stmt;

    public DbmsOutput(Connection conn) throws SQLException {
        enable_stmt = conn.prepareCall("begin dbms_output.enable(:1); end;");
        disable_stmt = conn.prepareCall("begin dbms_output.disable; end;");

        show_stmt = conn.prepareCall(
                "declare " +
                        " l_line varchar2(32767); " +
                        " l_done number; " +
                        " l_buffer CLOB; " +
                        "begin " +
                        " l_buffer := TO_CLOB('');" +
                        " loop " +
                        " exit when length(l_buffer)+255 > :maxbytes OR l_done = 1; " +
                        " dbms_output.get_line( l_line, l_done ); " +
                        " l_buffer := l_buffer || l_line || chr(10); " +
                        " end loop; " +
                        " :done := l_done; " +
                        " :buffer := l_buffer; " +
                        "end;");
    }

    public void enable(int size) throws SQLException {
        enable_stmt.setInt(1, size);
        enable_stmt.executeUpdate();
    }

    public void disable() throws SQLException {
        disable_stmt.executeUpdate();
    }

    public void show(List<Object> outputList) throws SQLException {
        int done = 0;

        show_stmt.registerOutParameter(2, java.sql.Types.INTEGER);
        show_stmt.registerOutParameter(3, java.sql.Types.VARCHAR);

        for (; ; ) {
            show_stmt.setInt(1, 32000);
            show_stmt.executeUpdate();
            String result = show_stmt.getString(3);
            List<String> lines = StrUtil.split(result, '\n');
            if (CollUtil.isNotEmpty(lines)) {
                for (String line : lines) {
                    if (StrUtil.isBlank(line)) {
                        continue;
                    }
//                    if (JSONUtil.isTypeJSON(line)) {
//                        JSONObject lineJson;
//                        try {
//                            lineJson = JSONUtil.parseObj(line);
//                            outputList.add(lineJson);
//                            continue;
//                        } catch (Exception ignored) {
//                        }
//                    }
                    outputList.add(line);
                }
            }
            if ((done = show_stmt.getInt(2)) == 1) break;
        }
    }

    public void close() throws SQLException {
        enable_stmt.close();
        disable_stmt.close();
        show_stmt.close();
    }
}

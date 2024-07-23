package com.alibaba.druid.analysis.sql.block.oracle;//package com.alibaba.druid.analysis.sql.block.oracle;
//
//import java.sql.*;
//
//public class JdbcExample {
//
//    public static void main(String args[])
//            throws SQLException {
//        DriverManager.registerDriver
//                (new oracle.jdbc.driver.OracleDriver());
//
//        Connection conn = DriverManager.getConnection
//                ("jdbc:oracle:thin:@116.211.118.67:40001:ORCL",
//                        "his_cloud", "123456");
//        conn.setAutoCommit(false);
//
//        Statement stmt = conn.createStatement();
//
//        DbmsOutput dbmsOutput = new DbmsOutput(conn);
//
//        dbmsOutput.enable(1000000);
//
//        stmt.execute
//                ("DECLARE\n" +
//                        "  project_info COM_BEDINFO%rowtype;\n" +
//                        "\tv_inpatient_no VARCHAR2(55);\n" +
//                        "BEGIN\n" +
//                        "\tSELECT * INTO project_info FROM COM_BEDINFO WHERE ROWNUM <= 1;\n" +
//                        "\tv_inpatient_no := project_info.ITEM_CODE || project_info.ROOM_NUM;\n" +
//                        "\tdbms_output.put_line(v_inpatient_no);\n" +
//                        "\tdbms_output.put_line('{\"ITEM_CODE\":' || '\"' || project_info.ITEM_CODE || '\"' || ',\"ROOM_NUM\":' || '\"' || project_info.ROOM_NUM || '\"' || '}');\n" +
//                        "END;");
//        stmt.close();
//
//        dbmsOutput.show();
//
//        dbmsOutput.close();
//        conn.close();
//    }
//
//    static class DbmsOutput {
//
//        private CallableStatement enable_stmt;
//        private CallableStatement disable_stmt;
//        private CallableStatement show_stmt;
//
//        public DbmsOutput(Connection conn) throws SQLException {
//            enable_stmt = conn.prepareCall("begin dbms_output.enable(:1); end;");
//            disable_stmt = conn.prepareCall("begin dbms_output.disable; end;");
//
//            show_stmt = conn.prepareCall(
//                    "declare " +
//                            " l_line varchar2(255); " +
//                            " l_done number; " +
//                            " l_buffer long; " +
//                            "begin " +
//                            " loop " +
//                            " exit when length(l_buffer)+255 > :maxbytes OR l_done = 1; " +
//                            " dbms_output.get_line( l_line, l_done ); " +
//                            " l_buffer := l_buffer || l_line || chr(10); " +
//                            " end loop; " +
//                            " :done := l_done; " +
//                            " :buffer := l_buffer; " +
//                            "end;");
//        }
//
//        public void enable(int size) throws SQLException {
//            enable_stmt.setInt(1, size);
//            enable_stmt.executeUpdate();
//        }
//
//        public void disable() throws SQLException {
//            disable_stmt.executeUpdate();
//        }
//
//        public void show() throws SQLException {
//            int done = 0;
//
//            show_stmt.registerOutParameter(2, Types.INTEGER);
//            show_stmt.registerOutParameter(3, Types.VARCHAR);
//
//            for (; ; ) {
//                show_stmt.setInt(1, 32000);
//                show_stmt.executeUpdate();
//                System.out.print(show_stmt.getString(3));
//                if ((done = show_stmt.getInt(2)) == 1) break;
//            }
//        }
//
//        public void close() throws SQLException {
//            enable_stmt.close();
//            disable_stmt.close();
//            show_stmt.close();
//        }
//    }
//
//    public static Connection getConnection() throws SQLException, ClassNotFoundException {
//        // Oracle数据库的JDBC URL格式：jdbc:oracle:thin:@<hostname>:<port>:<dbname>
//        String jdbcUrl = "jdbc:oracle:thin:@116.211.118.67:40001:ORCL";
//        String username = "his_cloud";
//        String password = "123456";
//        // 加载Oracle JDBC驱动
//        Class.forName("oracle.jdbc.driver.OracleDriver");
//        // 建立连接
//        return DriverManager.getConnection(jdbcUrl, username, password);
//    }
//}
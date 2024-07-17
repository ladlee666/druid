package com.alibaba.druid.analysis.seq;//package com.alibaba.druid.analysis.seq;
//
//import com.alibaba.druid.analysis.seq.builder.DbSeqBuilder;
//import com.alibaba.druid.analysis.seq.sequence.Sequence;
//import com.zaxxer.hikari.HikariConfig;
//import com.zaxxer.hikari.HikariDataSource;
//
//import javax.sql.DataSource;
//
//
//public class Test {
//
//    public static void main(String[] args) {
//        DataSource datasource = getDatasource();
//        Sequence sequence = DbSeqBuilder
//                .create()
//                .dataSource(datasource)
//                .dbType("oracle")
//                .stepStart(0)
//                .bizName(() -> "ladlee")
//                .build();
//        for (int i = 0; i < 50; i++) {
//            String s = sequence.nextNo();
//            System.out.println(s);
//        }
//        String value = sequence.nextNo();
//        System.out.println(value);
//    }
//
//    public static DataSource getDatasource() {
//        HikariConfig config = new HikariConfig();
//        config.setDriverClassName("oracle.jdbc.driver.OracleDriver");
//        config.setJdbcUrl("jdbc:oracle:thin:@116.211.118.66:40001:ORCL");
//        config.setUsername("lowcode_tj_test");
//        config.setPassword("123456");
//        return new HikariDataSource(config);
//    }
//}

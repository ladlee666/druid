package com.alibaba.druid.analysis.seq.range.impl.db;

public class DbConstant {

    public static final String SQL_ORACLE_CREATE_TABLE =
            "DECLARE i INTEGER; BEGIN SELECT COUNT(*) INTO i FROM USER_TABLES WHERE TABLE_NAME = '#tableName'; IF i <= 0 THEN EXECUTE IMMEDIATE 'CREATE TABLE #tableName( ID VARCHAR2 ( 32 ) NOT NULL, VALUE NUMBER(20) NOT NULL, NAME VARCHAR2 ( 20 ) NOT NULL UNIQUE, STEP NUMBER(9) DEFAULT 1, LOOP NUMBER(1), max NUMBER(11), range NUMBER(4) default 10, GMT_CREATE DATE DEFAULT SYSDATE NOT NULL, GMT_MODIFIED DATE, PRIMARY KEY ( ID ) )'; END IF; END;";

    public static final String SQL_MYSQL_CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS mg_config( `id` BIGINT( 20) NOT NULL AUTO_INCREMENT, `value` BIGINT( 20 ) NOT NULL, `name` VARCHAR( 32 ) NOT NULL, step INT( 9 ) DEFAULT 1, loop tinyint(1), max INT(11), range INT(4) default 10, gmt_create DATETIME NOT NULL, gmt_modified DATETIME, PRIMARY KEY ( `id` ), UNIQUE uk_name ( `name` ) )";

    public final static String SQL_SELECT_RANGE = "SELECT value, step, loop, max, range FROM #tableName WHERE name = ?";

    public final static String SQL_INSERT_RANGE = "INSERT INTO #tableName(ID, NAME, VALUE, STEP) VALUES(?, ?, ?, ?)";

    public final static String SQL_UPDATE_RANGE = "UPDATE #tableName SET value=?,gmt_modified=? WHERE name=? AND value=?";

    public final static Long DELTA = 100000000L;

    public final static int OVER_SINGLE_RANGE = -1;

    public final static int OVER_LOOP_MAX = -2;

}

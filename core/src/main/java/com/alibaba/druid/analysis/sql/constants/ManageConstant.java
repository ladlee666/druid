package com.alibaba.druid.analysis.sql.constants;

import java.util.Arrays;
import java.util.List;

/**
 * @author godfunc
 */
public interface ManageConstant {
    String TOKEN_HEADER = "access_token";
    String LOGIN_PATH = "/login";

    String MAIN_PATH = "/page";

    Integer RM_TAG_NOT = 0;

    /**
     * token 有效时间 秒
     */
    long TOKEN_EXPIRED = 60 * 60 * 24 * 90;

    /**
     * 快过期时间（毫秒）
     */
    long EXPIRED_PLUS = 2 * 60 * 60 * 1000;


    String JDBC_BEAN_PREFIX = "jdbc_";

    /**
     * 职工过滤字段
     */
    String USER_ID = "USER_ID";

    /**
     * 科室过滤字段
     */
    String DEPT_FIELD = "DEPT_ID";

    String DEPT_HOSPITAL_CODE_FIELD = "HOS_CODE";

    /**
     * 默认的科室ID
     */
    String DEFAULT_DEPT_ID = "0";

    String DEFAULT_HOSPITAL_CODE = "0";

    /**
     * 主数据源ID
     */
    String MASTER_DATASOURCE = "master";

    /**
     * 数据源配置页面url前缀
     */
    String DATASOURCE_CONFIG_PATH = "/datasources";

    /**
     * 三方系统通过免密登录的url
     */
    String UN_LOGIN_PATH = "/application/ddLogin";
    /**
     * 三方系统通过免密登录的参数，值由AES加密传输
     */
    String UN_LOGIN_PARAMETER = "UUID";
    /**
     * 三方系统通过免密登录的参数，机构编码，支持跨机构
     */
    String UN_LOGIN_PARAMETER_HOSPITAL = "HOSPITAL_CODE";
    /**
     * 拼接字符
     */
    String CHAR_JOIN = "_";

    /**
     * 安全相关修改：登录后不允许再访问的数据源配置url
     */
    String AUTH_DATASOURCE_PATH_1 = "/v1/datasources/getAllDatasource";
    String AUTH_DATASOURCE_PATH_2 = "/v1/datasources/updateDatasource";
    String AUTH_DATASOURCE_PATH_3 = "/v1/datasources/addDatasourceMaster";
    List<String> AUTH_DATASOURCE_PATHS = Arrays.asList(AUTH_DATASOURCE_PATH_1, AUTH_DATASOURCE_PATH_2, AUTH_DATASOURCE_PATH_3);

    String USER_DETAIL_KEY = "app:user:detail";
    String MENU_DETAIL_KEY = "app:menu:detail";

    String DEFAULT_JASYPT_PASSWORD = "P@ssw0rd";
    String DEFAULT_JASYPT_PASSWORD_KEY = "jasypt.encryptor.password";
    String DEFAULT_JASYPT_PASSWORD_ENV_KEY = "jasypt_custom";

    String DEFAULT_MANAGER_USER_ID = "1";
}

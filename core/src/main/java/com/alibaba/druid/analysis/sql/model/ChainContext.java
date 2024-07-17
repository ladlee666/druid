package com.alibaba.druid.analysis.sql.model;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.analysis.sql.visitor.CustomVisitor;
import lombok.Builder;
import lombok.Data;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;

@Data
@Builder
public class ChainContext {

    private SQLStatement sqlStatement;

    private SQLStatement countStatement;

    private CustomVisitor visitor;

    private ExecuteContext context;

    private DbType dbType;

    private boolean authWrap;

    private List<String> paramKeys;

    private JdbcTemplate jdbcTemplate;

    private String userId;

    private String deptId;

    private Integer roleRange;

    private List<String> childDeptIds;

    private DataSource dataSource;

    private List<Object> autoIds;

    /**
     * oracle PL/SQL 输出
     */
    private boolean dbmsOutput;

}

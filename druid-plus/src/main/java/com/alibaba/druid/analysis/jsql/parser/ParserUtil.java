package com.alibaba.druid.analysis.jsql.parser;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

import com.alibaba.druid.analysis.jsql.KeyWord;
import com.alibaba.druid.analysis.jsql.StatementSchemaVisitor;
import com.alibaba.druid.analysis.jsql.StmtModifyVisitor;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author LENOVO
 * @date 2024/6/5 11:13
 */
public class ParserUtil {

    /**
     * @param body           被修改对象
     * @param incrPrefix     前缀(prefix + incrNum)
     * @param prefixMappings 前缀映射关系
     * @return
     */
    public static String parser(String body, String incrPrefix, Map<String, Set<String>> prefixMappings, KeyWord... keyWords) {
        if (StrUtil.isBlank(body) || CollUtil.isEmpty(prefixMappings)) {
            return body;
        }
        try {
            List<String> placeholders = new LinkedList<>();
            // update {{...}} -> ST0_EN1
            String sql = new TokenParser("{{", "}}", str -> {
                placeholders.add(str);
                return "ST0_EN1";
            }).parse(body);

            String transform = transform(sql, incrPrefix, prefixMappings, keyWords);

            AtomicInteger ai = new AtomicInteger(0);
            // 还原 ST0...EN1 -> {{...}}
            String body2 = new TokenParser("ST0", "EN1", str -> {
                int index = ai.getAndIncrement();
                return "{{" + placeholders.get(index) + "}}";
            }).parse(transform);

            return body2;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return body;
    }

    public static Pair<String, Boolean> replaceTable(String body, String openToken, String closeToken, Map<String, String> tableMappings) {
        if (StrUtil.isBlank(body) || CollUtil.isEmpty(tableMappings)) {
            return Pair.of(body, false);
        }
        try {
            List<String> placeholders = new LinkedList<>();
            String sql = new TokenParser(openToken, closeToken, str -> {
                placeholders.add(str);
                return "ST0_EN1";
            }).parse(body);
            StmtModifyVisitor visitor = new StmtModifyVisitor(tableMappings);
            String transform = transform(sql, visitor);
            AtomicInteger ai = new AtomicInteger(0);
            String finalSql = new TokenParser("ST0", "EN1", str -> {
                int index = ai.getAndIncrement();
                return openToken + placeholders.get(index) + closeToken;
            }).parse(transform);
            return Pair.of(finalSql, visitor.isTableUpdate());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Pair.of(body, false);
    }

    public static String transform(String sql, String prefix, Map<String, Set<String>> prefixMappings, KeyWord... keyWords) throws JSQLParserException {
        if (StrUtil.isBlank(sql)) {
            return sql;
        }
        StringBuilder sb = new StringBuilder();
        Statements stmts = CCJSqlParserUtil.parseStatements(sql);
        StmtModifyVisitor visitor = new StmtModifyVisitor(prefixMappings, prefix, keyWords);
        for (Statement stmt : stmts) {
            stmt.accept(visitor);
            sb.append(stmt);
        }
        return sb.toString();
    }

    public static String transform(String sql, Map<String, String> tableMappings) throws JSQLParserException {
        if (StrUtil.isBlank(sql)) {
            return sql;
        }
        return transform(sql, new StmtModifyVisitor(tableMappings));
    }

    private static String transform(String sql, StmtModifyVisitor visitor) throws JSQLParserException {
        if (visitor == null) {
            return sql;
        }
        StringBuilder sb = new StringBuilder();
        Statements stmts = CCJSqlParserUtil.parseStatements(sql);
        for (Statement stmt : stmts) {
            stmt.accept(visitor);
            sb.append(stmt).append(";\n");
        }
        return sb.toString();
    }

    public static Set<String> getSelectTables(Statement statement) {
        StatementSchemaVisitor visitor = new StatementSchemaVisitor();
        statement.accept(visitor);
        return visitor.getTableNames();
    }

    public static Set<String> getSelectTables(String sql) throws JSQLParserException {
        return getSelectTables(getStatement(sql));
    }

    public static Statement getStatement(String sql) throws JSQLParserException {
        return CCJSqlParserUtil.parse(sql);
    }

}

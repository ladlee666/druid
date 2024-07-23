package com.alibaba.druid.analysis.sql.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.core.convert.ConversionService;

import java.sql.*;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
public class PreStatementSetParamUtils {

    public static void setParams(PreparedStatement ps, ConversionService conversionService, Map<String, Object> paramsSource, List<String> mustacheKeysInOrder) throws SQLException {
        if (MapUtils.isNotEmpty(paramsSource) && CollUtil.isNotEmpty(mustacheKeysInOrder)) {
            int index = 0;
            for (int i = 0; i < mustacheKeysInOrder.size(); i++) {
                Object value = paramsSource.get(mustacheKeysInOrder.get(i));
                DataType dataType = TypeResolverUtils.resolverValueType(value);
                switch (dataType) {
                    case STRING:
                        ps.setString(index + 1, conversionService.convert(value, String.class));
                        break;
                    case INTEGER:
//                        ps.setInt(index + 1, conversionService.convert(value, Integer.class));
//                        break;
                    case LONG:
//                        ps.setLong(index + 1, conversionService.convert(value, Long.class));
//                        break;
                    case FLOAT:
//                        ps.setFloat(index + 1, conversionService.convert(value, Float.class));
//                        break;
                    case DOUBLE:
//                        ps.setDouble(index + 1, conversionService.convert(value, Double.class));
                        ps.setObject(index + 1, value);
                        break;
                    case BOOLEAN:
                        ps.setBoolean(index + 1, conversionService.convert(value, Boolean.class));
                        break;
                    case ARRAY:
                        index--;

                        break;
                    case DATE:
//                        try {
//                            ps.setDate(index + 1, conversionService.convert(value, Date.class));
//                        } catch (Exception throwables) {
//                            DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
//                                    .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
//                                    .toFormatter();
//                            try {
//                                LocalDate parse = LocalDate.parse(StrUtil.toString(value), dateFormatter);
//                                ps.setDate(index + 1, Date.valueOf(parse));
//                            } catch (Exception e) {
//                                dateFormatter = new DateTimeFormatterBuilder()
//                                        .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
//                                        .toFormatter();
//                                try {
//                                    LocalDate parse = LocalDate.parse(StrUtil.toString(value), dateFormatter);
//                                    ps.setDate(index + 1, Date.valueOf(parse));
//                                } catch (Exception e2) {
//                                    dateFormatter = new DateTimeFormatterBuilder()
//                                            .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
//                                            .toFormatter();
//                                    LocalDate parse = LocalDate.parse(StrUtil.toString(value), dateFormatter);
//                                    ps.setDate(index + 1, Date.valueOf(parse));
//
//                                }
//                            }
//                        }
                        String stringValue = conversionService.convert(value, String.class);
                        String format = TimeRegex.match(stringValue);
                        log.info("oraclePlugin.substituteValueInInput.DataType.DATE.match,value:{}, format:{}", value, format);
                        if (format != null) {
                            try {
                                java.util.Date utilDate = DateUtils.parseDate(stringValue, format);
                                Timestamp timestamp = new Timestamp(utilDate.getTime());
                                ps.setTimestamp(index + 1, timestamp);
                            } catch (ParseException e) {
                                log.error("{}解析{}时间错误:{}", value, format, e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            ps.setObject(index + 1, value);
                        }
                        break;
                    case NULL:
                        // ps.setNull(i + 1, Types.OTHER);
                        ps.setNull(index + 1, Types.NULL);
                        break;
                    case TIMESTAMP:
                        ps.setTimestamp(index + 1, conversionService.convert(value, Timestamp.class));
                        break;
                    default:
                        ps.setString(index + 1, conversionService.convert(value, String.class));
                        break;
                }
                index++;
            }
        }
    }

    private static Integer[] getArrayByString(String value) {
        value = value.replace("(", "[").replace(")", "]");
        JSONArray objects = JSONUtil.parseArray(value);
        Integer[] strings = new Integer[objects.size()];
        for (int i = 0; i < objects.size(); i++) {
            Integer valueString = objects.getInt(i);
            strings[i] = valueString;

        }
        return strings;

    }

    public static String setArrayParams(String sql, Map<String, Object> paramsSource, List<String> mustacheKeysInOrder) {
        if (MapUtils.isNotEmpty(paramsSource)) {
            for (int i = 0; i < mustacheKeysInOrder.size(); i++) {
                Object valueObject = paramsSource.get(mustacheKeysInOrder.get(i));
                DataType dataType = TypeResolverUtils.resolverValueType(valueObject);
                if (DataType.ARRAY.equals(dataType)) {

                    sql = sql.toUpperCase().replaceFirst("IN \\?", "IN " + valueObject);
                }
            }
        }
        return sql;
    }

    public static void setParams(CallableStatement ps, ConversionService conversionService, Map<String, Object> paramsSource, List<String> mustacheKeysInOrder, List<Integer> inIndexList, Map<Integer, Integer> mapSort) throws SQLException {
        if (MapUtils.isNotEmpty(paramsSource)) {
            for (int i = 0; i < mustacheKeysInOrder.size(); i++) {
                Object value = paramsSource.get(mustacheKeysInOrder.get(i));
                Integer index = mapSort.get(inIndexList.get(i));
                DataType dataType = TypeResolverUtils.resolverValueType(value);
                switch (dataType) {
                    case STRING:
                        ps.setString(index, conversionService.convert(value, String.class));
                        break;
                    case INTEGER:
                        ps.setInt(index, conversionService.convert(value, Integer.class));
                        break;
                    case LONG:
                        ps.setLong(index, conversionService.convert(value, Long.class));
                        break;
                    case FLOAT:
                        ps.setFloat(index, conversionService.convert(value, Float.class));
                        break;
                    case DOUBLE:
                        ps.setDouble(index, conversionService.convert(value, Double.class));
                        break;
                    case BOOLEAN:
                        ps.setBoolean(index, conversionService.convert(value, Boolean.class));
                        break;
                    case DATE:
                        try {
                            ps.setDate(index, conversionService.convert(value, Date.class));
                        } catch (Exception throwables) {
                            DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
                                    .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                                    .toFormatter();
                            try {
                                LocalDate parse = LocalDate.parse(StrUtil.toString(value), dateFormatter);
                                ps.setDate(index, Date.valueOf(parse));
                            } catch (Exception e) {
                                dateFormatter = new DateTimeFormatterBuilder()
                                        .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                                        .toFormatter();
                                try {
                                    LocalDate parse = LocalDate.parse(StrUtil.toString(value), dateFormatter);
                                    ps.setDate(index, Date.valueOf(parse));
                                } catch (Exception e2) {
                                    dateFormatter = new DateTimeFormatterBuilder()
                                            .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                            .toFormatter();
                                    LocalDate parse = LocalDate.parse(StrUtil.toString(value), dateFormatter);
                                    ps.setDate(index, Date.valueOf(parse));

                                }
                            }
                        }
                        break;
                    case NULL:
                        ps.setNull(index, Types.OTHER);
                        break;
                    case TIMESTAMP:
                        ps.setTimestamp(index, conversionService.convert(value, Timestamp.class));
                        break;
                    default:
                        ps.setString(index, conversionService.convert(value, String.class));
                        break;
                }
            }
        }
    }

    public static void setOutParams(CallableStatement ps, Map<String, Integer> outIndexMap, Map<String, String> finalOutMap, Map<Integer, Integer> mapSort) {
        finalOutMap.forEach((key, value) -> {
            //取出参的下标
            Integer index = mapSort.get(outIndexMap.get(key));
            try {
                ps.registerOutParameter(index, TypeResolverUtils.toValueType(value));
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
    }


    @AllArgsConstructor
    public enum TimeRegex {

        FORMAT1("yyyy-MM-dd HH:mm:ss", "^((([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})-(((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)-(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|[1][0-9]|2[0-8]))))|((([0-9]{2})(0[48]|[2468][048]|[13579][26])|((0[48]|[2468][048]|[3579][26])00))-02-29))\\s+([0-1]?[0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])$"),
        FORMAT2("yyyy-MM-dd", "(([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})-(((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)-(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|[1][0-9]|2[0-8]))))|((([0-9]{2})(0[48]|[2468][048]|[13579][26])|((0[48]|[2468][048]|[3579][26])00))-02-29)$"),
        FORMAT3("yyyy/MM/dd", "(([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})\\/(((0[13578]|1[02])\\/(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)\\/(0[1-9]|[12][0-9]|30))|(02\\/(0[1-9]|[1][0-9]|2[0-8]))))|((([0-9]{2})(0[48]|[2468][048]|[13579][26])|((0[48]|[2468][048]|[3579][26])00))\\/02\\/29)$"),
        FORMAT4("yyyy/MM/dd HH:mm:ss", "((([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})\\/(((0[13578]|1[02])\\/(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)\\/(0[1-9]|[12][0-9]|30))|(02\\/(0[1-9]|[1][0-9]|2[0-8]))))|((([0-9]{2})(0[48]|[2468][048]|[13579][26])|((0[48]|[2468][048]|[3579][26])00))\\/02\\/29))\\s([0-1][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])$"),
        FORMAT5("yyyyMMdd", "(([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})(((0[13578]|1[02])(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)(0[1-9]|[12][0-9]|30))|(02(0[1-9]|[1][0-9]|2[0-8]))))|((([0-9]{2})(0[48]|[2468][048]|[13579][26])|((0[48]|[2468][048]|[3579][26])00))0229)$"),
        FORMAT6("yyyyMMddHHmmss", "((([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})(((0[13578]|1[02])(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)(0[1-9]|[12][0-9]|30))|(02(0[1-9]|[1][0-9]|2[0-8]))))|((([0-9]{2})(0[48]|[2468][048]|[13579][26])|((0[48]|[2468][048]|[3579][26])00))0229))([0-1][0-9]|2[0-3])([0-5][0-9])([0-5][0-9])$"),
        FORMAT7("yyyyMMddHHmmssSSS", "((([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})(((0[13578]|1[02])(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)(0[1-9]|[12][0-9]|30))|(02(0[1-9]|[1][0-9]|2[0-8]))))|((([0-9]{2})(0[48]|[2468][048]|[13579][26])|((0[48]|[2468][048]|[3579][26])00))0229))([0-1][0-9]|2[0-3])([0-5][0-9])([0-5][0-9])([0-9]{3})$"),
        FORMAT8("yyyyMMdd HH:mm:ss", "((([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})(((0[13578]|1[02])(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)(0[1-9]|[12][0-9]|30))|(02(0[1-9]|[1][0-9]|2[0-8]))))|((([0-9]{2})(0[48]|[2468][048]|[13579][26])|((0[48]|[2468][048]|[3579][26])00))0229))\\s([0-1][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])$"),
        FORMAT9("yyyy-MM-dd HH:mm", "^((([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})-(((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)-(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|[1][0-9]|2[0-8]))))|((([0-9]{2})(0[48]|[2468][048]|[13579][26])|((0[48]|[2468][048]|[3579][26])00))-02-29))\\s+([0-1]?[0-9]|2[0-3]):([0-5][0-9])$"),
        FORMAT10("yyyy-MM-dd'T'HH:mm:ss'Z'", "^([0-9]{4})-([0-9]{2})-([0-9]{2})T([0-9]{2}):([0-9]{2}):([0-9]{2})Z$"),
        ;

        @Getter
        private String format;

        @Getter
        private String regex;

        public static String match(String dateInput) {
            String format = null;
            for (TimeRegex value : values()) {
                if (Pattern.matches(value.getRegex(), dateInput)) {
                    format = value.getFormat();
                }
            }
            return format;
        }
    }

}

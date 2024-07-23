package com.alibaba.druid.analysis.sql.utils;

import cn.hutool.core.util.StrUtil;

import java.sql.JDBCType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;

public class TypeResolverUtils {

    public static DataType resolverValueType(String input) {
//        String strNumericValue = input.trim().replaceAll(",", "");
        String strNumericValue = input;
        if (input.startsWith("[") && input.endsWith("]") || input.startsWith("(") && input.endsWith(")")) {
            String betweenBraces = input.substring(1, input.length() - 1);
            String trimmedInputBetweenBraces = betweenBraces.trim();
            // In case of no values in the array, set this as null. Otherwise plugins like postgres and ms-sql
            // would break while creating a SQL array.
            if (trimmedInputBetweenBraces.isEmpty()) {
                return DataType.NULL;
            }
            return DataType.ARRAY;
        }
        try {
            Integer.parseInt(strNumericValue);
            return DataType.INTEGER;
        } catch (NumberFormatException e) {
            // Not an integer
        }
        try {
            Long.parseLong(strNumericValue);
            return DataType.LONG;
        } catch (NumberFormatException e1) {
            // Not long
        }
        try {
            Float.parseFloat(strNumericValue);
            return DataType.FLOAT;
        } catch (NumberFormatException e2) {
            // Not float
        }
        try {
            Double.parseDouble(strNumericValue);
            return DataType.DOUBLE;
        } catch (NumberFormatException e3) {
            // Not double
        }
        // Creating a copy of the input in lower case form to do simple string equality to check for boolean/null types.
        String copyInput = input.toLowerCase().trim();
        if (copyInput.equals("true") || copyInput.equals("false")) {
            return DataType.BOOLEAN;
        }
        if (copyInput.equals("null")) {
            return DataType.NULL;
        }
        try {
            final DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder()
                    .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
//                    .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    .toFormatter();
            LocalDateTime.parse(input, dateTimeFormatter);
            return DataType.TIMESTAMP;
        } catch (DateTimeParseException ex) {
            // Not timestamp
        }
        try {
            final DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
                    .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    .toFormatter();
            LocalDate.parse(input, dateFormatter);
            return DataType.DATE;
        } catch (Exception ex) {
            // Not date
        }
        try {
            final DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
                    .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    .toFormatter();
            LocalDate.parse(input, dateFormatter);
            return DataType.DATE;
        } catch (Exception ex) {
            // Not date
        }
        try {
            final DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
                    .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                    .toFormatter();
            LocalDate.parse(input, dateFormatter);
            return DataType.DATE;
        } catch (Exception ex) {
            // Not date
        }
        try {
            final DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
                    .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE)
                    .toFormatter();
            LocalDate.parse(input, dateFormatter);
            return DataType.DATE;
        } catch (Exception ex) {
            // Not date
        }
        try {
            final DateTimeFormatter timeFormatter = new DateTimeFormatterBuilder()
                    .appendOptional(DateTimeFormatter.ISO_LOCAL_TIME)
                    .toFormatter();
            LocalTime.parse(input, timeFormatter);
            return DataType.TIME;
        } catch (DateTimeParseException ex) {
            // Not time
        }

   /*     try (JsonReader reader = new JsonReader(new StringReader(input))) {
            strictGsonObjectAdapter.read(reader);
            reader.hasNext(); // throws on multiple top level values
            return DataType.JSON_OBJECT;
        } catch (IOException | JsonSyntaxException e) {
            // Not a strict JSON object
        }*/

        /*try {
            Document.parse(input);
            return DataType.BSON;
        } catch (JsonParseException | BsonInvalidOperationException e) {
            // Not BSON
        }*/
        // default return type if none of the above matches.
        return DataType.STRING;
    }

    /**
     * 根据输入的字符串，检测他的数据类型，（自欺欺人的一种数据类型解析方式，从官方copy过来的）
     *
     * @return
     */
    public static DataType resolverValueType(Object value) {
        if (value == null) {
            return DataType.NULL;
        }
        if (value instanceof String) {
            String input = StrUtil.toString(value);
            return resolverValueType(input);
        }
        return DataType.STRING;
    }

    public static JDBCType toValueType(String value) {
        if (StrUtil.isBlank(value)) {
            return JDBCType.NULL;
        }
        for (JDBCType jdbcType : JDBCType.values()) {
            if (jdbcType.getName().equals(value)) {
                return jdbcType;
            }
        }
        return null;
    }
}

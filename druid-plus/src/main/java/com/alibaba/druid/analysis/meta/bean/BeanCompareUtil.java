package com.alibaba.druid.analysis.meta.bean;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author LENOVO
 * @date 2024/7/2 10:48
 */
public class BeanCompareUtil {

    public static List<BeanCompareResult> compare(Object oldBean, Object newBean)
            throws InvocationTargetException, IllegalAccessException,
            IntrospectionException, NullPointerException, InstantiationException, NoSuchMethodException {
        if (oldBean == null || newBean == null) {
            throw new IllegalArgumentException("the argument must be not null");
        }
        if (oldBean.getClass() != newBean.getClass()) {
            return Collections.emptyList();
        }
        List<BeanCompareResult> list = new ArrayList<>();
        Class<?> aClass = oldBean.getClass();
        while (aClass != null) {
            Field[] fieldList = aClass.getDeclaredFields();
            for (Field field : fieldList) {
                if (field.isAnnotationPresent(FieldComparison.class)) {
                    BeanCompareResult comResult = new BeanCompareResult();
                    String propertyName = field.getName();
                    if (propertyName.startsWith("is")) {
                        // 去掉is前缀
                        propertyName = propertyName.substring(2);
                    }
                    PropertyDescriptor pd = new PropertyDescriptor(propertyName, aClass);
                    String name = pd.getName();
                    comResult.setCode(name);
                    Method getMethod = pd.getReadMethod();
                    Object o1 = getMethod.invoke(oldBean);
                    Object o2 = getMethod.invoke(newBean);
                    if (o2 == null) {
                        continue;
                    }
                    String first = null == o1 ? "" : String.valueOf(o1).trim();
                    String second = String.valueOf(o2).trim();
                    if (field.isAnnotationPresent(FieldComparison.class)) {
                        FieldComparison fieldComparison = field.getAnnotation(FieldComparison.class);
                        String value = fieldComparison.codeName();
                        Class<? extends FieldMapping> mapping = fieldComparison.mapping();
                        Method method = mapping.getMethod("mapping", Map.class);
                        Object invoke = method.invoke(mapping.newInstance(), new HashMap<>());
                        if (invoke != null) {
                            Map<String, String> map = (Map<String, String>) invoke;
                            String s = map.get(first);
                            String s2 = map.get(second);
                            comResult.setOldValue(s);
                            comResult.setNewValue(s2);
                        } else {
                            comResult.setOldValue(first);
                            comResult.setNewValue(second);
                        }
                        comResult.setName(value);
                    }
                    if (!first.equals(second)) {
                        list.add(comResult);
                    }
                }
            }
            aClass = aClass.getSuperclass();
        }
        return list;
    }

//    public static void main(String[] args) throws
//            IntrospectionException, InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
//        Student s1 = new Student("jack", 18, true);
//        Student s2 = new Student("jay", 18, false);
//        List<BeanCompareResult> results = compare(s1, s2);
//        for (BeanCompareResult result : results) {
//            System.out.println(result.getName());
//            System.out.println(result.getCode());
//            System.out.println(result.getOldValue());
//            System.out.println(result.getNewValue());
//            System.out.println("------------------------------------");
//        }
//    }
//
//    public static class Student {
//
//        @FieldComparison(codeName = "名称")
//        private String name;
//        @FieldComparison(codeName = "年纪")
//        private Integer age;
//        @FieldComparison(codeName = "主键")
//        private boolean isPk;
//
//        public Student() {
//        }
//
//        public Student(String name, Integer age) {
//            this.name = name;
//            this.age = age;
//        }
//
//        public Student(String name, Integer age, boolean isPk) {
//            this.name = name;
//            this.age = age;
//            this.isPk = isPk;
//        }
//
//        public String getName() {
//            return name;
//        }
//
//        public void setName(String name) {
//            this.name = name;
//        }
//
//        public Integer getAge() {
//            return age;
//        }
//
//        public void setAge(Integer age) {
//            this.age = age;
//        }
//
//        public boolean isPk() {
//            return isPk;
//        }
//
//        public void setPk(boolean pk) {
//            isPk = pk;
//        }
//    }
}

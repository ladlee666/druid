package com.alibaba.druid.analysis.seq;

import com.alibaba.druid.analysis.seq.sequence.Sequence;

import javax.sql.DataSource;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SeqContext {

    private final static Map<DataSource, Map<String, Sequence>> SEQUENCE_MAP = new ConcurrentHashMap<>();

    public static void setSequence(DataSource dataSource, String name, Sequence sequence) {
        Map<String, Sequence> innerMap = SEQUENCE_MAP.get(dataSource);
        if (innerMap == null) {
            innerMap = new ConcurrentHashMap<>();
        }
        innerMap.put(name, sequence);
        SEQUENCE_MAP.put(dataSource, innerMap);
    }

    public static Sequence getSequence(DataSource dataSource, String name) {
        Map<String, Sequence> innerMap = SEQUENCE_MAP.get(dataSource);
        if (innerMap == null) {
            return null;
        }
        return innerMap.get(name);
    }

    public static void clear(DataSource dataSource) {
        if (dataSource == null) {
            return;
        }
        Map<String, Sequence> innerMap = SEQUENCE_MAP.get(dataSource);
        if (innerMap != null) {
            Set<String> keySet = new HashSet<>(innerMap.keySet());
            for (String key : keySet) {
                innerMap.remove(key);
            }
        }
        SEQUENCE_MAP.remove(dataSource);
    }
}

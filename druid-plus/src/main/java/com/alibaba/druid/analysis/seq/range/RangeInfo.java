package com.alibaba.druid.analysis.seq.range;

import lombok.Data;

@Data
public class RangeInfo {

    private String id;

    /**
     * 当前值
     */
    private Long value;

    /**
     * 增长步长
     */
    private Integer step;

    /**
     * 范围
     */
    private Boolean loop;

    /**
     * 最大值
     */
    private Long max;

    /**
     * 单次取值范围
     */
    private Integer range;

    private String name;

    public RangeInfo() {
    }

    public RangeInfo(Long value, Integer step, Boolean loop, Long max, Integer range) {
        this.value = value;
        this.step = step;
        this.loop = loop;
        this.max = max;
        this.range = range;
    }

    public RangeInfo(String name, Long value, Integer step, Boolean loop, Long max, Integer range) {
        this.value = value;
        this.step = step;
        this.loop = loop;
        this.max = max;
        this.range = range;
        this.name = name;
    }
}

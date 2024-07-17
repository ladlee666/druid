package com.alibaba.druid.analysis.seq.range;

import lombok.Data;

import java.util.concurrent.atomic.AtomicLong;

import static com.alibaba.druid.analysis.seq.range.impl.db.DbConstant.OVER_LOOP_MAX;
import static com.alibaba.druid.analysis.seq.range.impl.db.DbConstant.OVER_SINGLE_RANGE;

@Data
public class SeqRange {

    /**
     * 初始化的值
     */
    private final long min;

    /**
     * 步长
     */
    private final Integer step;

    /**
     * 最大值
     */
    private final Long max;

    /**
     * 范围
     */
    private final Boolean loop;

    /**
     * 范围
     */
    private final Long singleRange;

    /**
     * 当前值
     */
    private final AtomicLong value;

    private volatile boolean over = false;

    public SeqRange(Long min, Integer step, Long max, Boolean loop, Long singleRange) {
        this.min = min;
        if (max == null || max <= 0L) {
            this.max = Long.MAX_VALUE;
        } else {
            this.max = max;
        }
        this.loop = loop;
        this.step = step;
        this.singleRange = singleRange;
        this.value = new AtomicLong(min);
        //System.out.println("min is : " + min + ",\nSeqRange value is : " + this.value + ",\nloop is : " + loop + ",\nmax is : " + max);
    }

    public long getValue() {
        Long currentValue;
        if (step != null && step > 0) {
            currentValue = value.getAndAdd(step);
        } else {
            currentValue = value.getAndIncrement();
        }

        if (loop && currentValue.compareTo(max) > 0) {
            over = true;
            return OVER_LOOP_MAX;
        }

        if (currentValue.compareTo(singleRange) > 0) {
            over = true;
            return OVER_SINGLE_RANGE;
        }
        return currentValue;
    }

}

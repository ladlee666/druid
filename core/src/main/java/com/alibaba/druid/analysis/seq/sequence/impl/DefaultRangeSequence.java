package com.alibaba.druid.analysis.seq.sequence.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alibaba.druid.analysis.seq.SeqException;
import com.alibaba.druid.analysis.seq.SeqRangeMgr;
import com.alibaba.druid.analysis.seq.range.BizName;
import com.alibaba.druid.analysis.seq.range.SeqRange;
import com.alibaba.druid.analysis.seq.sequence.RangeSequence;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.alibaba.druid.analysis.seq.range.impl.db.DbConstant.OVER_LOOP_MAX;
import static com.alibaba.druid.analysis.seq.range.impl.db.DbConstant.OVER_SINGLE_RANGE;

@Slf4j
public class DefaultRangeSequence implements RangeSequence {

    /**
     * 获取区间是加一把独占锁防止资源冲突
     */
    private final Lock lock = new ReentrantLock();

    /**
     * 序列号区间管理器
     */
    private SeqRangeMgr seqRangeMgr;

    /**
     * 当前序列号区间
     */
    private volatile SeqRange currentRange;

    /**
     * 需要获取区间的业务名称
     */
    private BizName bizName;

    @Override
    public long nextValue(String name) throws SeqException {
        if (StringUtils.isBlank(name)) {
            name = bizName.create();
        }
        if (null == currentRange) {
            lock.lock();
            try {
                if (null == currentRange) {
                    currentRange = seqRangeMgr.nextRange(name, null);
                }
            } finally {
                lock.unlock();
            }
        }

        long value = currentRange.getValue();
        // 当value值为-1时，表明区间的序列号已经分配完，需要重新获取区间
        // 当value值为-2时，表明当前循环区间的序列号已经分配完，需要重新从初始值开始
        if (value < 0) {
            lock.lock();
            try {
                for (; ; ) {
//                    log.info("current value is:{}, current range is over:{}", value, currentRange.isOver());
//                    System.out.println("current value is:" + value + ",current range is over:" + currentRange.isOver());
                    if (currentRange.isOver()) {
                        if (value == OVER_SINGLE_RANGE) {
                            currentRange = seqRangeMgr.nextRange(name, null);
                        } else if (value == OVER_LOOP_MAX) {
                            currentRange = seqRangeMgr.nextRange(name, 0L);
                        } else {
                            throw new SeqException("Invalid status number, value = " + value);
                        }
                    }
                    value = currentRange.getValue();
//                    log.info("next range value is:{}", value);
//                    System.out.println("next range value is:" + value);
                    if (value < 0) {
                        continue;
                    }
                    break;
                }
            } finally {
                lock.unlock();
            }
        }
//        if (value < 0) {
//            throw new SeqException("Sequence value overflow, value = " + value);
//        }
        return value;
    }

    @Override
    public String nextNo(String format, String name) throws SeqException {
        if (StringUtils.isBlank(format)) {
            return String.format("%s%05d", DateUtil.format(new Date(), DatePattern.PURE_DATE_FORMAT), nextValue(name));
        }
        return String.format(format, nextValue(name));
    }

    @Override
    public void setSeqRangeMgr(SeqRangeMgr seqRangeMgr) {
        this.seqRangeMgr = seqRangeMgr;
    }

    @Override
    public void setName(BizName bizName) {
        this.bizName = bizName;
    }
}

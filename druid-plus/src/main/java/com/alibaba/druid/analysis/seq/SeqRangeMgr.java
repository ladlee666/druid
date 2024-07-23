package com.alibaba.druid.analysis.seq;

import com.alibaba.druid.analysis.seq.range.SeqRange;

public interface SeqRangeMgr {

    /**
     * 初始化
     */
    void init();

    /**
     * 指定区间的下一个区间
     *
     * @param name
     * @return
     * @throws SeqException
     */
    SeqRange nextRange(String name,Long defaultVal) throws SeqException;
}

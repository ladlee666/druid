package com.alibaba.druid.analysis.seq.sequence;

import com.alibaba.druid.analysis.seq.SeqRangeMgr;
import com.alibaba.druid.analysis.seq.range.BizName;

public interface RangeSequence extends Sequence {

    /**
     * 设置区间管理器
     *
     * @param seqRangeMgr
     */
    void setSeqRangeMgr(SeqRangeMgr seqRangeMgr);

    /**
     * 设置获取序列号名称
     *
     * @param name 名称
     */
    void setName(BizName name);
}

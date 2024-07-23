package com.alibaba.druid.analysis.seq.sequence;

import com.alibaba.druid.analysis.seq.SeqException;

public interface Sequence {

    /**
     * 生成下一个序列号
     *
     * @return 序列号
     * @throws SeqException 序列号异常
     */
    long nextValue(String name) throws SeqException;

    /**
     * 下一个生成序号（带格式）
     *
     * @return
     * @throws SeqException
     */
    String nextNo(String format,String name) throws SeqException;

}

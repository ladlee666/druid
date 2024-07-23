package com.alibaba.druid.analysis.seq.builder;

import com.alibaba.druid.analysis.seq.SeqBuilder;
import com.alibaba.druid.analysis.seq.sequence.Sequence;
import com.alibaba.druid.analysis.spi.Service;

@Service(name = "redis")
public class RedisSeqBuilder implements SeqBuilder {

    @Override
    public Sequence build() {
        return null;
    }
}

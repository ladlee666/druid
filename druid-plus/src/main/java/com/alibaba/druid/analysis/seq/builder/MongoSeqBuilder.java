package com.alibaba.druid.analysis.seq.builder;

import com.alibaba.druid.analysis.seq.SeqBuilder;
import com.alibaba.druid.analysis.seq.range.BizName;
import com.alibaba.druid.analysis.seq.range.impl.mongo.MongoSeqRangeMgr;
import com.alibaba.druid.analysis.seq.sequence.Sequence;
import com.alibaba.druid.analysis.seq.sequence.impl.DefaultRangeSequence;
import com.alibaba.druid.analysis.spi.Service;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * @author LENOVO
 * @date 2024/5/29 15:25
 */
@Service(name = "mongo")
public class MongoSeqBuilder implements SeqBuilder {

    private MongoTemplate mongoTemplate;

    /**
     * *业务名称
     */
    private BizName bizName;

    /**
     * 步长[默认：1]
     */
    private int step = 1;

    /**
     * 序列号分配起始值[默认：0]
     */
    private long stepStart = 0L;

    /**
     * mongo文档名称
     */
    private String collectionName = "rangeInfo";

    @Override
    public Sequence build() {
        MongoSeqRangeMgr mgr = new MongoSeqRangeMgr();
        mgr.setStep(step);
        mgr.setCollectionName(collectionName);
        mgr.setStepStart(stepStart);
        mgr.setMongoTemplate(mongoTemplate);
        mgr.init();
        // 构建序列号
        DefaultRangeSequence sequence = new DefaultRangeSequence();
        sequence.setName(this.bizName);
        sequence.setSeqRangeMgr(mgr);
        return sequence;
    }

    public static MongoSeqBuilder create() {
        MongoSeqBuilder builder = new MongoSeqBuilder();
        return builder;
    }

    public MongoSeqBuilder bizName(BizName bizName) {
        this.bizName = bizName;
        return this;
    }

    public MongoSeqBuilder stepStart(long stepStart) {
        this.stepStart = stepStart;
        return this;
    }

    public MongoSeqBuilder collectionName(String collectionName) {
        this.collectionName = collectionName;
        return this;
    }

    public MongoSeqBuilder mongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
        return this;
    }
}

package com.alibaba.druid.analysis.seq.range.impl.mongo;

import com.alibaba.druid.analysis.seq.SeqException;
import com.alibaba.druid.analysis.seq.SeqRangeMgr;
import com.alibaba.druid.analysis.seq.range.RangeInfo;
import com.alibaba.druid.analysis.seq.range.SeqRange;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

/**
 * @author LENOVO
 * @date 2024/5/29 14:15
 */
@Data
public class MongoSeqRangeMgr implements SeqRangeMgr {

    private MongoTemplate mongoTemplate;

    /**
     * 区间步长
     */
    private int step;

    /**
     * 区间起始位置(从stepStart+1开始)
     */
    private long stepStart = 0L;

    /**
     * 默认的表名
     */
    private String collectionName;

    @Override
    public void init() {
        checkParam();
    }

    @Override
    public SeqRange nextRange(String name, Long defaultVal) throws SeqException {
        if (StringUtils.isBlank(name)) {
            throw new SeqException("[MongoSeqRangeMgr - nextRange]  param name is empty!");
        }
        RangeInfo rangeInfo = mongoTemplate.findOne(new Query(Criteria.where("name").is(name)), RangeInfo.class, collectionName);
        if (rangeInfo == null) {
            rangeInfo = new RangeInfo(name, 0L, 1, false, null, 1);
            mongoTemplate.save(rangeInfo, collectionName);
        }
        Long oldVal = rangeInfo.getValue();
        Integer step = rangeInfo.getStep() == null ? 0 : rangeInfo.getStep();

        long singleRange = oldVal + rangeInfo.getRange();
        long newVal = oldVal + step;

        rangeInfo.setValue(newVal);
//        mongoTemplate.update(getClass()).matching(new Query(Criteria.where("").));
        mongoTemplate.save(rangeInfo);

        return new SeqRange(oldVal + 1L, rangeInfo.getStep(), newVal, false, singleRange);
    }

    private void checkParam() {
        if (mongoTemplate == null) {
            throw new IllegalArgumentException("[MongoSeqRangeMgr] mongoTemplate is null.");
        }
        if (step <= 0) {
            throw new SecurityException("[MongoSeqRangeMgr] step must greater than 0.");
        }
        if (stepStart < 0) {
            throw new SecurityException("[MongoSeqRangeMgr] stepStart must greater than 0.");
        }
    }
}

package com.alibaba.druid.analysis.seq.range.impl.db;

import com.alibaba.druid.analysis.seq.SeqException;
import com.alibaba.druid.analysis.seq.SeqRangeMgr;
import com.alibaba.druid.analysis.seq.range.RangeInfo;
import com.alibaba.druid.analysis.seq.range.SeqRange;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.sql.DataSource;

@Data
public class DbSeqRangeMgr implements SeqRangeMgr {

    /**
     * 区间步长
     */
    private int step;

    /**
     * 区间起始位置(从stepStart+1开始)
     */
    private long stepStart;

    /**
     * 获取区间重试次数
     */
    private int retryTimes;

    /**
     * 数据源
     */
    private DataSource dataSource;

    /**
     * DB类型 ：oracle 、 myssql
     */
    private String dbType;

    /**
     * 默认的表名
     */
    private String tableName;

    private Long DEFAULT_RANGE = 100L;

    @Override
    public void init() {
        checkParam();
        DbHelper.createTable(dataSource, tableName, dbType);
    }

    @Override
    public SeqRange nextRange(String name, Long defaultValue) throws SeqException {
        if (StringUtils.isEmpty(name)) {
            throw new SeqException("[DbSeqRangeMgr - nextRange]  param name is empty!");
        }

        for (int i = 0; i < getRetryTimes(); i++) {
            RangeInfo rangeInfo = DbHelper.selectRange(dataSource, tableName, name, stepStart, step);
            if (rangeInfo == null) {
                continue;
            }
            // 范围和最大值
            Boolean loop = rangeInfo.getLoop();
            Long max = rangeInfo.getMax();

            // 当前值和步长
            Long oldVal = rangeInfo.getValue();
            Integer step = rangeInfo.getStep() == null ? getStep() : rangeInfo.getStep();

            // 单次取值范围
            Integer range = (rangeInfo.getRange() == null || rangeInfo.getRange() == 0) ? 1000 : rangeInfo.getRange();

            // 持久化到数据库的值
            long singleRange;
            long startVal;
            if (defaultValue == null) {
                singleRange = oldVal + range;
                startVal = oldVal + step;
            } else {
                singleRange = defaultValue + range;
                startVal = defaultValue + step;
            }

            boolean update = DbHelper.updateRange(dataSource, tableName, singleRange, oldVal, name);
            if (update) {
                SeqRange seqRange = new SeqRange(startVal, step, max, loop, singleRange);
                return seqRange;
            }
        }
        throw new SeqException("Retried too many times, retryTimes = " + getRetryTimes());
    }

    private void checkParam() {
        if (StringUtils.isBlank(dbType)) {
            throw new SecurityException("[DbSeqRangeMgr] dbType is null.");
        }
        if (step <= 0) {
            throw new SecurityException("[DbSeqRangeMgr] rangeStep must greater than 0.");
        }
        if (stepStart < 0) {
            throw new SecurityException("[DbSeqRangeMgr] stepStart < 0.");
        }
        if (retryTimes <= 0) {
            throw new SecurityException("[DbSeqRangeMgr] retryTimes must greater than 0.");
        }
        if (null == dataSource) {
            throw new SecurityException("[DbSeqRangeMgr] dataSource is null.");
        }
        if (StringUtils.isBlank(tableName)) {
            throw new SecurityException("[DbSeqRangeMgr] tableName is empty.");
        }
    }
}

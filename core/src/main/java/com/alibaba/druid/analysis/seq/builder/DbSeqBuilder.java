package com.alibaba.druid.analysis.seq.builder;

import com.alibaba.druid.analysis.seq.SeqBuilder;
import com.alibaba.druid.analysis.seq.sequence.Sequence;
import com.alibaba.druid.analysis.seq.range.BizName;
import com.alibaba.druid.analysis.seq.range.impl.db.DbSeqRangeMgr;
import com.alibaba.druid.analysis.seq.sequence.impl.DefaultRangeSequence;
import com.alibaba.druid.analysis.spi.Service;

import javax.sql.DataSource;

@Service(name = "db")
public class DbSeqBuilder implements SeqBuilder {

    /**
     * *数据库类型
     */
    private String dbType;

    /**
     * *数据源
     */
    private DataSource dataSource;

    /**
     * *业务名称
     */
    private BizName bizName;

    /**
     * 序列表
     */
    private String tableName = "SEQ_RANGE";

    /**
     * 失败重试的次数[默认：10]
     */
    private int retryTimes = 10;

    /**
     * 步长[默认：1]
     */
    private int step = 1;

    /**
     * 序列号分配起始值[默认：0]
     */
    private long stepStart = 0;

    @Override
    public Sequence build() {
        // 构建序列管理器
        DbSeqRangeMgr dbSeqRangeMgr = new DbSeqRangeMgr();
        dbSeqRangeMgr.setDataSource(this.dataSource);
        dbSeqRangeMgr.setDbType(this.dbType);
        dbSeqRangeMgr.setTableName(this.tableName.toUpperCase());
        dbSeqRangeMgr.setRetryTimes(this.retryTimes);
        dbSeqRangeMgr.setStep(this.step);
        dbSeqRangeMgr.setStepStart(this.stepStart);
        dbSeqRangeMgr.init();
        // 构建序列号
        DefaultRangeSequence sequence = new DefaultRangeSequence();
        sequence.setName(this.bizName);
        sequence.setSeqRangeMgr(dbSeqRangeMgr);
        return sequence;
    }

    public static DbSeqBuilder create() {
        DbSeqBuilder builder = new DbSeqBuilder();
        return builder;
    }

    public DbSeqBuilder dataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        return this;
    }

    public DbSeqBuilder tableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public DbSeqBuilder retryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
        return this;
    }

    public DbSeqBuilder step(int step) {
        this.step = step;
        return this;
    }

    public DbSeqBuilder bizName(BizName bizName) {
        this.bizName = bizName;
        return this;
    }

    public DbSeqBuilder stepStart(long stepStart) {
        this.stepStart = stepStart;
        return this;
    }

    public DbSeqBuilder dbType(String dbType) {
        this.dbType = dbType;
        return this;
    }
}

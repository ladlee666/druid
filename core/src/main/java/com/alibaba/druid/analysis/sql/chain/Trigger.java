package com.alibaba.druid.analysis.sql.chain;


import com.alibaba.druid.analysis.sql.model.ChainContext;

public class Trigger {

    public final AbstractProcessor processor;

    private final ChainContext context;

    public Trigger(AbstractProcessor processor, ChainContext context) {
        this.processor = processor;
        this.context = context;
    }

    public void doProcess() {
        processor.handle(context);
    }
}

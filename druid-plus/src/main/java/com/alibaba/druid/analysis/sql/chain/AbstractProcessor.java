package com.alibaba.druid.analysis.sql.chain;


import com.alibaba.druid.analysis.sql.model.ChainContext;

public abstract class AbstractProcessor {

    AbstractProcessor processor;

    public static AbstractProcessor link(AbstractProcessor firstProcessor, AbstractProcessor... processors) {
        if (firstProcessor == null) {
            return null;
        }
        AbstractProcessor head = firstProcessor;
        for (AbstractProcessor nextProcessor : processors) {
            head.processor = nextProcessor;
            head = nextProcessor;
        }
        return firstProcessor;
    }

    public Object doNext(ChainContext context) {
        if (processor == null) {
            return null;
        }
        processor.handle(context);
        return null;
    }

    public void handle(ChainContext context) {
        before(context);
        execute(context);
        after(context);
        doNext(context);
    }

    protected void checkArg(Object obj) {
        if (null == obj) {
            throw new IllegalArgumentException("无效的参数!");
        }
    }

    protected abstract void before(ChainContext context);

    protected abstract void execute(ChainContext context);

    protected abstract void after(ChainContext context);
}

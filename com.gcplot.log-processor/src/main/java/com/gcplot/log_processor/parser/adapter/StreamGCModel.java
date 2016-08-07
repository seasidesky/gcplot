package com.gcplot.log_processor.parser.adapter;

import com.google.common.base.Preconditions;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/7/16
 */
public class StreamGCModel extends GCModel {
    private static final int BATCH_SIZE_MIN_THRESHOLD = 1 << 5;
    private static final int DEFAULT_BATCH_SIZE = 1 << 9;

    private int batchSize = DEFAULT_BATCH_SIZE;
    public int getBatchSize() {
        return batchSize;
    }
    public StreamGCModel setBatchSize(int batchSize) {
        Preconditions.checkArgument(Integer.bitCount(batchSize) != 1, "Batch size should be of power of 2 (bs=2^N)!");
        Preconditions.checkArgument(batchSize > BATCH_SIZE_MIN_THRESHOLD, "Batch size should be higher than %s!", BATCH_SIZE_MIN_THRESHOLD);
        this.batchSize = batchSize;
        return this;
    }

    private Consumer<List<AbstractGCEvent<?>>> eventsConsumer;
    public void setEventsConsumer(Consumer<List<AbstractGCEvent<?>>> eventsConsumer) {
        this.eventsConsumer = eventsConsumer;
    }

    @Override
    public void add(AbstractGCEvent<?> abstractEvent) {
        if (allEvents.size() % batchSize == 0) {
            List<AbstractGCEvent<?>> toConsume = allEvents.subList(0, allEvents.size() / 2);
            allEvents = cutBy(allEvents, 2);
            if (stopTheWorldEvents.size() >= BATCH_SIZE_MIN_THRESHOLD) {
                stopTheWorldEvents = cutBy(stopTheWorldEvents, 2);
            }
            if (gcEvents.size() >= BATCH_SIZE_MIN_THRESHOLD) {
                gcEvents = cutBy(gcEvents, 2);
            }
            if (vmOperationEvents.size() >= BATCH_SIZE_MIN_THRESHOLD) {
                vmOperationEvents = cutBy(vmOperationEvents, 2);
            }
            if (concurrentGCEvents.size() >= BATCH_SIZE_MIN_THRESHOLD) {
                concurrentGCEvents = cutBy(concurrentGCEvents, 2);
            }
            if (currentNoFullGCEvents.size() >= BATCH_SIZE_MIN_THRESHOLD) {
                currentNoFullGCEvents = cutBy(currentNoFullGCEvents, 2);
            }
            if (fullGCEvents.size() >= BATCH_SIZE_MIN_THRESHOLD) {
                fullGCEvents = cutBy(fullGCEvents, 2);
            }
            eventsConsumer.accept(toConsume);
        }
        super.add(abstractEvent);
    }

    private <T> List<T> cutBy(List<T> list, int ratio) {
        return list.subList(list.size() / ratio, list.size());
    }
}
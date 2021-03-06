/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements.  See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.  You may obtain a
 * copy of the License at <p/> http://www.apache.org/licenses/LICENSE-2.0 <p/> Unless required by
 * applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 **/

package org.apache.kafka.copycat.runtime;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.copycat.errors.IllegalWorkerStateException;
import org.apache.kafka.copycat.sink.SinkTaskContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WorkerSinkTaskContext implements SinkTaskContext {
    private Map<TopicPartition, Long> offsets;
    private long timeoutMs;
    private KafkaConsumer<byte[], byte[]> consumer;

    public WorkerSinkTaskContext(KafkaConsumer<byte[], byte[]> consumer) {
        this.offsets = new HashMap<>();
        this.timeoutMs = -1L;
        this.consumer = consumer;
    }

    @Override
    public void offset(Map<TopicPartition, Long> offsets) {
        this.offsets.putAll(offsets);
    }

    @Override
    public void offset(TopicPartition tp, long offset) {
        offsets.put(tp, offset);
    }

    public void clearOffsets() {
        offsets.clear();
    }

    /**
     * Get offsets that the SinkTask has submitted to be reset. Used by the Copycat framework.
     * @return the map of offsets
     */
    public Map<TopicPartition, Long> offsets() {
        return offsets;
    }

    @Override
    public void timeout(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    /**
     * Get the timeout in milliseconds set by SinkTasks. Used by the Copycat framework.
     * @return the backoff timeout in milliseconds.
     */
    public long timeout() {
        return timeoutMs;
    }

    @Override
    public Set<TopicPartition> assignment() {
        if (consumer == null) {
            throw new IllegalWorkerStateException("SinkTaskContext may not be used to look up partition assignment until the task is initialized");
        }
        return consumer.assignment();
    }

    @Override
    public void pause(TopicPartition... partitions) {
        if (consumer == null) {
            throw new IllegalWorkerStateException("SinkTaskContext may not be used to pause consumption until the task is initialized");
        }
        try {
            consumer.pause(partitions);
        } catch (IllegalStateException e) {
            throw new IllegalWorkerStateException("SinkTasks may not pause partitions that are not currently assigned to them.", e);
        }
    }

    @Override
    public void resume(TopicPartition... partitions) {
        if (consumer == null) {
            throw new IllegalWorkerStateException("SinkTaskContext may not be used to resume consumption until the task is initialized");
        }
        try {
            consumer.resume(partitions);
        } catch (IllegalStateException e) {
            throw new IllegalWorkerStateException("SinkTasks may not resume partitions that are not currently assigned to them.", e);
        }
    }
}

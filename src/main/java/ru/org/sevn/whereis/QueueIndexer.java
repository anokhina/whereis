/*
 * Copyright 2018 Veronica Anokhina.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.org.sevn.whereis;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.tika.metadata.Metadata;

public class QueueIndexer extends Indexer {
    
    private final LinkedBlockingQueue<Metadata> queue;
    private final int capacity;
    
    public QueueIndexer(final int capacity) {
        this.capacity = capacity;
        queue = new LinkedBlockingQueue<>(capacity);
    }
    
    public void index(final Metadata ... metadata) throws IOException {
        if (metadata != null) {
            for (final Metadata m : metadata) {
                maycommit();
                synchronized(queue) {
                    queue.add(m);
                }
            }
        }
        super.index(metadata);
    }
    
    private void maycommit() throws IOException {
        synchronized(queue) {
            if (queue.size() >= capacity) {
                commit();
            }
        }
    }
    
    public void commit() throws IOException {
        Metadata[] metadata =  null;
        synchronized(queue) {
            metadata = clearQueue();
        }
        super.index(metadata);
    }
    
    private Metadata[] clearQueue() {
        Metadata[] metadata =  null;
        if (queue.size() > 0) {
            metadata = queue.toArray(new Metadata[queue.size()]);
            queue.clear();
        }
        return metadata;
    }
    
    protected void aboutFind() throws IOException {
        commit();
    }
}

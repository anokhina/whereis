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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import org.apache.tika.metadata.Metadata;

public class FileIndexerProcessor implements FileProcessor {
    
    private final Indexer indexer;
    private final MetadataExtractor metadataExtractor;
    private final String storeId;
    private final Path root;
    private final long indexAt;
    
    public FileIndexerProcessor(final long indexAt, final String storeId, final Path root, final Indexer indexer, final MetadataExtractor metadataExtractor) {
        this.indexAt = indexAt;
        this.storeId = storeId;
        this.root = root;
        this.indexer = indexer;
        this.metadataExtractor = metadataExtractor;
    }
    
    public void startIndexing(final Path path, final FileWalker fileWalker) throws IOException {
        final Metadata metadata = new Metadata();
        metadata.add(MetaParam.ID, MetadataExtractor.makeId(storeId, root, path));
        indexer.index(addIndexInfo(metadata));
        
        Files.walkFileTree(path, fileWalker);
        indexer.commit();
    }

    @Override
    public void processFile(final Path file, BasicFileAttributes attrs) throws Exception {
        indexer.index(addIndexInfo(metadataExtractor.parse(storeId, root, file, attrs)));
    }
    
    Metadata[] addIndexInfo(final Metadata... metadata) {
        for (final Metadata m : metadata) {
            m.add(MetaParam.INDEXED_AT, "" + indexAt);
        }
        return metadata;
    }

    public Indexer getIndexer() {
        return indexer;
    }

    public MetadataExtractor getMetadataExtractor() {
        return metadataExtractor;
    }

}

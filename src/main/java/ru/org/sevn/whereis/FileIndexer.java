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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.queryparser.classic.ParseException;

public class FileIndexer {

    public static final String EXCLUDE_FILE_MARKER = ".noindex";
    
    private final Indexer indexer= new Indexer();
    private final MetadataExtractor metadataExtractor = new MetadataExtractor();

    public Indexer getIndexer() {
        return indexer;
    }

    public MetadataExtractor getMetadataExtractor() {
        return metadataExtractor;
    }

    public void processDir(final String storeid, final Path path) throws IOException {
        final FileIndexerProcessor fileIndexerProcessor = new FileIndexerProcessor(storeid, path, indexer, metadataExtractor);
        final FileWalker fileWalker = new FileWalker(fileIndexerProcessor, EXCLUDE_FILE_MARKER);
        
        Files.walkFileTree(path, fileWalker);
    }

    public static void main(String[] args) throws Exception {
        FileIndexer fi = new FileIndexer();
        fi.processDir("zzz", new File("/home/sevn-arc/docs").toPath());
        
        try {
            System.out.println("<???");
            DocUtil.printDoc(fi.getIndexer().find(10, "test*"));
            System.out.println("???>");
        } catch (ParseException ex) {
            Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex);
        }
        //DocUtil.printDoc(fi.getIndexer().find(10, "test*"));
    }
}

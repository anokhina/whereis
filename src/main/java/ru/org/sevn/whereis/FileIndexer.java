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
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.standard.CaseSensitiveStandardAnalyzer;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.FSDirectory;

public class FileIndexer {

    public static final String EXCLUDE_FILE_MARKER = ".noindex";
    
    private final Indexer indexer= new QueueIndexer(20);
    private final MetadataExtractor metadataExtractor = new MetadataExtractor();

    public Indexer getIndexer() {
        return indexer;
    }

    public MetadataExtractor getMetadataExtractor() {
        return metadataExtractor;
    }

    public void processDir(final long indexTime, final String storeid, final Path path) throws IOException {
        final FileIndexerProcessor fileIndexerProcessor = new FileIndexerProcessor(indexTime, storeid, path, indexer, metadataExtractor);
        
        fileIndexerProcessor.startIndexing(path, new FileWalker(fileIndexerProcessor, EXCLUDE_FILE_MARKER));
        fileIndexerProcessor.getIndexer().commit();
        removeNotExisted(indexTime, storeid);
    }
    
    void removeNotExisted(final long indexTime, final String storeid) throws IOException {
        try {  
            final Query q = new QueryParser(MetaParam.ID, new CaseSensitiveStandardAnalyzer()).parse(
                    new SimpleQueryBuilder()
                            .add(MetaParam.STORE_ID, storeid)
                            .addRange(MetaParam.INDEXED_AT, Long.MIN_VALUE, indexTime - 1).build());
            System.out.println("<???");
            DocUtil.printDoc(getIndexer().find(10, q));
            System.out.println("???>");
            getIndexer().inWriter(w -> {
                w.deleteDocuments(q);
            });
            System.out.println("<???-");
            DocUtil.printDoc(getIndexer().find(10, q));
            System.out.println("???->");
            
            //DocUtil.printDoc(getIndexer().findByFields(10, MetaParam.ID, "zzz:", MetaParam.STORE_ID, storeid));
        } catch (ParseException ex) {
            Logger.getLogger(FileIndexer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    public static void main(String[] args) throws Exception {
        FileIndexer fi = new FileIndexer();
        fi.getIndexer().setIndex(FSDirectory.open(new File("wherisdb").toPath()));
        fi.processDir(System.currentTimeMillis(), "zzz", new File("/home/sevn-arc/docs").toPath());
        fi.processDir(System.currentTimeMillis(), "zzz", new File("/home/sevn-arc/docs2").toPath());
        
//        try {
//            System.out.println("<???");
//            //DocUtil.printDoc(fi.getIndexer().find(10, MetaParam.strName("meta:author"), "вальтер*"));
//            //str:meta:author:вальтер* +long:indexedAt:[-9223372036854775808 TO 1533931319698]
//            // "storeid:zzz +indexedAt:[-9223372036854775808 TO 3533931319698]"
//            DocUtil.printDoc(fi.getIndexer().find(10, MetaParam.strName("meta:author"), 
//                    new SimpleQueryBuilder().add(MetaParam.ID, "zzz").addRange(MetaParam.INDEXED_AT, Long.MIN_VALUE, System.currentTimeMillis()).build()));
//            System.out.println("???>");
//        } catch (ParseException ex) {
//            Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex);
//        }
        
        DocUtil.printDoc(fi.getIndexer().findByField(10, MetaParam.ID, "zzz:"));
        
        //DocUtil.printDoc(fi.getIndexer().findByFields(10, MetaParam.ID, "zzz:", MetaParam.STORE_ID, "zzz"));
        //DocUtil.printDoc(fi.getIndexer().findByFields(10, MetaParam.STORE_ID, "zzz"));
        //DocUtil.printDoc(fi.getIndexer().find(10, "test*"));
        
        System.out.println("$$$$$$$$$$$");
        BooleanQuery.Builder qb = fi.getIndexer().findByFieldsQuery(MetaParam.strName("meta:author"), "вальтер*");
        qb.add(LongPoint.newRangeQuery(MetaParam.longName(MetaParam.INDEXED_AT), Long.MIN_VALUE, System.currentTimeMillis()), BooleanClause.Occur.MUST);
        System.out.println("++++++" + qb.build().toString(""));
        DocUtil.printDoc(fi.getIndexer().findByFields(10, qb.build()));
        //DocUtil.printDoc(fi.getIndexer().findRange(10, MetaParam.longName(MetaParam.INDEXED_AT), Long.MIN_VALUE, System.currentTimeMillis()));
    }
}
//https://habr.com/post/277509/

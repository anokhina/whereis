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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.tika.metadata.Metadata;

//https://github.com/c0rp-aubakirov/lucene-tutorial/blob/master/src/main/java/kz/kaznu/lucene/BasicSearchExamples.java
//https://habr.com/post/277509/
public class Indexer {
    
    private final Analyzer analyzer = new StandardAnalyzer();
    private final Directory index = new RAMDirectory();
    
    
    private IndexWriterConfig getIndexWriterConfig() {
        return new IndexWriterConfig(analyzer);
    }
    
    public void index(final Metadata ... metadata) throws IOException {
        if (metadata != null) {
            final IndexWriter w = new IndexWriter(index, getIndexWriterConfig());
            try {
                for (final Metadata m : metadata) {
                    add(w, m);
                }
                w.commit();
            } finally {
                System.out.println("+++++++++++++++++++++++++++++");
                w.close();
            }
        }
    }
    
    public void commit() throws IOException {}
    
    void add(final IndexWriter w, final Metadata metadata) throws IOException {
        final Document doc = new Document();
        for (final String n : metadata.names()) {
            doc.add(getField(n, metadata.get(n)));
            System.out.println("==" + n + "=" + metadata.get(n));
        }
        //w.addDocument(doc);
        w.updateDocument(new Term (MetaParam.ID, metadata.get(MetaParam.ID)), doc); 
    }
    
    private IndexableField getField(final String n, final String c) {
        if (MetaParam.TITLE.equals(n) || MetaParam.TEXT.equals(n)) {
            return new TextField(n, c, isStoreField(n));
        }
        return new StringField(n, c, isStoreField(n));
    }
    
    private Field.Store isStoreField(final String n) {
        if (MetaParam.TEXT.equals(n)) {
            return Field.Store.NO;
        }
        return Field.Store.YES;
    }
    
    protected void aboutFind() throws IOException {}

    public List<Document> find(final int hitsPerPage, final String querystr) throws IOException, ParseException {
        aboutFind();
        Query q = new QueryParser(MetaParam.FILE_NAME, analyzer).parse(querystr);//TODO
        return find(hitsPerPage, q);
    }
    
    public List<Document> find(final int hitsPerPage, final Query q) throws IOException {
        aboutFind();
        final ArrayList<Document> ret = new ArrayList<>();
        try (IndexReader reader = DirectoryReader.open(index)) {
            final IndexSearcher searcher = new IndexSearcher(reader);
            final TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
            
            searcher.search(q, collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;

            for (int i = 0; i < hits.length; ++i) {
                ret.add(searcher.doc(hits[i].doc));
            }
        }
        return ret;
    }
    
    public List<Document> findByFields(final int limit, final String ... fieldValues) throws IOException {
        final HashMap<String, String> map = new HashMap<>();
        for(int i = 1; i < fieldValues.length; i+= 2) {
            map.put(fieldValues[i-1], fieldValues[i]);
        }
        return findByFields(limit, map);
    }
    
    public List<Document> findByField(final int limit, final String fieldName, final String text) throws IOException {
        try (IndexReader reader = DirectoryReader.open(index)) {
            return findByField(limit, reader, fieldName, text);
        }
    }
    
    List<Document> findByField(final int limit, final IndexReader reader, final String fieldName, final String text) throws IOException {
        aboutFind();
        final ArrayList<Document> result = new ArrayList<>();
        final IndexSearcher searcher = new IndexSearcher(reader);
        final TopDocs td = searcher.search(
            new BooleanQuery.Builder().add(new TermQuery(new Term(fieldName, text)), BooleanClause.Occur.MUST).build()
                , limit);
        for (final ScoreDoc sd : td.scoreDocs) {
            result.add(reader.document(sd.doc));
        }
        return result;
    }
    
    public List<Document> findByFields(final int limit, final Map<String, String> fieldValues) throws IOException {
        try (IndexReader reader = DirectoryReader.open(index)) {
            return findByFields(limit, reader, fieldValues);
        }
    }
    
    List<Document> findByFields(final int limit, final IndexReader reader, final Map<String, String> fieldValues) throws IOException {
        aboutFind();
        final ArrayList<Document> result = new ArrayList<>();
        final IndexSearcher searcher = new IndexSearcher(reader);
        final BooleanQuery.Builder builder = new BooleanQuery.Builder();
        for (final String fieldName : fieldValues.keySet()) {
            builder.add(new TermQuery(new Term(fieldName, fieldValues.get(fieldName))), BooleanClause.Occur.MUST);
        }
        final TopDocs td = searcher.search(builder.build(), limit);
        for (final ScoreDoc sd : td.scoreDocs) {
            result.add(reader.document(sd.doc));
        }
        return result;
    }
}

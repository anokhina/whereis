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
import org.apache.lucene.document.Document;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

public class IndexFinder {
    
    private Directory index = new RAMDirectory();
    
    public <T extends IndexFinder> T setIndex(final Directory index) {
        this.index = index;
        return (T)this;
    }

    public Directory getIndex() {
        return index;
    }
    
    protected void aboutFind() throws IOException {}

    public List<Document> findRange(final int hitsPerPage, final String fieldName, final Long from, final Long to) throws IOException {
        return find(hitsPerPage, LongPoint.newRangeQuery(fieldName, from, to));
    }
    
    public List<Document> find(final int hitsPerPage, final Query q) throws IOException {
        aboutFind();
        final ArrayList<Document> ret = new ArrayList<>();
        try (IndexReader reader = DirectoryReader.open(index)) {
            final IndexSearcher searcher = new IndexSearcher(reader);
            final TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, hitsPerPage);
            
            searcher.search(q, collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;

            for (int i = 0; i < hits.length; ++i) {
                ret.add(searcher.doc(hits[i].doc));
            }
        }
        return ret;
    }
    
    public List<Document> findByFields(final int limit, final String ... fieldValues) throws IOException {
        return findByFields(limit, findByFieldsQuery(fieldValues).build());
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
        return findByFields(limit, findByFieldsQuery(fieldValues).build());
    }
    
    public List<Document> findByFields(final int limit, final Query q) throws IOException {
        try (IndexReader reader = DirectoryReader.open(index)) {
            return findByFields(limit, reader, q);
        }
    }
    
    List<Document> findByFields(final int limit, final IndexReader reader, final Query q) throws IOException {
        aboutFind();
        final ArrayList<Document> result = new ArrayList<>();
        final IndexSearcher searcher = new IndexSearcher(reader);
        
        final TopDocs td = searcher.search(q, limit);
        for (final ScoreDoc sd : td.scoreDocs) {
            result.add(reader.document(sd.doc));
        }
        return result;
    }
    
    public static BooleanQuery.Builder findByFieldsQuery(final Map<String, String> fieldValues) {
        final BooleanQuery.Builder builder = new BooleanQuery.Builder();
        return findByFieldsQueryEq(builder, fieldValues);
    }
    
    public static BooleanQuery.Builder findByFieldsQueryEq(BooleanQuery.Builder builder, final Map<String, String> fieldValues) {
        for (final String fieldName : fieldValues.keySet()) {
            builder.add(new TermQuery(new Term(fieldName, fieldValues.get(fieldName))), BooleanClause.Occur.MUST);
        }
        return builder;
    }
    
    public static BooleanQuery.Builder findByFieldsQueryLikePrefix(BooleanQuery.Builder builder, final Map<String, String> fieldValues) {
        for (final String fieldName : fieldValues.keySet()) {
            builder.add(new PrefixQuery(new Term(fieldName, fieldValues.get(fieldName))), BooleanClause.Occur.MUST);
        }
        return builder;
    }
    
    public static BooleanQuery.Builder findByFieldsQueryLike(BooleanQuery.Builder builder, final Map<String, String> fieldValues) {
        for (final String fieldName : fieldValues.keySet()) {
            builder.add(new WildcardQuery(new Term(fieldName, fieldValues.get(fieldName))), BooleanClause.Occur.MUST);
        }
        return builder;
    }

    public static BooleanQuery.Builder findByFieldsQuery(final String ... fieldValues) {
        return findByFieldsQuery(toMap(fieldValues));
    }
    
    public static HashMap<String, String> toMap(final String ... fieldValues) {
        final HashMap<String, String> map = new HashMap<>();
        for(int i = 1; i < fieldValues.length; i+= 2) {
            map.put(fieldValues[i-1], fieldValues[i]);
        }
        return map;
    }
}

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
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.util.BytesRef;
import org.apache.tika.metadata.Metadata;

//https://github.com/c0rp-aubakirov/lucene-tutorial/blob/master/src/main/java/kz/kaznu/lucene/BasicSearchExamples.java
//https://habr.com/post/277509/
public class Indexer extends IndexFinder {
    
    private final Analyzer analyzer = new /*CaseSensitive*/StandardAnalyzer();

    public Analyzer getAnalyzer() {
        return analyzer;
    }
    
    private IndexWriterConfig getIndexWriterConfig() {
        return new IndexWriterConfig(analyzer);
    }
    
    public void inWriter(final ThrowableConsumer<IndexWriter, IOException> consumer) throws IOException {
        final IndexWriter w = new IndexWriter(getIndex(), getIndexWriterConfig());
        try {
            consumer.accept(w);
            w.commit();
        } finally {
            System.out.println("+++++++++++++++++++++++++++++");
            w.close();
        }
    }
    
    public void index(final Metadata ... metadata) throws IOException {
        if (metadata != null) {
            inWriter(w -> {
                for (final Metadata m : metadata) {
                    add(w, m);
                }
            });
        }
    }
    
    public void commit() throws IOException {}
    
    void add(final IndexWriter w, final Metadata metadata) throws IOException {
        final Document doc = new Document();
        for (final String n : metadata.names()) {
            addFields(doc, getField(n, metadata.get(n)));
            if (MetaParam.PATH.equals(n)) {
                System.out.println("==" + n + "=" + metadata.get(n));
            }
        }
        //w.addDocument(doc);
        w.updateDocument(new Term (MetaParam.ID, metadata.get(MetaParam.ID)), doc); 
    }
    
    private void addFields(final Document doc, final IndexableField ... fields) {
        for (final IndexableField f : fields) {
            doc.add(f);
        }
    }
    
    protected IndexableField[] getField(final String n, final String c) {
        final Field.Store isStoreField = isStoreField(n);
        switch(n) {
            case MetaParam.ALL:
            case MetaParam.SPATH:
            case MetaParam.TITLE:
            case MetaParam.TEXT:
                return new IndexableField[] { 
                    new TextField(n, trim(n, isStoreField, c), isStoreField), 
                    new TextField(MetaParam.strName(n), toSearchable(c), Field.Store.NO) 
                };
            case MetaParam.FILE_CREATIONTIME:
            case MetaParam.FILE_LASTACCESSTIME:
            case MetaParam.FILE_LASTMODIFIEDTIME:
            case MetaParam.INDEXED_AT:
                return new IndexableField[] { 
                    new LongPoint(MetaParam.LONG_ + n, Long.valueOf(c)), 
                    new StringField(n, c, isStoreField) 
                };
        }
        return new IndexableField[] { 
            new StringField(n, trim(n, isStoreField, c), isStoreField), 
            new TextField(MetaParam.strName(n), toSearchable(c), Field.Store.NO),
            new SortedDocValuesField(n, new BytesRef(c))
        };
    }
    
    protected Field.Store isStoreField(final String n) {
        switch(n) {
            case MetaParam.ALL:
            case MetaParam.SPATH:
            case MetaParam.TEXT:
                return Field.Store.NO;
        }
        return Field.Store.YES;
    }
    
    protected String toSearchable(String s) {
        return s.toLowerCase().replace("/", " ").replace("_", " ");
    }
    
    private static final int TERM_LENGTH = 32765;
    
    protected String trim(final String n, final Field.Store isStoreField, String s) {
        if (isStoreField == Field.Store.YES) {
            try {
                final byte[] bytes = s.getBytes("UTF-8");
                if (bytes.length > TERM_LENGTH) {
                    System.out.println(">>>TRIMMED:" + n);
                    System.err.println(">>>TRIMMED:" + n + ":" + s);
                    return new String(Arrays.copyOf(bytes, TERM_LENGTH), "UTF-8");
                }
            } catch (UnsupportedEncodingException ex) {
            }
        }
        return s;
    }
}

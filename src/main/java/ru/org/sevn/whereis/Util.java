/*
 * Copyright 2020 Veronica Anokhina.
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
import java.util.List;
import org.apache.lucene.analysis.standard.CaseSensitiveStandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.FSDirectory;

public class Util {
    public static Indexer configDbPath (final Indexer indexer, final String dbpath) throws IOException {
        return configDbPath (indexer, new File (dbpath));
    }

    public static Indexer configDbPath (final Indexer indexer, final File dbpathFile) throws IOException {
        dbpathFile.mkdirs ();
        indexer.setIndex (Util.openDb(dbpathFile));
        return indexer;
    }
    
    public static FSDirectory openDb(final File dbpathFile) throws IOException {
        return FSDirectory.open (dbpathFile.toPath ());
    }
    
    public static FSDirectory openDb(final String dbpath) throws IOException {
        return Util.openDb(new File (dbpath));
    }
    
    public static QueryParser getQueryParser() {
        final QueryParser qp = new QueryParser(MetaParam.ALL, new CaseSensitiveStandardAnalyzer());
        qp.setAllowLeadingWildcard(true);
        return qp;
    }
    
    public static Query parse(final String query) throws ParseException {
        return getQueryParser().parse(query);
    }
    
    public static List<Document> find(final IndexFinder idxfinder, final String query, final int lines) throws ParseException, IOException {
        return find(idxfinder, getQueryParser(), query, lines);
    }
    
    public static List<Document> find(final IndexFinder idxfinder, final QueryParser qp, final String query, final int lines) throws ParseException, IOException {
        return find(idxfinder, qp.parse(query), lines);
    }
    
    public static List<Document> find(final IndexFinder idxfinder, final Query q, final int lines) throws IOException {
        return idxfinder.find(lines, q);
    }
}

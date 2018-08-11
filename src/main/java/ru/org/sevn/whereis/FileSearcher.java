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
import org.apache.lucene.analysis.standard.CaseSensitiveStandardAnalyzer;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.FSDirectory;

public class FileSearcher {
    public static void main(String[] args) throws Exception {
        //final IndexFinder fi = new IndexFinder();
        final CompoundIndexFinder fi = new CompoundIndexFinder()
                .add(new IndexFinder().setIndex(FSDirectory.open(new File("wherisdb").toPath())))
                .add(new IndexFinder().setIndex(FSDirectory.open(new File("wherisdb1").toPath())))
                ;
        DocUtil.printDoc(fi.findByField(10, MetaParam.ID, "zzz:").get());
        
        System.out.println("$$$$$$$$$$$");
        //MetaParam.strName("meta:author"), "вальтер*"
        final String storeid = "zzz";
        final long indexTime = System.currentTimeMillis();
        final Query q = new QueryParser(MetaParam.ID, new CaseSensitiveStandardAnalyzer()).parse(
                new SimpleQueryBuilder()
                        .add(MetaParam.STORE_ID, storeid)
                        .add(MetaParam.strName("meta:author"), "вальтер*")
                        .addRange(MetaParam.INDEXED_AT, Long.MIN_VALUE, indexTime - 1).build());
        System.out.println("<???"+q.toString(""));
        DocUtil.printDoc(fi.find(10, q).get(3));
        System.out.println("???>");
    }    
}

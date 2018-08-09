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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;

public class DocUtil {
    
    public static void printDoc(final List<Document> docs) {
        //        metadata.add(MetaParam.INDEXED_AT, DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now().atZone(ZoneId.systemDefault())));

        for(final Document d : docs) {
            printDoc(d);
            System.out.println();
        }
    }
    
    public static void printDoc(final Document doc) {
        for (IndexableField f : doc.getFields()) {
            switch(f.name()) {
                case MetaParam.INDEXED_AT:
                case MetaParam.FILE_CREATIONTIME:
                case MetaParam.FILE_LASTACCESSTIME:
                case MetaParam.FILE_LASTMODIFIEDTIME:
                    System.out.println(">["+f.name()+"] " + printMS(f.stringValue()));
                    break;
                default:
                    System.out.println(">["+f.name()+"] " + f.stringValue());
            }
        }
    }
    
    private static String printMS(final String s) {
        try {
            final Long ms = Long.valueOf(s);
            return DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(ms), ZoneId.systemDefault()));
        } catch (Exception e) {
        }
        return s;
    }
    
}

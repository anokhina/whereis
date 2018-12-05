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

public class MetaParam {
    public static final String STORE_ID = "storeid";
    public static final String ID = "id";
    public static final String PATH = "path";
    public static final String SPATH = "spath";
    public static final String ALL = "all";
    public static final String TEXT = "text";
    public static final String TITLE = "title";
    public static final String INDEXED_AT = "indexedAt";
    public static final String INDEX_ID = "idxId";
    
    public static final String FILE_CHECK_SUM = "file:checkSum";
    public static final String FILE_CREATIONTIME = "file:creationTime";
    public static final String FILE_LASTACCESSTIME = "file:lastAccessTime";
    public static final String FILE_LASTMODIFIEDTIME = "file:lastModifiedTime";

    public static final String FILE_ISDIRECTORY = "file:isDirectory";
    public static final String FILE_ISOTHER = "file:isOther";
    public static final String FILE_ISREGULARFILE = "file:isRegularFile";
    public static final String FILE_ISSYMBOLICLINK = "file:isSymbolicLink";
    public static final String FILE_SIZE = "file:size";
    public static final String FILE_NAME = "file:name";
    public static final String FILE_ = "file:";
    public static final String STR_ = "str:";
    public static final String LONG_ = "long:";
    
    public static final String[] FIELDS = new String[] {
        INDEX_ID,
        ID, STORE_ID, PATH, TEXT, TITLE, INDEXED_AT, 
        FILE_CHECK_SUM,
        FILE_CREATIONTIME, FILE_LASTMODIFIEDTIME,
        FILE_NAME, FILE_SIZE,
        FILE_ISDIRECTORY
    };
 
    public static String longName(final String n) {
        return MetaParam.LONG_ + n;
    }
    public static String strName(final String n) {
        return MetaParam.STR_ + n;
    }
}

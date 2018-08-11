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

import org.apache.lucene.queryparser.classic.QueryParser;

//http://lucene.apache.org/core/2_9_4/queryparsersyntax.html
public class SimpleQueryBuilder {
    
    private final StringBuilder stringBuilder = new StringBuilder();
    
    public SimpleQueryBuilder addEsc(final String fieldName, final String v) {
        return add(fieldName, QueryParser.escape(v));
    }
    
    public SimpleQueryBuilder adds(final String ...str) {
        if (str != null) {
            for(int i = 1; i < str.length; i+=2) {
                add(str[i-1], str[i]);
            }
        }
        return this;
    }
    
    public SimpleQueryBuilder addRanges(final String ...str) {
        if (str != null) {
            for(int i = 2; i < str.length; i+=3) {
                addRange(str[i-2], Long.valueOf(str[i-1]), Long.valueOf(str[i]));
            }
        }
        return this;
    }
    
    public SimpleQueryBuilder add(final String fieldName, final String v) {
        if (stringBuilder.length() > 0) {
            stringBuilder.append(" AND ");
        }
        stringBuilder.append(QueryParser.escape(fieldName)).append(":").append(v).append(" ");
        return this;
    }
    public SimpleQueryBuilder addRange(final String fieldName, final long from, final long to) {
        if (stringBuilder.length() > 0) {
            stringBuilder.append(" AND ");
        }
        stringBuilder.append(QueryParser.escape(fieldName)).append(":").append("[").append(from).append(" TO ").append(to).append("]").append(" ");
        return this;
    }
    
    public String build() {
        return stringBuilder.toString();
    }
    
    public String build(final String prefix) {
        if (prefix.length() > 0) {
            return prefix + " AND " + stringBuilder.toString();
        }
        return stringBuilder.toString();
    }
}

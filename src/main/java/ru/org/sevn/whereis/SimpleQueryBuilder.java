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
    
    public SimpleQueryBuilder add(final String fieldName, final String v) {
        if (stringBuilder.length() > 0) {
            stringBuilder.append(" AND ");
        }
        stringBuilder.append(QueryParser.escape(fieldName)).append(":").append(QueryParser.escape(v)).append(" ");
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
}

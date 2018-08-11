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
import java.util.List;
import java.util.Map;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;

public class CompoundIndexFinder {
    final ArrayList<IndexFinder> finders = new ArrayList<>();
    
    public <T extends CompoundIndexFinder> T add(final IndexFinder f) {
        finders.add(f);
        return (T)this;
    }
    
    public <T extends CompoundIndexFinder> T remove(final IndexFinder f) {
        finders.remove(f);
        return (T)this;
    }
    
    public static class Result {
        
        private final ArrayList<List<Document>> results = new ArrayList<>();
        
        public List<Document> get() {
            return get(Integer.MAX_VALUE);
        }
        
        public List<Document> get(int maxlen) {
            final ArrayList<Document> ret = new ArrayList<>();
            int j = 0;
            int I = 0;
            outer: do {
                for(int i = 0; i < results.size(); i++) {
                    if (ret.size() >= maxlen) {
                        break outer;
                    }
                    final List<Document> lst = results.get(i);
                    if (j == 0) {
                        I = Math.max(lst.size(), I);
                    }
                    if (j < lst.size()) {
                        ret.add(lst.get(j));
                    }
                }
                j++;
            } while (j < I);
            
            return ret;
        }
    }
    
    private void mix(final Result result, final List<Document> lst) {
        result.results.add(lst);
    }
    
    public Result findRange(final int hitsPerPage, final String fieldName, final Long from, final Long to) throws IOException {
        final Result result = new Result();
        for(final IndexFinder f : finders) {
            mix(result, f.findRange(hitsPerPage, fieldName, from, to));
        }
        return result;
    }
    
    public Result find(final int hitsPerPage, final Query q) throws IOException {
        final Result result = new Result();
        for(final IndexFinder f : finders) {
            mix(result, f.find(hitsPerPage, q));
        }
        return result;
    }
    
    public Result findByFields(final int limit, final String ... fieldValues) throws IOException {
        final Result result = new Result();
        for(final IndexFinder f : finders) {
            mix(result, f.findByFields(limit, fieldValues));
        }
        return result;
    }
    
    public Result findByField(final int limit, final String fieldName, final String text) throws IOException {
        final Result result = new Result();
        for(final IndexFinder f : finders) {
            mix(result, f.findByFields(limit, fieldName, text));
        }
        return result;
    }    
    public Result findByFields(final int limit, final Map<String, String> fieldValues) throws IOException {
        final Result result = new Result();
        for(final IndexFinder f : finders) {
            mix(result, f.findByFields(limit, fieldValues));
        }
        return result;
    }
    
    public Result findByFields(final int limit, final Query q) throws IOException {
        final Result result = new Result();
        for(final IndexFinder f : finders) {
            mix(result, f.findByFields(limit, q));
        }
        return result;
    }    
}

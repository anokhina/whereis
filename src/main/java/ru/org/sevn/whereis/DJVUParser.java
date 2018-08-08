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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AbstractParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.XHTMLContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class DJVUParser extends AbstractParser {

    private static final Set<MediaType> SUPPORTED_TYPES = new HashSet<>(Arrays.asList(
            MediaType.image("djvu"),
            MediaType.image("vnd.djvu")
    ));

    public Set<MediaType> getSupportedTypes(ParseContext context) {
        return SUPPORTED_TYPES;
    }

    @Override
    public void parse(InputStream stream, ContentHandler handler, Metadata metadata, ParseContext context) throws IOException, SAXException, TikaException {
        metadata.set(Metadata.CONTENT_TYPE, "image/vnd.djvu");
        XHTMLContentHandler xhtml = new XHTMLContentHandler(handler, metadata);
        xhtml.startDocument();
        
        xhtml.element("p", getTextAny(metadata.get(MetaParam.PATH)));
        xhtml.endDocument();
    }
    
    private String getTextAny(final String url) {
        try {
            return getText(url);
        } catch (Exception ex) {
            Logger.getLogger(DJVUParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
    
    private String getText(final String url) throws IOException, InterruptedException {
        final StringBuilder sb = new StringBuilder();
        if (url != null) {
            ProcessBuilder ps = new ProcessBuilder("djvutxt", url);
            ps.redirectErrorStream(true);

            Process pr = ps.start();

            try (BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream()))) {
                String line;
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                }
                pr.waitFor();
            }
        }
        return sb.toString();
    }
    
}

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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.DatatypeConverter;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.utils.XMLReaderUtils;
import org.xml.sax.SAXException;

public class MetadataExtractor {
    
    /*
<?xml version="1.0" encoding="UTF-8"?>
<properties>
  <parsers>
    <parser class="org.apache.tika.parser.DefaultParser">
      <mime-exclude>audio/mpeg</mime-exclude>
      <mime-exclude>image/vnd.djvu</mime-exclude>
    </parser>
    <parser class="org.apache.tika.parser.mp3.Mp3ParserFake">
      <mime>audio/mpeg</mime>
    </parser>
    <parser class="ru.org.sevn.whereis.DJVUParser">
      <mime>image/vnd.djvu</mime>
    </parser>
    
  </parsers>
</properties>    
    */
    public static final String tikaCfg = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
"<properties>\n" +
"  <parsers>\n" +
"    <parser class=\"org.apache.tika.parser.DefaultParser\">\n" +
"      <mime-exclude>audio/mpeg</mime-exclude>\n" +
"      <mime-exclude>image/vnd.djvu</mime-exclude>\n" +
"    </parser>\n" +
"    <parser class=\"org.apache.tika.parser.mp3.Mp3ParserFake\">\n" +
"      <mime>audio/mpeg</mime>\n" +
"    </parser>\n" +
"    <parser class=\"ru.org.sevn.whereis.DJVUParser\">\n" +
"      <mime>image/vnd.djvu</mime>\n" +
"    </parser>\n" +
"    \n" +
"  </parsers>\n" +
"</properties>";
    
    private String getTikaConfigString() {
        return tikaCfg;
    }
    
    private TikaConfig getTikaConfig() throws TikaException, SAXException, IOException {
        InputStream stream = new ByteArrayInputStream(getTikaConfigString().getBytes(StandardCharsets.UTF_8));
        return new TikaConfig(XMLReaderUtils.getDocumentBuilder().parse(stream));
    }
    
    private String getCheckSum(final Path path) {
        byte[] b;
        try {
            b = Files.readAllBytes(path);
            byte[] hash = MessageDigest.getInstance("MD5").digest(b);
            return DatatypeConverter.printHexBinary(hash);
        } catch (Exception ex) {
            Logger.getLogger(MetadataExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private void readFileAttr(final Path path, final BasicFileAttributes attr, final Metadata metadata) throws IOException {
        metadata.add(MetaParam.FILE_ISDIRECTORY, str(attr.isDirectory()));
        if (!attr.isDirectory()) {
            final String checkSum = getCheckSum(path);
            if (checkSum != null) {
                metadata.add(MetaParam.FILE_CHECK_SUM, checkSum);
            }
        }
        metadata.add(MetaParam.FILE_CREATIONTIME, str(attr.creationTime()));
        //metadata.add(MetaParam.FILE_LASTACCESSTIME, str(attr.lastAccessTime()));
        metadata.add(MetaParam.FILE_LASTMODIFIEDTIME, str(attr.lastModifiedTime()));

        metadata.add(MetaParam.FILE_ISOTHER, str(attr.isOther()));
        metadata.add(MetaParam.FILE_ISREGULARFILE, str(attr.isRegularFile()));
        metadata.add(MetaParam.FILE_ISSYMBOLICLINK, str(attr.isSymbolicLink()));
        metadata.add(MetaParam.FILE_SIZE, str(attr.size()));
        metadata.add(MetaParam.FILE_NAME, path.getFileName().toString());
    }
    
    private String str(boolean b) {
        return "" + b;
    }
    private String str(long b) {
        return "" + b;
    }
    private String str(FileTime b) {
        //return DateTimeFormatter.ISO_DATE_TIME.format(b.toInstant().atZone(ZoneId.systemDefault()));
        return "" + b.toMillis();
    }
    
    public Metadata parse(final String storeId, final Path root, final Path path) throws IOException, SAXException, TikaException {
        return parse(storeId, root, path, Files.readAttributes(path, BasicFileAttributes.class));
    }
    public Metadata parse(final String storeId, final Path root, final Path path, final BasicFileAttributes attr) throws IOException, SAXException, TikaException {
        final Metadata metadata = new Metadata();
        metadata.add(MetaParam.PATH, path.toAbsolutePath().toString());
        metadata.add(MetaParam.SPATH, path.toAbsolutePath().toString().toLowerCase().replace("/", " ").replace("_", " "));
        metadata.add(MetaParam.STORE_ID, storeId);
        metadata.add(MetaParam.ID, makeId(storeId, root, path));
        readFileAttr(path, attr, metadata);
        if (!attr.isDirectory() && attr.size() > 0) {
        
            final BodyContentHandler handler = new BodyContentHandler(-1);
            final TikaConfig tc = getTikaConfig();
            final ParseContext parseContext = new ParseContext();
            final AutoDetectParser parser = new AutoDetectParser(tc);

            try (final InputStream input = new FileInputStream(path.toFile())) {
                parser.parse(input, handler, metadata, parseContext);
            } catch (Exception e) {
                System.err.println(">>>ERROR:" + path);
                e.printStackTrace();
            }
            metadata.add(MetaParam.TEXT, handler.toString().replaceAll("\n|\r|\t", " "));
        }
        final StringBuilder all = new StringBuilder();
        for(final String n : metadata.names()) {
            all.append(clear(metadata.get(n))).append(" ");
        }
        
        metadata.add(MetaParam.ALL, all.toString());
        return metadata;
    }
    
    private String clear(final String all) {
        return all.toLowerCase().replace("/", " ").replace("_", " ");
    }
    
    static String makeId(final String storeId, final Path root, final Path path) {
        return storeId + ":" + root.relativize(path);
    }
    
    public static void main(String arg[]) throws Exception {
        final Path root = new File("/mnt/winh/").toPath();
        final MetadataExtractor me = new MetadataExtractor();
        {
            final Metadata metadata = me.parse("zzz", root, new File("/mnt/winh/pub/DjVu_Tech_Primer.djvu").toPath());
            for (final String n : metadata.names()) {
                System.out.println("==" + n + "=" + metadata.get(n));
            }
        }
        {
            final Metadata metadata = me.parse("zzz", root, new File("/mnt/winh/pub/Зачисление в 5 класс.docx").toPath());
            for (final String n : metadata.names()) {
                System.out.println("==" + n + "=" + metadata.get(n));
            }
        }
    }    
}

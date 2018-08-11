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
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Arrays;
import org.apache.commons.cli.*;
import org.apache.lucene.analysis.standard.CaseSensitiveStandardAnalyzer;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.FSDirectory;

public class WhereIs {
    public static void printUsage() {
        final HelpFormatter formatter = new HelpFormatter() {
            private Appendable renderWrappedTextBlock(StringBuffer sb, int width, int nextLineTabStop, String text) {
                try {
                    BufferedReader in = new BufferedReader(new StringReader(text));
                    String line;
                    boolean firstLine = true;
                    while ((line = in.readLine()) != null) {
                        if (!firstLine) {
                            sb.append(getNewLine());
                        } else {
                            firstLine = false;
                        }
                        renderWrappedText(sb, width, nextLineTabStop, line);
                    }
                } catch (IOException e) //NOPMD
                {
                    // cannot happen
                }

                return sb;
            }
            
            public void printWrapped(PrintWriter pw, int width, int nextLineTabStop, String text) {
                StringBuffer sb = new StringBuffer(text.length());

                renderWrappedTextBlock(sb, width, nextLineTabStop, text);
                pw.print(sb.toString());
            }
        };
        formatter.setSyntaxPrefix("");
        PrintWriter pw = new PrintWriter(System.out);
        final String usg = " java -jar " + "WhereIs.jar" + " ";
        
        pw.append("\nIndex files:\n").append(usg).append("[i id path_to_index ");
        formatter.printUsage(pw, formatter.getWidth(), "", dbOptions(new Options()));
        pw.append("]").append("\n");
        pw.append("\nSearch in indexed:\n").append(usg).append("[s \"query\"" );
        formatter.printUsage(pw, formatter.getWidth(), "", dbOptions(queryOptions(new Options())));
        pw.append("]").append("\n");
        pw.append("\nBuild query string:\n").append(usg).append("[q ");
        formatter.printUsage(pw, formatter.getWidth(), "", queryOptions(new Options()));
        pw.append("]").append("\n");
        pw.append("\nPrint common field names:\n").append(usg).append("[n ");
        pw.append("]").append("\n");
        pw.append("\nMake folder store skel: UNSUPPORTED\n").append(usg).append("[skel ");
        pw.append("]").append("\n");

        pw.append("\n");
        formatter.printOptions(pw, formatter.getWidth(), dbOptions(queryOptions(new Options())), formatter.getLeftPadding(), formatter.getDescPadding());        
        pw.flush();
        
    }
    
    public static void indexIt(final String args[]) {
        if (args.length < 2) {
            printUsage();
        } else {
            try {
                final CommandLine line = new DefaultParser().parse( dbOptions(new Options()), Arrays.copyOfRange(args, 2, args.length) );
                String dbpath = "wherisdb";
                if (line.hasOption("db")) {
                    dbpath = line.getOptionValue("db");
                }
                final FileIndexer fi = new FileIndexer();
                fi.getIndexer().setIndex(FSDirectory.open(new File(dbpath).toPath()));
                fi.processDir(System.currentTimeMillis(), args[0], new File(args[1]).toPath());
                System.out.println("Indexing finished.");
            }
            catch( ParseException exp ) {
                System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
            } catch (IOException ex) {
                System.err.println( "Indexing failed.  Reason: " + ex.getMessage() );
            }
            
        }
    }
    
    public static void searchIt(final String args[]) {
        if (args.length < 1) {
            printUsage();
        } else {
            if (args.length > 1) {
                try {
                    final CommandLine line = new DefaultParser().parse( 
                            queryOptions(dbOptions(new Options())), Arrays.copyOfRange(args, 1, args.length) );
                    final CompoundIndexFinder fi = new CompoundIndexFinder();
                    if (line.hasOption("db")) {
                        for(final String dbpath : line.getOptionValues("db")) {
                            fi.add(new IndexFinder().setIndex(FSDirectory.open(new File(dbpath).toPath())));
                        }
                    } else {
                        fi.add(new IndexFinder().setIndex(FSDirectory.open(new File("wherisdb").toPath())));
                    }
                    final SimpleQueryBuilder sqb = new SimpleQueryBuilder();
                    queryCommandLine(sqb, line);
                    sqb.build(args[0]);
                    
                    final Query q = new QueryParser(MetaParam.ID, new CaseSensitiveStandardAnalyzer()).parse(sqb.build(args[0]));
                    System.out.println("Query=" + q.toString(""));
                    
                    System.out.println("<???");
                    DocUtil.printDoc(fi.find(10, q).get());
                    System.out.println("???>");
                }
                catch( ParseException exp ) {
                    System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
                } catch (IOException ex) {
                    System.err.println( "Search failed.  Reason: " + ex.getMessage() );
                } catch (org.apache.lucene.queryparser.classic.ParseException ex) {
                    System.err.println( "Parsing query failed.  Reason: " + ex.getMessage() );
                }
                
            } else {
                
            }
        }
    }
    
    private static Options dbOptions(final Options options) {
        return options.addOption(
            Option.builder("db").numberOfArgs(1).argName("path").desc("path to index storage").build()
        );
    }
    
    private static Options queryOptions(final Options options) {
        return options.addOption(
                Option.builder("q").numberOfArgs(2).argName("field value_expression").desc("query for parameter").build()
        ).addOption(
                Option.builder("qr").numberOfArgs(3).argName("field from till").desc("query for range").build()
        );
    }
    
    private static CommandLine queryCommandLine(final SimpleQueryBuilder sqb, final CommandLine line) {
        if( line.hasOption( "q" ) ) {
            sqb.adds(line.getOptionValues("q"));
        }            
        if( line.hasOption( "qr" ) ) {
            sqb.addRanges(line.getOptionValues("qr"));
        }      
        return line;
    }
    
    public static StringBuilder printQuery(String args[]) {
        final SimpleQueryBuilder sqb = new SimpleQueryBuilder();
        
        try {
            queryCommandLine(sqb, new DefaultParser().parse( queryOptions(new Options()), args ));
        }
        catch( ParseException exp ) {
            System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
        }
        
        final StringBuilder sb = new StringBuilder();
        sb.append(sqb.build());
        return sb;
    }
    
    public static StringBuilder printNames() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Fields:").append("\n");
        for (final String n : MetaParam.FIELDS) {
            sb.append(n).append("\n");
        }
        return sb;
    }
    
    public static void out(final StringBuilder sb) {
        System.out.println(sb.toString());
    }
    
    public static void main(String[] args) throws Exception {
//        args = new String[] {"q", "-q", "f", "v", "-q", "f1", "v1", "-qr", "rrr", "1", "2"};
//        args = new String[] {"i", "zzz", "/home/sevn-arc/docs", "-db", "target/wherisdb"};
//        args = new String[] {"s", "", "-db", "target/wherisdb", "-q", MetaParam.STORE_ID, "zzz", "-q", MetaParam.strName("meta:author"), "вальтер*", "-qr", MetaParam.INDEXED_AT, "" + Long.MIN_VALUE, "" + System.currentTimeMillis()};
        
        if (args.length == 0) {
            printUsage();
            return ;
        }
        switch(args[0].toLowerCase()) {
            case "i":
                indexIt(Arrays.copyOfRange(args, 1, args.length));
                break;
            case "s":
                searchIt(Arrays.copyOfRange(args, 1, args.length));
                break;
            case "q":
                out(printQuery(Arrays.copyOfRange(args, 1, args.length)));
                break;
            case "n":
                out(printNames());
                break;
            case "skel":
                break;
                default: 
                    printUsage();
                    return;
        }
    }
    

}

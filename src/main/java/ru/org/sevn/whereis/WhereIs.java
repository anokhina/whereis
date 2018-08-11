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

import java.util.Arrays;
import org.apache.commons.cli.*;

public class WhereIs {
    public static StringBuilder printUsage() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Usage:").append("\n");
        return sb;
    }
    
    public static StringBuilder printQuery(String args[]) {
        final SimpleQueryBuilder sqb = new SimpleQueryBuilder();
        
        Options options = new Options();
        Option q  = Option.builder("q").numberOfArgs(2).argName("property value").build();
        Option qr  = Option.builder("qr").numberOfArgs(3).argName("property from till").build();
        options.addOption(qr).addOption(q);
        
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse( options, args );
            if( line.hasOption( "q" ) ) {
                sqb.adds(line.getOptionValues("q"));
            }            
            if( line.hasOption( "qr" ) ) {
                sqb.addRanges(line.getOptionValues("qr"));
            }            
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
    
    public static String[] zzz(final String opt, final Options options, final String[] args) {
        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse( options, args );
            if( line.hasOption( opt ) ) {
                // initialise the member variable
                return line.getOptionValues( opt );
            }            
        }
        catch( ParseException exp ) {
            // oops, something went wrong
            System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
        }
        return null;
    }
    
    public static void main(String[] args) throws Exception {
        Options options = new Options();
        //Option input = new Option("i", "input", true, "input file path");
        //Option o = Option.builder("O").hasArgs().valueSeparator('=').build();
options.addOption( OptionBuilder.withLongOpt( "block-size" )
                                .withDescription( "use SIZE-byte blocks" )
                                .hasArg()
                                .withArgName("SIZE")
                                .withValueSeparator('=')
                                .create() );        
        HelpFormatter formatter = new HelpFormatter();
Option property  = OptionBuilder.withArgName( "property=value" )
                                .hasArgs(4)
                                .withValueSeparator()
                                .withDescription( "use value for given property" )
                                .create( "D" );
options.addOption(property);
        formatter.printHelp("utility-name", options);
//Option property  = OptionBuilder.withArgName( "property=value" )
//                                .hasArgs(2)
//                                .withValueSeparator()
//                                .withDescription( "use value for given property" )
//                                .create( "D" );        
        
        args = new String[] {"q", "-q", "f", "v", "-q", "f1", "v1", "-qr", "rrr", "1", "2"};
        if (args.length == 0) {
            printUsage();
            return ;
        }
        switch(args[0].toLowerCase()) {
            case "i":
                break;
            case "s":
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
                    out(printUsage());
                    return;
        }
    }    
}

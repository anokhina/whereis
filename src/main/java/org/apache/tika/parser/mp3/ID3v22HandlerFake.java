package org.apache.tika.parser.mp3;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.tika.exception.TikaException;
import org.apache.tika.parser.mp3.ID3v2Frame.RawTag;
import org.apache.tika.parser.mp3.ID3v2Frame.RawTagIterator;
import org.xml.sax.SAXException;

/**
 * This is used to parse ID3 Version 2.2 Tag information from an MP3 file,
 * if available.
 *
 * @see <a href="http://id3lib.sourceforge.net/id3/id3v2-00.txt">MP3 ID3 Version 2.2 specification</a>
 */
public class ID3v22HandlerFake implements ID3Tags {
    private String title;
    private String artist;
    private String album;
    private String year;
    private String composer;
    private String genre;
    private String trackNumber;
    private String albumArtist;
    private String disc;
    private List<ID3Tags.ID3Comment> comments = new ArrayList<ID3Tags.ID3Comment>();

    public ID3v22HandlerFake(ID3v2Frame frame)
            throws IOException, SAXException, TikaException {
        RawTagIterator tags = new RawV22TagIterator(frame);
        while (tags.hasNext()) {
            RawTag tag = tags.next();
            if (tag.name.equals("TT2")) {
                title = getTagString(tag.data, 0, tag.data.length); 
            } else if (tag.name.equals("TP1")) {
                artist = getTagString(tag.data, 0, tag.data.length); 
            } else if (tag.name.equals("TP2")) {
                albumArtist = getTagString(tag.data, 0, tag.data.length); 
            } else if (tag.name.equals("TAL")) {
                album = getTagString(tag.data, 0, tag.data.length); 
            } else if (tag.name.equals("TYE")) {
                year = getTagString(tag.data, 0, tag.data.length); 
            } else if (tag.name.equals("TCM")) {
                composer = getTagString(tag.data, 0, tag.data.length); 
            } else if (tag.name.equals("COM")) {
                comments.add( getComment(tag.data, 0, tag.data.length) ); 
            } else if (tag.name.equals("TRK")) {
                trackNumber = getTagString(tag.data, 0, tag.data.length); 
            } else if (tag.name.equals("TPA")) {
                disc = getTagString(tag.data, 0, tag.data.length); 
            } else if (tag.name.equals("TCO")) {
                genre = extractGenre( getTagString(tag.data, 0, tag.data.length) );
            }
        }
    }

    private String getTagString(byte[] data, int offset, int length) {
        return ID3v2FrameFake.getTagString(data, offset, length);
    }
    private ID3Tags.ID3Comment getComment(byte[] data, int offset, int length) {
        return ID3v2FrameFake.getComment(data, offset, length);
    }
    
    protected static String extractGenre(String rawGenre) {
       int open = rawGenre.indexOf("(");
       int close = rawGenre.indexOf(")");
       if (open == -1 && close == -1) {
          return rawGenre;
       } else if (open < close) {
           String genreStr = rawGenre.substring(0, open).trim();
           try {
               int genreID = Integer.parseInt(rawGenre.substring(open+1, close));
               return ID3Tags.GENRES[genreID];
           } catch(ArrayIndexOutOfBoundsException invalidNum) {
              return genreStr;
           } catch(NumberFormatException notANum) {
              return genreStr;
           }
       } else {
          return null;
       }
    }

    public boolean getTagsPresent() {
        return true;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getYear() {
        return year;
    }
    
    public String getComposer() {
        return composer;
    }

    public List<ID3Tags.ID3Comment> getComments() {
        return comments;
    }

    public String getGenre() {
        return genre;
    }

    public String getTrackNumber() {
        return trackNumber;
    }

    public String getAlbumArtist() {
        return albumArtist;
    }

    public String getDisc() {
        return disc;
    }

    /**
     * ID3v22 doesn't have compilations,
     *  so returns null;
     */
    public String getCompilation() {
        return null;
    }

    private class RawV22TagIterator extends RawTagIterator {
        private RawV22TagIterator(ID3v2Frame frame) {
            frame.super(3, 3, 1, 0);
        }
    }
}

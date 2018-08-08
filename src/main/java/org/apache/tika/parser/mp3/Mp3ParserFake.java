package org.apache.tika.parser.mp3;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TailStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.metadata.XMPDM;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AbstractParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.XHTMLContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class Mp3ParserFake extends AbstractParser {
    
    /** Serial version UID */
    private static final long serialVersionUID = 8537074922934844370L;

    private static final Set<MediaType> SUPPORTED_TYPES =
        Collections.singleton(MediaType.audio("mpeg"));

    public Set<MediaType> getSupportedTypes(ParseContext context) {
        return SUPPORTED_TYPES;
    }


    public void parse(
            InputStream stream, ContentHandler handler,
            Metadata metadata, ParseContext context)
            throws IOException, SAXException, TikaException {
        metadata.set(Metadata.CONTENT_TYPE, "audio/mpeg");
        metadata.set(XMPDM.AUDIO_COMPRESSOR, "MP3");

        XHTMLContentHandler xhtml = new XHTMLContentHandler(handler, metadata);
        xhtml.startDocument();

        // Create handlers for the various kinds of ID3 tags
        ID3TagsAndAudio audioAndTags = getAllTagHandlers(stream, handler);

        if (audioAndTags.tags.length > 0) {
           CompositeTagHandler tag = new CompositeTagHandler(audioAndTags.tags);

           metadata.set(TikaCoreProperties.TITLE, tag.getTitle());
           metadata.set(TikaCoreProperties.CREATOR, tag.getArtist());
           metadata.set(XMPDM.ARTIST, tag.getArtist());
           metadata.set(XMPDM.COMPOSER, tag.getComposer());
           metadata.set(XMPDM.ALBUM, tag.getAlbum());
           metadata.set(XMPDM.RELEASE_DATE, tag.getYear());
           metadata.set(XMPDM.GENRE, tag.getGenre());
           metadata.set(XMPDM.DURATION, audioAndTags.duration);

           List<String> comments = new ArrayList<String>();
           for (ID3Tags.ID3Comment comment : tag.getComments()) {
              StringBuffer cmt = new StringBuffer();
              if (comment.getLanguage() != null) {
                 cmt.append(comment.getLanguage());
                 cmt.append(" - ");
              }
              if (comment.getDescription() != null) {
                 cmt.append(comment.getDescription());
                 if (comment.getText() != null) {
                    cmt.append("\n");
                 }
              }
              if (comment.getText() != null) {
                 cmt.append(comment.getText());
              }
              
              comments.add(cmt.toString());
              metadata.add(XMPDM.LOG_COMMENT.getName(), cmt.toString());
           }

           xhtml.element("h1", tag.getTitle());
           xhtml.element("p", tag.getArtist());

            // ID3v1.1 Track addition
            if (tag.getTrackNumber() != null) {
                xhtml.element("p", tag.getAlbum() + ", track " + tag.getTrackNumber());
                metadata.set(XMPDM.TRACK_NUMBER, tag.getTrackNumber());
            } else {
                xhtml.element("p", tag.getAlbum());
            }
            xhtml.element("p", tag.getYear());
            xhtml.element("p", tag.getGenre());
            xhtml.element("p", String.valueOf(audioAndTags.duration));
            for (String comment : comments) {
               xhtml.element("p", comment);
            }
        }
        if (audioAndTags.audio != null) {
            metadata.set("samplerate", String.valueOf(audioAndTags.audio.getSampleRate()));
            metadata.set("channels", String.valueOf(audioAndTags.audio.getChannels()));
            metadata.set("version", audioAndTags.audio.getVersion());
            
            metadata.set(
                    XMPDM.AUDIO_SAMPLE_RATE,
                    Integer.toString(audioAndTags.audio.getSampleRate()));
            if(audioAndTags.audio.getChannels() == 1) {
               metadata.set(XMPDM.AUDIO_CHANNEL_TYPE, "Mono");
            } else if(audioAndTags.audio.getChannels() == 2) {
               metadata.set(XMPDM.AUDIO_CHANNEL_TYPE, "Stereo");
            } else if(audioAndTags.audio.getChannels() == 5) {
               metadata.set(XMPDM.AUDIO_CHANNEL_TYPE, "5.1");
            } else if(audioAndTags.audio.getChannels() == 7) {
               metadata.set(XMPDM.AUDIO_CHANNEL_TYPE, "7.1");
            }
        }
        if (audioAndTags.lyrics != null && audioAndTags.lyrics.hasLyrics()) {
           xhtml.startElement("p", "class", "lyrics");
           xhtml.characters(audioAndTags.lyrics.lyricsText);
           xhtml.endElement("p");
        }

        xhtml.endDocument();
    }

    /**
     * Scans the MP3 frames for ID3 tags, and creates ID3Tag Handlers
     *  for each supported set of tags. 
     */
    public static ID3TagsAndAudio getAllTagHandlers(InputStream stream, ContentHandler handler1)
           throws IOException, SAXException, TikaException {
       ID3v24Handler v24 = null;
       ID3v23HandlerFake v23 = null;
       ID3v22HandlerFake v22 = null;
       ID3v1HandlerFake v1 = null;
       AudioFrame firstAudio = null;

       TailStream tailStream = new TailStream(stream, 10240+128);
       MpegStreamFake mpegStream = new MpegStreamFake(tailStream);

       // ID3v2 tags live at the start of the file
       // You can apparently have several different ID3 tag blocks
       // So, keep going until we don't find any more
       MP3Frame f;
       ArrayList<ID3v2Frame> frames = new ArrayList<>();
       while ((f = ID3v2Frame.createFrameIfPresent(mpegStream)) != null) {
           if(f instanceof ID3v2Frame) {
               frames.add((ID3v2Frame)f);
               ID3v2Frame id3F = (ID3v2Frame)f;
               if (id3F.getMajorVersion() == 4) {
                   v24 = new ID3v24Handler(id3F);
               } else if(id3F.getMajorVersion() == 3) {
                   v23 = new ID3v23HandlerFake(id3F);
               } else if(id3F.getMajorVersion() == 2) {
                   v22 = new ID3v22HandlerFake(id3F);
               }
           }
       }

        // Now iterate over all audio frames in the file
        AudioFrame frame = mpegStream.nextFrame();
        float duration = 0;
        while (frame != null)
        {
            duration += frame.getDuration();
            if (firstAudio == null)
            {
                firstAudio = frame;
            }
            mpegStream.skipFrame();
            frame = mpegStream.nextFrame();
        }

       // ID3v1 tags live at the end of the file
       // Lyrics live just before ID3v1, at the end of the file
       // Search for both (handlers seek to the end for us)
       LyricsHandlerFake lyrics = new LyricsHandlerFake(tailStream.getTail());
       v1 = lyrics.id3v1;

       // Go in order of preference
       // Currently, that's newest to oldest
       List<ID3Tags> tags = new ArrayList<ID3Tags>();

       if(v24 != null && v24.getTagsPresent()) {
          tags.add(v24);
       }
       if(v23 != null && v23.getTagsPresent()) {
          tags.add(v23);
       }
       if(v22 != null && v22.getTagsPresent()) {
          tags.add(v22);
       }
       if(v1 != null && v1.getTagsPresent()) {
          tags.add(v1);
       }
       
       ID3TagsAndAudio ret = new ID3TagsAndAudio();
       ret.audio = firstAudio;
       ret.lyrics = lyrics;
       ret.tags = tags.toArray(new ID3Tags[tags.size()]);
       ret.frames = frames.toArray(new ID3v2Frame[frames.size()]);
       ret.duration = duration;
       return ret;
    }

    public static class ID3TagsAndAudio {
        public ID3Tags[] tags;
        public AudioFrame audio;
        public LyricsHandlerFake lyrics;
        public float duration;
        public ID3v2Frame[] frames;
    }

}

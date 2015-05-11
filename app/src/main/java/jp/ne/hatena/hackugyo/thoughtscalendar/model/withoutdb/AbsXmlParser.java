package jp.ne.hatena.hackugyo.thoughtscalendar.model.withoutdb;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

abstract public class AbsXmlParser<T> {
    // We don't use namespaces
    protected static final String ns = null;

    public ArrayList<T> parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }
    }

    protected ArrayList<T> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<T> entries = new ArrayList<T>();

        // parser.require(XmlPullParser.START_TAG, ns, "feed");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag

            skip(parser);
            //            if (name.equals("Event")) {
            //                entries.add(readEventEntry(parser));
            //            } else {
            //                skip(parser);
            //            }
        }
        return entries;
    }

    protected T readEventEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "entry");
        String title = null;
        String summary = null;
        String link = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            skip(parser);
            //            if (name.equals("Name")) {
            //                title = readTitle(parser);
            //            } else if (name.equals("DateStart")) {
            //                summary = readDateFrom(parser);
            //            } else {
            //                skip(parser);
            //            }
        }
        return null;
    }

    // For the tags title and summary, extracts their text values.
    protected String readText(XmlPullParser parser, final String key) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, key);
        String title = readTextImpl(parser);
        parser.require(XmlPullParser.END_TAG, ns, key);
        return title;
    }

    // For the tags title and summary, extracts their text values.
    private String readTextImpl(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    protected void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

}

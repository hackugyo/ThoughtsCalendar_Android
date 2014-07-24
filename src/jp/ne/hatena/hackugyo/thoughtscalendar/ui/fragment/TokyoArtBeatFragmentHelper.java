package jp.ne.hatena.hackugyo.thoughtscalendar.ui.fragment;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import jp.ne.hatena.hackugyo.thoughtscalendar.model.TokyoArtBeatEvent;
import jp.ne.hatena.hackugyo.thoughtscalendar.model.withoutdb.AbsXmlParser;
import jp.ne.hatena.hackugyo.thoughtscalendar.util.LogUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

public class TokyoArtBeatFragmentHelper {
    private TokyoArtBeatFragmentHelper() {

    }

    public static ArrayList<TokyoArtBeatEvent> parseXml(InputStream response) {
        try {
            return parseXmlImpl(response);
        } catch (IOException e) {
        }
        return new ArrayList<TokyoArtBeatEvent>();
    }

    private static ArrayList<TokyoArtBeatEvent> parseXmlImpl(InputStream response) throws IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(response, null);
            parser.nextTag();
            return new TokyoArtBeatXmlParser().readFeed(parser);
        } catch (XmlPullParserException e) {
            LogUtils.e("xmlp", e);
        } finally {
            response.close();
        }
        return null;
    }

    public static class TokyoArtBeatXmlParser extends AbsXmlParser<TokyoArtBeatEvent> {

        @Override
        protected ArrayList<TokyoArtBeatEvent> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
            ArrayList<TokyoArtBeatEvent> entries = new ArrayList<TokyoArtBeatEvent>();

            // parser.require(XmlPullParser.START_TAG, ns, "feed");
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                // Starts by looking for the entry tag
                if (name.equals("Event")) {
                    entries.add(readEventEntry(parser));
                } else {
                    skip(parser);
                }
            }
            return entries;
        }

        protected TokyoArtBeatEvent readEventEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
            parser.require(XmlPullParser.START_TAG, ns, "Event");
            String title = null;
            String dateFrom = null;
            String imageUrl = null;
            String description = null;
            String address = null;
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                if (name.equals("Name")) {
                    title = readTitle(parser);
                } else if (name.equals("DateStart")) {
                    dateFrom = readDateFrom(parser);
                } else if (name.equals("Image")) {
                    imageUrl = readLink(parser);
                } else if (name.equals("Description")) {
                    description = readText(parser, "Description");
                } else if (name.equals("Venue")) {
                    address = readAddress(parser);
                } else {
                    skip(parser);
                }
            }
            return new TokyoArtBeatEvent(title, dateFrom, imageUrl, description, address);
        }

        // Processes title tags in the feed.
        private String readTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
            return readText(parser, "Name");
        }

        private String readDateFrom(XmlPullParser parser) throws IOException, XmlPullParserException {
            return readText(parser, "DateStart");
        }

        // Processes link tags in the feed.
        private String readLink(XmlPullParser parser) throws IOException, XmlPullParserException {
            String link = "";
            parser.require(XmlPullParser.START_TAG, ns, "Image");
            String tag = parser.getName();
            if (tag.equals("Image")) {
                link = parser.getAttributeValue(null, "src");
                parser.nextTag();
            }
            parser.require(XmlPullParser.END_TAG, ns, "Image");
            return link;
        }

        protected String readAddress(XmlPullParser parser) throws XmlPullParserException, IOException {
            parser.require(XmlPullParser.START_TAG, ns, "Venue");
            String address = null;
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                if (name.equals("Name")) {
                    address = readText(parser, "Name");
                } else {
                    skip(parser);
                }
            }
            return address;
        }

    }
}

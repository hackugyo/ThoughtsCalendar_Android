package jp.ne.hatena.hackugyo.thoughtscalendar.ui.fragment;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import jp.ne.hatena.hackugyo.thoughtscalendar.model.AttendingEvent;
import jp.ne.hatena.hackugyo.thoughtscalendar.model.TokyoArtBeatEvent;
import jp.ne.hatena.hackugyo.thoughtscalendar.model.withoutdb.AbsXmlParser;
import jp.ne.hatena.hackugyo.thoughtscalendar.util.LogUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.util.Xml;

public class TokyoArtBeatFragmentHelper {
    private TokyoArtBeatFragmentHelper() {

    }

    /**
     * <a href="http://www.tokyoartbeat.com/list/event_comingsoon.ja.xml">Tokyo Art Beat API</a>のパース．
     * @param response
     * @return イベント一覧
     */
    public static ArrayList<AttendingEvent> parseXml(InputStream response) {
        try {
            return parseXmlImpl(response);
        } catch (IOException e) {
        }
        return new ArrayList<AttendingEvent>();
    }

    private static ArrayList<AttendingEvent> parseXmlImpl(InputStream response) throws IOException {
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
    
    public static void saveToAttend(Context context, AttendingEvent item, String hashTag, String ownersAccount) {
        item.save(context);
    }

    public static class TokyoArtBeatXmlParser extends AbsXmlParser<AttendingEvent> {

        @Override
        protected ArrayList<AttendingEvent> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
            ArrayList<AttendingEvent> entries = new ArrayList<AttendingEvent>();

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

        @Override
        protected TokyoArtBeatEvent readEventEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
            parser.require(XmlPullParser.START_TAG, ns, "Event");
            String title = null;
            String dateFrom = null;
            String imageUrl = null;
            String description = null;
            String address = null;
            String eventId = null;
            String url = null;
            String location = null;
            
            String tag = parser.getName();
            if (tag.equals("Event")) {
                eventId = parser.getAttributeValue(null, "id");
                url = parser.getAttributeValue(null, "href");
            }
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
                    String[] readAddressAndLocation = readAddressAndLocation(parser);
                    address = readAddressAndLocation[0];
                    location = readAddressAndLocation[1];
                } else {
                    skip(parser);
                }
            }
            return new TokyoArtBeatEvent(eventId, title, dateFrom, imageUrl, description, address, location, url);
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
        
        protected String readLink(XmlPullParser parser, final String key) throws IOException, XmlPullParserException {
            String link = "";
            parser.require(XmlPullParser.START_TAG, ns, key);
            String tag = parser.getName();
            if (tag.equals(key)) {
                link = parser.getAttributeValue(null, "href");
                parser.nextTag();
            }
            parser.require(XmlPullParser.END_TAG, ns, key);
            return link;
        }

        protected String[] readAddressAndLocation(XmlPullParser parser) throws XmlPullParserException, IOException {
            parser.require(XmlPullParser.START_TAG, ns, "Venue");
            String[] result = new String[]{"", ""};
            String address = null;
            String location = null;
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                if (name.equals("Name")) {
                    location = readText(parser, "Name");
                } else if (name.equals("Address")) {
                    address = readText(parser, "Address");
                } else {
                    skip(parser);
                }
            }
            result[0] = address;
            result[1] = location;
            return result;
        }

    }
}
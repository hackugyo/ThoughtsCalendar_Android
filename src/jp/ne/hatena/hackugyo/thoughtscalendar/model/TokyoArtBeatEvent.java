package jp.ne.hatena.hackugyo.thoughtscalendar.model;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import jp.ne.hatena.hackugyo.thoughtscalendar.util.CalendarUtils;

public class TokyoArtBeatEvent implements Comparable<TokyoArtBeatEvent> {

    private final TokyoArtBeatEvent self = this;

    private String mTitle;
    private String mDateFrom;
    private String mImageUrl;
    private Calendar mCalendar;

    private String mDescription;

    private String mAddress;

    public TokyoArtBeatEvent(String title, String dateFrom, String imageUrl, String description, String address) {
        // TODO 20140716 もっと実装する
        mTitle = title;
        mDateFrom = dateFrom;
        mImageUrl = imageUrl;
        mDescription = description;
        mAddress = address;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDateFromString() {
        return mDateFrom;
    }

    public String getImageUrl() {
        if (mImageUrl != null && mImageUrl.contains("images/nopic")) return null;
        return mImageUrl;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getAddress() {
        return mAddress;
    }

    public Calendar getDateFrom() {
        if (mCalendar == null) {
            mCalendar = CalendarUtils.parseGregorianDate(mDateFrom, "-");
        }
        return mCalendar;
    }

    /***********************************************
     * Comparable *
     **********************************************/
    @Override
    public int compareTo(TokyoArtBeatEvent another) {
        if (self == null && another == null) {
            return 0;
        } else if (self == null) {
            return 1;
        } else if (another == null) {
            return -1;
        } else if (self.getDateFrom() == null) {
            if (another.getDateFrom() == null) {
                return 0;
            } else {
                return 1;
            }
        } else if (another.getDateFrom() == null) {
            return -1;
        } else {
            return self.getDateFrom().compareTo(another.getDateFrom());
        }
    }

    public static Comparator<TokyoArtBeatEvent> ascending() {
        return new Comparator<TokyoArtBeatEvent>() {
            public int compare(TokyoArtBeatEvent lhs, TokyoArtBeatEvent rhs) {
                if (lhs == null && rhs == null) {
                    return 0;
                } else if (lhs == null) {
                    return 1;
                } else if (rhs == null) {
                    return -1;
                } else if (lhs.getDateFrom() == null) {
                    if (rhs.getDateFrom() == null) {
                        return 0;
                    } else {
                        return 1;
                    }
                } else if (rhs.getDateFrom() == null) {
                    return -1;
                } else {
                    return lhs.getDateFrom().compareTo(rhs.getDateFrom());
                }
            }
        };
    }

    public static Comparator<TokyoArtBeatEvent> descending() {
        return Collections.reverseOrder(ascending());
    }
}

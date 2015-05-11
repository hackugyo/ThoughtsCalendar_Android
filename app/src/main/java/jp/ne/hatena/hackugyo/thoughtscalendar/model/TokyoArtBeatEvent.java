package jp.ne.hatena.hackugyo.thoughtscalendar.model;

import jp.ne.hatena.hackugyo.thoughtscalendar.util.CalendarUtils;
import jp.ne.hatena.hackugyo.thoughtscalendar.util.StringUtils;

public class TokyoArtBeatEvent extends AttendingEvent  {

    

    public TokyoArtBeatEvent() {
        // no-arg constructor for deserializing
    }

    public TokyoArtBeatEvent(String eventId, String title, String dateFrom, String dateTo, String imageUrl, String description, String address, String location, String detailUrl) {
        this.eventId = eventId;
        this.ownersAccount = AUTHORITY_TOKYO_ART_BEAT;
        this.title = title;
        mCalendarFrom = CalendarUtils.parseGregorianDate(dateFrom, "-");
        if (mCalendarFrom != null) {
            this.begin = CalendarUtils.toDate(mCalendarFrom);
        }

        if (StringUtils.isEmpty(dateTo)) dateTo = dateFrom;
        mCalendarTo = CalendarUtils.parseGregorianDate(dateTo, "-");
        if (mCalendarTo != null) {
            this.end = CalendarUtils.toDate(mCalendarTo);
        }
        this.imageUrl = imageUrl;
        this.description = description;
        this.address = address;
        this.location = location;
        this.attending = false;
        this.detailUrl = detailUrl;
    }

    @Override
    public String getImageUrl() {
        if (imageUrl != null && imageUrl.contains("images/nopic")) return null;
        return imageUrl;
    }
    
}

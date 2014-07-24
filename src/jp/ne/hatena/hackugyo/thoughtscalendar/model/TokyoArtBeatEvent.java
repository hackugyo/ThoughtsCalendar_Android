package jp.ne.hatena.hackugyo.thoughtscalendar.model;

import jp.ne.hatena.hackugyo.thoughtscalendar.util.CalendarUtils;

public class TokyoArtBeatEvent extends AttendingEvent  {

    
    public TokyoArtBeatEvent() {
        // no-arg constructor for deserializing
    }

    public TokyoArtBeatEvent(String eventId, String title, String dateFrom, String imageUrl, String description, String address, String location, String detailUrl) {
        this.eventId = eventId;
        this.ownersAccount = AUTHORITY_TOKYO_ART_BEAT;
        this.title = title;
        mCalendar = CalendarUtils.parseGregorianDate(dateFrom, "-");
        if (mCalendar != null) {
            this.begin = CalendarUtils.toDate(mCalendar);
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

package jp.ne.hatena.hackugyo.thoughtscalendar.model;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jp.ne.hatena.hackugyo.thoughtscalendar.util.CalendarUtils;
import jp.ne.hatena.hackugyo.thoughtscalendar.util.LogUtils;
import jp.ne.hatena.hackugyo.thoughtscalendar.util.StringUtils;
import android.content.Context;

import com.google.common.base.Objects;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.table.DatabaseTable;

/**
 * 参加予定のイベント．<br>
 * 
 * @author kwatanabe
 * 
 */
@DatabaseTable(tableName = "t_event")
public class AttendingEvent {

    public static final String AUTHORITY_TOKYO_ART_BEAT = "tokyoartbeat";

    public static final String COLUMN_NAME_PRIMARY_KEY = "primaryKey";
    public static final String COLUMN_NAME_EVENTID = "eventId";
    public static final String COLUMN_NAME_OWNERS_ACCOUNT = "authority";
    public static final String COLUMN_NAME_TITLE = "title";
    public static final String COLUMN_NAME_LOCATION = "location";
    public static final String COLUMN_NAME_ADDRESS = "address";
    public static final String COLUMN_NAME_IMAGE_URL = "imageUrl";
    public static final String COLUMN_NAME_DESCRIPTION = "description";
    public static final String COLUMN_NAME_DETAIL_URL = "detailUrl";
    public static final String COLUMN_NAME_BEGIN = "begin";
    public static final String COLUMN_NAME_END = "end";
    public static final String COLUMN_NAME_ATTENDING = "attending";
    public static final String COLUMN_NAME_HASHTAG = "hashTag";
    public static final String COLUMN_NAME_CREATEDAT = "createdAt";

    /** 参加予定イベントキー */
    @DatabaseField(columnName = COLUMN_NAME_PRIMARY_KEY, generatedId = true)
    public Long primaryKey;
    /** イベントID */
    @DatabaseField(columnName = COLUMN_NAME_EVENTID)
    public String eventId;
    /** カレンダ管理者ID */
    @DatabaseField(columnName = COLUMN_NAME_OWNERS_ACCOUNT)
    public String ownersAccount;
    /** タイトル */
    @DatabaseField(columnName = COLUMN_NAME_TITLE)
    public String title;
    /** 開始日時 */
    @DatabaseField(columnName = COLUMN_NAME_BEGIN, dataType = DataType.DATE_LONG)
    public java.util.Date begin;
    /** 終了日時 */
    @DatabaseField(columnName = COLUMN_NAME_END, dataType = DataType.DATE_LONG)
    public java.util.Date end;
    /** 場所名称 */
    @DatabaseField(columnName = COLUMN_NAME_LOCATION)
    public String location;
    /** 場所住所 */
    @DatabaseField(columnName = COLUMN_NAME_ADDRESS)
    public String address;
    /** 詳細説明 */
    @DatabaseField(columnName = COLUMN_NAME_DESCRIPTION)
    public String description;
    /** 詳細URL */
    @DatabaseField(columnName = COLUMN_NAME_DETAIL_URL)
    public String detailUrl;
    /** 画像URL */
    @DatabaseField(columnName = COLUMN_NAME_IMAGE_URL)
    public String imageUrl;
    /** 参加有無 */
    @DatabaseField(columnName = COLUMN_NAME_ATTENDING, dataType = DataType.BOOLEAN)
    public boolean attending;
    /** ハッシュタグ */
    @DatabaseField(columnName = COLUMN_NAME_HASHTAG)
    public String hashTag;
    /** 作成日時 */
    @DatabaseField(columnName = COLUMN_NAME_CREATEDAT, dataType = DataType.DATE_LONG)
    private java.util.Date createdAt;

    protected Calendar mCalendarFrom, mCalendarTo;


    public AttendingEvent() {
        // no-arg constructor for deserializing
    }


    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other instanceof AttendingEvent) {
            AttendingEvent r = (AttendingEvent) other;
            return (StringUtils.isSame(eventId, r.eventId)) //
                    && (StringUtils.isSame(ownersAccount, r.ownersAccount));
        } else {
            return false;
        }
    }


    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)//
                .add(COLUMN_NAME_PRIMARY_KEY, primaryKey)//
                .add(COLUMN_NAME_EVENTID, eventId)//
                .add(COLUMN_NAME_OWNERS_ACCOUNT, ownersAccount)//
                .add(COLUMN_NAME_TITLE, title)//
                .add(COLUMN_NAME_BEGIN, begin)//
                .add(COLUMN_NAME_END, end)//
                .add(COLUMN_NAME_LOCATION, location)//
                .add(COLUMN_NAME_ADDRESS, address)//
                .add(COLUMN_NAME_DESCRIPTION, description)//
                .add(COLUMN_NAME_DETAIL_URL, detailUrl)//
                .add(COLUMN_NAME_IMAGE_URL, imageUrl)//
                .add(COLUMN_NAME_ATTENDING, attending)//
                .add(COLUMN_NAME_HASHTAG, hashTag)//
                .add(COLUMN_NAME_CREATEDAT, createdAt)//
                .toString();
    }

    /***********************************************
     * getter *
     ***********************************************/
    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public String getAddress() {
        return address;
    }

    public String getEventId() {
        return eventId;
    }

    public boolean getAttending() {
        return attending;
    }
    public void setAttending(boolean attending) {
        this.attending  = attending;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public CharSequence getLocation() {
        return location;
    }

    public Calendar getDateFrom() {
        if (mCalendarFrom == null) {
            mCalendarFrom = CalendarUtils.getInstance(this.begin);
        }
        return mCalendarFrom;
    }


    public Calendar getDateTo() {
        if (mCalendarTo == null) {
            if (end == null)  end = begin;
            mCalendarTo = CalendarUtils.getInstance(this.end);
        }
        return mCalendarTo;
    }

    /***********************************************
     * DB *
     ***********************************************/

    public void save(Context context) {
        DatabaseHelper helper = DatabaseHelper.getHelper(context);
        try {
            Dao<AttendingEvent, Integer> dao = helper.getDao(AttendingEvent.class);
            // 同一のものを探す
            QueryBuilder<AttendingEvent, Integer> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(COLUMN_NAME_EVENTID, eventId).and().eq(COLUMN_NAME_OWNERS_ACCOUNT, ownersAccount);
            List<AttendingEvent> result = queryBuilder.query();
            if (result != null && !result.isEmpty()) {
                this.primaryKey = result.get(0).primaryKey;
            }
            dao.createOrUpdate(this);
        } catch (SQLException e) {
            LogUtils.e("cannot find.", e);
        } finally {
            DatabaseHelper.releaseHelper();
        }
    }

    public static AttendingEvent findEvent(Context context, String eventId, String ownersAccount) {
        DatabaseHelper helper = DatabaseHelper.getHelper(context);
        List<AttendingEvent> results;
        try {
            Dao<AttendingEvent, Integer> dao = helper.getDao(AttendingEvent.class);
            QueryBuilder<AttendingEvent, Integer> queryBuilder = dao.queryBuilder();
            Where<AttendingEvent, Integer> where = queryBuilder.where();
            where.eq(COLUMN_NAME_EVENTID, eventId).and().eq(COLUMN_NAME_OWNERS_ACCOUNT, ownersAccount);
            queryBuilder.orderBy(COLUMN_NAME_BEGIN, true);
            results = queryBuilder.query();
        } catch (SQLException e) {
            LogUtils.e("cannot find.", e);
            return null;
        } finally {
            DatabaseHelper.releaseHelper();
        }
        if (results == null || results.isEmpty()) return null;
        return results.get(0);
    }

    /**
     * 
     * @param context
     * @param ownersAccount
     * @return 指定したカレンダの参加予定イベントの配列（0件の場合は空） or null
     */
    public static List<AttendingEvent> findEvents(Context context, String ownersAccount) {
        DatabaseHelper helper = DatabaseHelper.getHelper(context);
        List<AttendingEvent> results = null;
        try {
            Dao<AttendingEvent, Integer> dao = helper.getDao(AttendingEvent.class);
            QueryBuilder<AttendingEvent, Integer> queryBuilder = dao.queryBuilder();
            Where<AttendingEvent, Integer> where = queryBuilder.where();
            where.eq(COLUMN_NAME_OWNERS_ACCOUNT, ownersAccount);
            queryBuilder.orderBy(COLUMN_NAME_BEGIN, true);
            results = queryBuilder.query();
        } catch (SQLException e) {
            LogUtils.e("cannot find.", e);
            return null;
        } finally {
            DatabaseHelper.releaseHelper();
        }
        return results;
    }

    public static List<AttendingEvent> findAll(Context context) {

        DatabaseHelper helper = DatabaseHelper.getHelper(context);
        List<AttendingEvent> results;
        try {
            Dao<AttendingEvent, Integer> dao = helper.getDao(AttendingEvent.class);
            QueryBuilder<AttendingEvent, Integer> queryBuilder = dao.queryBuilder();
            queryBuilder.orderBy(COLUMN_NAME_BEGIN, true);
            results = queryBuilder.query();
        } catch (SQLException e) {
            LogUtils.e("cannot find.", e);
            return null;
        } finally {
            DatabaseHelper.releaseHelper();
        }
        if (results == null || results.isEmpty()) return null;
        return results;
    }


    /***********************************************
     * Comparable *
     **********************************************/

    public static Comparator<AttendingEvent> ascending() {
        return new Comparator<AttendingEvent>() {
            @Override
            public int compare(AttendingEvent lhs, AttendingEvent rhs) {
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

    public static Comparator<AttendingEvent> descending() {
        return Collections.reverseOrder(ascending());
    }
}

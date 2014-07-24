package jp.ne.hatena.hackugyo.thoughtscalendar.model;

import java.util.HashMap;

import jp.ne.hatena.hackugyo.thoughtscalendar.CustomApplication;
import jp.ne.hatena.hackugyo.thoughtscalendar.util.AppUtils;
import android.content.Context;

import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

public class AttendStatus {

    private static final String SNAPPYDB_KEY_IS_LOGGED_IN = "SNAPPYEDB_KEY_ALARM_IS_ENABLE_AttendStatus";
    /** LoginStatus cash. 0: logged out, 1: logged in, -1: undefined */
    private static HashMap<String, Boolean> sAttendStatusCash = new HashMap<String, Boolean>();

    public static boolean getAttendStatus(String eventId) {
        if (sAttendStatusCash.get(eventId) != null) return sAttendStatusCash.get(eventId);
        boolean result = false;
        DB db = null;
        Context context = CustomApplication.getAppContext();
        try {
            db = DBFactory.open(context, SNAPPYDB_KEY_IS_LOGGED_IN);
            if (db.exists(eventId)) {
                result = db.getBoolean(eventId);
            }
            db.close();
        } catch (SnappydbException e) {
            if (AppUtils.isDebuggable()) e.printStackTrace();
        }
        sAttendStatusCash.put(eventId, result);
        return result;
    }

    @Deprecated
    public static void setAttendStatus(String eventId, Boolean isAttend) {
        if (sAttendStatusCash.containsKey(eventId) && sAttendStatusCash.get(eventId) == isAttend) return;
        DB db = null;
        Context context = CustomApplication.getAppContext();
        try {
            db = DBFactory.open(context, SNAPPYDB_KEY_IS_LOGGED_IN);
            db.putBoolean(eventId, isAttend);
            db.close();
        } catch (SnappydbException e) {
            if (AppUtils.isDebuggable()) e.printStackTrace();
        }
        sAttendStatusCash.put(eventId, isAttend);
    }
}

package jp.ne.hatena.hackugyo.thoughtscalendar.ui.fragment;

import java.util.ArrayList;

import jp.ne.hatena.hackugyo.thoughtscalendar.CustomApplication;
import jp.ne.hatena.hackugyo.thoughtscalendar.R;
import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Instances;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;

public class PlaceHolderFragmentHelper {

    public static final ArrayList<String> sCalendarOwners = CustomApplication.getStringArrayById(R.array.list_calendar_owners);

    private PlaceHolderFragmentHelper() {

    }

    @SuppressLint("NewApi")
    static CursorLoader getCursorLoader(Fragment fragment, int begin, int end) {

        Uri content_by_day_uri;
        String[] instance_projection;
        String sort_order;
        String selection;
        String[] selectionArgs = new String[] { sCalendarOwners.get(fragment.getArguments().getInt(PlaceholderFragment.ARG_SECTION_NUMBER, 1) - 1) };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            content_by_day_uri = CalendarContract.Instances.CONTENT_BY_DAY_URI;
            instance_projection = new String[] { Instances._ID, Instances.EVENT_ID, Instances.BEGIN, Instances.END, Instances.TITLE, Instances.EVENT_LOCATION, Instances.DESCRIPTION, Instances.OWNER_ACCOUNT };
            sort_order = Instances.BEGIN + " ASC, " + Instances.END + " DESC, " + Instances.TITLE + " ASC";
            selection = "((" + Calendars.OWNER_ACCOUNT + " = ?))";
        } else {
            final String authority = "com.android.calendar";
            content_by_day_uri = Uri.parse("content://" + authority + "/instances/whenbyday");
            instance_projection = new String[] { "_id", "event_id", "begin", "end", "title", "eventLocation", "description", "ownerAccount" };
            sort_order = "begin ASC, end DESC, title ASC";
            selection = "((" + "ownerAccount" + " = ?))";
        }
        Uri baseUri = buildQueryUri(begin, end, content_by_day_uri);
        return new CursorLoader(fragment.getActivity(), baseUri, instance_projection, selection, selectionArgs, sort_order);
    }

    private static Uri buildQueryUri(int start, int end, Uri content_by_day_uri) {
        StringBuilder path = new StringBuilder();
        path.append(start);
        path.append('/');
        path.append(end);
        Uri uri = Uri.withAppendedPath(content_by_day_uri, path.toString());
        return uri;
    }

    @SuppressLint("InlinedApi")
    static String[] getBindFrom() {
        String[] from;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            from = new String[] { Instances.TITLE, Instances.BEGIN, Instances.EVENT_LOCATION, Instances.DESCRIPTION, Instances.EVENT_ID };
        } else {
            from = new String[] { "title", "begin", "eventLocation", "description", "event_id" };
        }
        return from;
    }
}

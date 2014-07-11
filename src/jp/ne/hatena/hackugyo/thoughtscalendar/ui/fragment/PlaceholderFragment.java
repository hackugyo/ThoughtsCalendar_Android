package jp.ne.hatena.hackugyo.thoughtscalendar.ui.fragment;

import java.util.Calendar;

import jp.ne.hatena.hackugyo.thoughtscalendar.R;
import jp.ne.hatena.hackugyo.thoughtscalendar.model.AttendStatus;
import jp.ne.hatena.hackugyo.thoughtscalendar.ui.AbsFragment;
import jp.ne.hatena.hackugyo.thoughtscalendar.ui.adapter.PlaceholderListAdapter;
import jp.ne.hatena.hackugyo.thoughtscalendar.util.CalendarUtils;
import jp.ne.hatena.hackugyo.thoughtscalendar.util.StringUtils;
import jp.ne.hatena.hackugyo.thoughtscalendar.util.TwitterUtils;
import jp.ne.hatena.hackugyo.thoughtscalendar.util.UrlUtils;
import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tjerkw.slideexpandable.library.AbstractSlideExpandableListAdapter.OnItemExpandCollapseListener;
import com.tjerkw.slideexpandable.library.ActionSlideExpandableListAdapter;
import com.tjerkw.slideexpandable.library.ActionSlideExpandableListView.OnActionClickListener;

/**
 * A placeholder fragment containing a simple view.
 * 
 * @see <a
 *      href="http://smartphone-zine.com/mobile/google-calendar-app-sourc.html">参考ページ</a>
 */
public class PlaceholderFragment extends AbsFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String TAG_DIALOG_FRAGMENT_SHOW_DETAIL = "TAG_DIALOG_FRAGMENT_SHOW_DETAIL";
    /**
     * The fragment argument representing the section number for this fragment.
     */
    static final String ARG_SECTION_NUMBER = "section_number";

    /**
     * Returns a new instance of this fragment for the given section number.
     */
    public static PlaceholderFragment newInstance(int sectionNumber) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    ListView mListView;
    private PlaceholderListAdapter mAdapter;
    private ActionSlideExpandableListAdapter mWrappedAdapter;
    private TextView mEmptyView;

    public PlaceholderFragment() {
    }

    /***********************************************
     * Life Cycle *
     **********************************************/

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View onCreateView = inflater.inflate(R.layout.fragment_placeholder, container, false);

        final int rowForDateTime = 2;
        mAdapter = new PlaceholderListAdapter(getActivitySafely(), null, false, //
                R.layout.list_header_placeholder, rowForDateTime);

        mWrappedAdapter = new ActionSlideExpandableListAdapter(//
                mAdapter,//
                R.id.list_row_placeholder_cell, //
                R.id.list_row_placeholder_expandable);
        getLoaderManager().initLoader(0, null, this);
        setupListView(onCreateView);
        setupEmptyView(onCreateView);

        return onCreateView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Loaderの廃棄
        getLoaderManager().destroyLoader(0);
    }

    /***********************************************
     * Loader *
     **********************************************/

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Calendar calendar = CalendarUtils.getInstance(true);
        int begin = Time.getJulianDay(calendar.getTimeInMillis(), 0);
        calendar.add(Calendar.YEAR, 1);
        int end = Time.getJulianDay(calendar.getTimeInMillis(), 0);

        return PlaceholderFragmentHelper.getCursorLoader(this, begin, end);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    /***********************************************
     * View *
     **********************************************/
    private void setupListView(View parentView) {
        mListView = (ListView) parentView.findViewById(android.R.id.list);
        mListView.setAdapter(mWrappedAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // ignored.
            }
        });
        mEmptyView = (TextView) parentView.findViewById(android.R.id.empty);
        mListView.setEmptyView(mEmptyView);
        // Expandable Cellのボタンにリスナを配置
        mWrappedAdapter.setItemActionListener(getExpandActionListener(), //
                R.id.list_row_placeholder_expandable_button_a, //
                R.id.list_row_placeholder_expandable_button_b,//
                R.id.list_row_placeholder_expandable_button_c,//
                R.id.list_row_placeholder_expandable_button_d);
        mWrappedAdapter.setItemExpandCollapseListener(getOnItemExpandCollapseListener());
        mWrappedAdapter.setAnimationDuration(100);
    }

    private void setupEmptyView(View parentView) {
        if (mEmptyView == null) {
            mEmptyView = (TextView) parentView.findViewById(android.R.id.empty);
        }
        mEmptyView.setOnClickListener(new OnClickListener() {

            @SuppressLint("NewApi")
            @Override
            public void onClick(View v) {
                String calendarId = PlaceholderFragmentHelper.sCalendarOwners.get(getArguments().getInt(PlaceholderFragment.ARG_SECTION_NUMBER, 1) - 1);

                String urlString = StringUtils.build("https://www.google.com/calendar/render?cid=",//
                        calendarId);
                launchExternalBrowser(urlString);
            }
        });
    }

    /***********************************************
     * OnClick Listeners *
     ***********************************************/

    private OnActionClickListener getExpandActionListener() {
        return new OnActionClickListener() {

            @Override
            public void onClick(View itemView, View clickedView, int position) {
                if (mAdapter.getItemViewType(position) == PlaceholderListAdapter.TYPE_HEADER) {
                    return;
                }
                final Cursor cursor = mAdapter.getCursor();
                cursor.moveToPosition(mAdapter.getCursorPosition(position));

                // _id,event_id,begin,end,title,eventLocation,description,ownerAccount

                final String eventId = cursor.getString(1);
                final CharSequence title = cursor.getString(4);
                final String location = cursor.getString(5);
                final String detail = cursor.getString(6);

                final String whenMillis = cursor.getString(2);
                Time time = new Time();
                time.set(Long.parseLong(whenMillis));
                CharSequence when = CalendarUtils.getDateString(CalendarUtils.getInstance(time.toMillis(false)));
                when = when.subSequence(0, Math.min(when.length(), 10));

                final boolean isUrl = UrlUtils.isValidUrl(detail);
                final int clickedViewId = clickedView.getId();
                String hashTag = PlaceholderFragmentHelper.createHashTag(title, when);
                switch (clickedViewId) {
                    case R.id.list_row_placeholder_expandable_button_a:
                        TwitterUtils.searchByHashTag(getActivitySafely(), hashTag);
                        break;
                    case R.id.list_row_placeholder_expandable_button_b:
                        boolean willAttendCurrently = AttendStatus.getAttendStatus(eventId);
                        if (!willAttendCurrently) {
                            showSingleToast("Twitterで参加表明しましょう！", Toast.LENGTH_SHORT);
                            TwitterUtils.sendText(getActivitySafely(),//
                                    StringUtils.build(//
                                            title, "(", when, "開催)に参加します！ #", hashTag)//
                                    );
                        }
                        AttendStatus.setAttendStatus(eventId, !willAttendCurrently);
                        mAdapter.notifyDataSetChanged();
                        break;
                    case R.id.list_row_placeholder_expandable_button_c:
                        if (isUrl) launchExternalBrowser(detail);
                        break;
                    case R.id.list_row_placeholder_expandable_button_d:
                        TwitterUtils.sendText(getActivitySafely(),//
                                StringUtils.build(" #", hashTag)//
                                );
                        break;
                    default:
                        break;
                }

            }
        };
    }

    private OnItemExpandCollapseListener getOnItemExpandCollapseListener() {
        return new OnItemExpandCollapseListener() {

            @Override
            public void onExpand(View itemView, int position) {
                if (mAdapter.getItemViewType(position) == PlaceholderListAdapter.TYPE_HEADER) {
                    return;
                }
                final Cursor cursor = mAdapter.getCursor();
                cursor.moveToPosition(mAdapter.getCursorPosition(position));
                final String location = cursor.getString(5);
                String alertMessage = StringUtils.build(//
                        "このイベントは、", //
                        (StringUtils.isEmpty(location) ? "開催場所不明です。" : location),//
                        (StringUtils.isEmpty(location) ? "" : "で開催されます。\n")//
                        );
                showSingleToast(alertMessage, Toast.LENGTH_LONG);
            }

            @Override
            public void onCollapse(View itemView, int position) {
                // nothing to do.
            }
        };
    }

}
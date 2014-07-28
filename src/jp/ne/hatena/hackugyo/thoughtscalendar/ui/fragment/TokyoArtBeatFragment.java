package jp.ne.hatena.hackugyo.thoughtscalendar.ui.fragment;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import jp.ne.hatena.hackugyo.thoughtscalendar.R;
import jp.ne.hatena.hackugyo.thoughtscalendar.model.AttendingEvent;
import jp.ne.hatena.hackugyo.thoughtscalendar.model.TokyoArtBeatEvent;
import jp.ne.hatena.hackugyo.thoughtscalendar.ui.adapter.TokyoArtBeatAdapter;
import jp.ne.hatena.hackugyo.thoughtscalendar.util.ArrayUtils;
import jp.ne.hatena.hackugyo.thoughtscalendar.util.CalendarUtils;
import jp.ne.hatena.hackugyo.thoughtscalendar.util.LogUtils;
import jp.ne.hatena.hackugyo.thoughtscalendar.util.StringUtils;
import jp.ne.hatena.hackugyo.thoughtscalendar.util.TwitterUtils;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.InputStreamRequest;
import com.tjerkw.slideexpandable.library.ActionSlideExpandableListAdapter;
import com.tjerkw.slideexpandable.library.ActionSlideExpandableListView.OnActionClickListener;

public class TokyoArtBeatFragment extends AbsApiFragment<InputStream> {
    @SuppressWarnings("unused")
    private final TokyoArtBeatFragment self = this;
    ListView mListView;
    TextView mEmptyView;
    private TokyoArtBeatAdapter mAdapter;
    private ActionSlideExpandableListAdapter mWrappedAdapter;

    /**
     * Returns a new instance of this fragment for the given section number.
     */
    public static TokyoArtBeatFragment newInstance() {
        TokyoArtBeatFragment fragment = new TokyoArtBeatFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public TokyoArtBeatFragment() {

    }

    /***********************************************
     * Life Cycle *
     **********************************************/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View onCreateView = inflater.inflate(R.layout.fragment_tokyoartbeat_list, container, false);
        mAdapter = new TokyoArtBeatAdapter(getActivitySafely(), new ArrayList<AttendingEvent>());
        mWrappedAdapter = new ActionSlideExpandableListAdapter(//
                mAdapter,//
                R.id.list_row_placeholder_cell, //
                R.id.list_row_placeholder_expandable);
        setupListView(onCreateView);
        getAPIAsync("http://www.tokyoartbeat.com/list/event_comingsoon.ja.xml", null);

        return onCreateView;
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
        mEmptyView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                launchExternalBrowser("http://www.tokyoartbeat.com");
            }
        });
        mListView.setEmptyView(mEmptyView);

        LayoutInflater inflater = LayoutInflater.from(parentView.getContext());
        TextView footer = (TextView) inflater.inflate(R.layout.list_footer_tokyoartbeat, null);
        footer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                launchExternalBrowser("http://www.tokyoartbeat.com");
            }
        });
        mListView.addFooterView(footer);

        // Expandable Cellのボタンにリスナを配置
        mWrappedAdapter.setItemActionListener(getExpandActionListener(), //
                android.R.id.text2,//
                R.id.list_row_placeholder_location_icon,
                R.id.list_row_placeholder_expandable_button_a, //
                R.id.list_row_placeholder_expandable_button_b,//
                R.id.list_row_placeholder_expandable_button_c,//
                R.id.list_row_placeholder_expandable_button_d);
        mWrappedAdapter.setAnimationDuration(100);
    }

    /***********************************************
     * OnClick Listeners *
     ***********************************************/

    private OnActionClickListener getExpandActionListener() {
        return new OnActionClickListener() {

            @Override
            public void onClick(View itemView, View clickedView, int position) {
                AttendingEvent event = (AttendingEvent) mAdapter.getItem(position);

                String title = event.getTitle();
                CharSequence when = CalendarUtils.getDateString(event.getDateFrom());
                when = when.subSequence(0, Math.min(when.length(), 10));
                String hashTag = PlaceholderFragmentHelper.createHashTag(title, when);

                final int clickedViewId = clickedView.getId();
                switch (clickedViewId) {
                    case R.id.list_row_placeholder_expandable_button_a:
                        TwitterUtils.searchByHashTag(getActivitySafely(), hashTag);
                        break;
                    case R.id.list_row_placeholder_expandable_button_b:
                        boolean willAttendCurrently = event.getAttending();
                        if (!willAttendCurrently) {
                            showSingleToast("Twitterで参加表明しましょう！", Toast.LENGTH_SHORT);
                            TwitterUtils.sendText(getActivitySafely(),//
                                    StringUtils.build(//
                                            title, "(", when, "〜開催)に興味あり！ ",//
                                            StringUtils.valueOf(event.getDetailUrl()),//
                                            " #", hashTag)//
                                    );
                        }
                        event.setAttending(!willAttendCurrently);
                        TokyoArtBeatFragmentHelper.saveToAttend(getActivity(), event, hashTag, AttendingEvent.AUTHORITY_TOKYO_ART_BEAT);
                        mAdapter.notifyDataSetChanged();
                        break;
                    case R.id.list_row_placeholder_expandable_button_c:
                        final String url = event.getDetailUrl();
                        LogUtils.i("  " + url);
                        if (StringUtils.isPresent(url)) launchExternalBrowser(url);
                        break;
                    case R.id.list_row_placeholder_expandable_button_d:
                        TwitterUtils.sendText(getActivitySafely(), StringUtils.build(" #", hashTag));
                        break;

                    case R.id.list_row_placeholder_location_icon: //
                        launchMapByLocation(event.getAddress(), event.getLocation());
                        break;
                    case android.R.id.text2:// 地図表示
                        launchMapByLocation(event.getAddress(), event.getLocation());
                        break;
                    default:
                        break;
                }
            }
        };
    }
    
    
    private void launchMapByLocation(String address, CharSequence location) {
        String alertMessage = StringUtils.build(//
                "このイベントは、", //
                (StringUtils.isEmpty(location) ? "開催場所不明です。" : location),//
                (StringUtils.isEmpty(location) ? "" : "で開催されます。\n")//
                );
        showSingleToast(alertMessage, Toast.LENGTH_LONG);
        if( StringUtils.isPresent(address))  {
            launchMap(address);
        } else  if( StringUtils.isPresent(location)) {
            launchMap(location.toString());
        }
    }

    /***********************************************
     * Volley *
     **********************************************/
    @Override
    Request<InputStream> createRequest(int method, String apiUrl, Listener<InputStream> listener, ErrorListener errorListener) {
        InputStreamRequest request = new InputStreamRequest(method, //
                apiUrl, //
                listener, errorListener);
        return request;
    }

    @Override
    public void onResponse(InputStream response) {
        ArrayList<AttendingEvent> data = TokyoArtBeatFragmentHelper.parseXml(response);
        try {
            response.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        onParseXml(data);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        if (mEmptyView != null) {
            mEmptyView.setText(R.string.tokyoArtBeat_nothing);
        }
    }

    private void onParseXml(ArrayList<AttendingEvent> data) {
        // DBに保存済みのイベントを追加する
        List<AttendingEvent> findEvents = AttendingEvent.findEvents(getActivitySafely(), AttendingEvent.AUTHORITY_TOKYO_ART_BEAT);
        if (findEvents != null) {
            Iterator<AttendingEvent> eventIterator = data.iterator();
            while (eventIterator.hasNext()) {
                AttendingEvent event = eventIterator.next();
                if (findEvents.contains(event)) {
                    int indexOf = findEvents.indexOf(event);
                    event.setAttending(findEvents.get(indexOf).attending);
                    findEvents.remove(event);
                }
            }
            data = new ArrayList<AttendingEvent>(ArrayUtils.concatList(findEvents, data));
        }
        
        Collections.sort(data, TokyoArtBeatEvent.ascending());
        
        mAdapter.setItems(data);
        mAdapter.notifyDataSetChanged();
        if (data == null || data.isEmpty()) {
            if (mEmptyView != null) {
                mEmptyView.setText(R.string.tokyoArtBeat_nothing);
            }
        }
    }
}

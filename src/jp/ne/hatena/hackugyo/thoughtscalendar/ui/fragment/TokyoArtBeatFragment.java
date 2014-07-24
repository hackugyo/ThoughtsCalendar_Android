package jp.ne.hatena.hackugyo.thoughtscalendar.ui.fragment;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

import jp.ne.hatena.hackugyo.thoughtscalendar.R;
import jp.ne.hatena.hackugyo.thoughtscalendar.model.TokyoArtBeatEvent;
import jp.ne.hatena.hackugyo.thoughtscalendar.ui.adapter.TokyoArtBeatAdapter;
import jp.ne.hatena.hackugyo.thoughtscalendar.util.LogUtils;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.InputStreamRequest;
import com.tjerkw.slideexpandable.library.AbstractSlideExpandableListAdapter.OnItemExpandCollapseListener;
import com.tjerkw.slideexpandable.library.ActionSlideExpandableListAdapter;
import com.tjerkw.slideexpandable.library.ActionSlideExpandableListView.OnActionClickListener;

public class TokyoArtBeatFragment extends AbsApiFragment<InputStream> {
    @SuppressWarnings("unused")
    private final TokyoArtBeatFragment self = this;
    ListView mListView;
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
        View onCreateView = inflater.inflate(R.layout.fragment_placeholder, container, false);
        mAdapter = new TokyoArtBeatAdapter(getActivitySafely(), new ArrayList<TokyoArtBeatEvent>());
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
        mListView.setEmptyView((TextView) parentView.findViewById(android.R.id.empty));

        // Expandable Cellのボタンにリスナを配置
        mWrappedAdapter.setItemActionListener(getExpandActionListener(), //
                android.R.id.text2,//
                R.id.list_row_placeholder_expandable_button_a, //
                R.id.list_row_placeholder_expandable_button_b,//
                R.id.list_row_placeholder_expandable_button_c,//
                R.id.list_row_placeholder_expandable_button_d);
        mWrappedAdapter.setItemExpandCollapseListener(getOnItemExpandCollapseListener());
        mWrappedAdapter.setAnimationDuration(100);
    }

    /***********************************************
     * OnClick Listeners *
     ***********************************************/

    private OnActionClickListener getExpandActionListener() {
        return new OnActionClickListener() {

            @Override
            public void onClick(View itemView, View clickedView, int position) {
                TokyoArtBeatEvent item = (TokyoArtBeatEvent) mAdapter.getItem(position);
                LogUtils.i("きただよ  " + item.getAddress());
            }
        };
    }

    private OnItemExpandCollapseListener getOnItemExpandCollapseListener() {
        return new OnItemExpandCollapseListener() {

            @Override
            public void onExpand(View itemView, int position) {
            }

            @Override
            public void onCollapse(View itemView, int position) {
            }
        };
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
        ArrayList<TokyoArtBeatEvent> data = TokyoArtBeatFragmentHelper.parseXml(response);
        try {
            response.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        onParseXml(data);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
    }

    private void onParseXml(ArrayList<TokyoArtBeatEvent> data) {
        Collections.sort(data, TokyoArtBeatEvent.ascending());
        mAdapter.setItems(data);
        mAdapter.notifyDataSetChanged();
    }
}

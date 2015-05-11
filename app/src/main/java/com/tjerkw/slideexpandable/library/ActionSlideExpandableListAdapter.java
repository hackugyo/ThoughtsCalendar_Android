package com.tjerkw.slideexpandable.library;

import jp.ne.hatena.hackugyo.thoughtscalendar.R;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import com.tjerkw.slideexpandable.library.ActionSlideExpandableListView.OnActionClickListener;

public class ActionSlideExpandableListAdapter extends SlideExpandableListAdapter {
    @SuppressWarnings("unused")
    private final ActionSlideExpandableListAdapter self = this;
    private OnActionClickListener listener;
    private int[] buttonIds;

    public ActionSlideExpandableListAdapter(ListAdapter wrapped, int toggle_button_id, int expandable_view_id) {
        super(wrapped, toggle_button_id, expandable_view_id);
    }

    public ActionSlideExpandableListAdapter(ListAdapter wrapped) {
        this(wrapped, R.id.expandable_toggle_button, R.id.expandable);
    }

    public void setItemActionListener(OnActionClickListener listener, int... buttonIds) {
        this.listener = listener;
        this.buttonIds = buttonIds;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        final View listView = super.getView(position, view, viewGroup);
        // add the action listeners
        if (buttonIds != null && listView != null) {
            for (int id : buttonIds) {
                View buttonView = listView.findViewById(id);
                if (buttonView != null) {
                    buttonView.findViewById(id).setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (listener != null) {
                                listener.onClick(listView, view, position);
                            }
                        }
                    });
                }
            }
        }
        return listView;
    }
}

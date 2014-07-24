package jp.ne.hatena.hackugyo.thoughtscalendar.ui.adapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashMap;

import jp.ne.hatena.hackugyo.thoughtscalendar.CustomApplication;
import jp.ne.hatena.hackugyo.thoughtscalendar.R;
import jp.ne.hatena.hackugyo.thoughtscalendar.model.AttendingEvent;
import jp.ne.hatena.hackugyo.thoughtscalendar.util.CalendarUtils;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.LruImageCache;
import com.android.volley.toolbox.NetworkImageView;

public class TokyoArtBeatAdapter extends BaseAdapter {
    @SuppressWarnings("unused")
    private final TokyoArtBeatAdapter self = this;

    private static final int TYPE_NORMAL = 1;
    public static final int TYPE_HEADER = 0;

    ArrayList<AttendingEvent> mPeriods = new ArrayList<AttendingEvent>();
    private LayoutInflater mInflater;

    private Context mContext;

    private ImageLoader mImageLoader;
    private LinkedHashMap<Integer, Calendar> mSectionsIndexer;

    public TokyoArtBeatAdapter(Context context, ArrayList<AttendingEvent> items) {
        super();
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mPeriods = items;
        mSectionsIndexer = calculateSectionHeaders(mPeriods);
        mImageLoader = new ImageLoader(CustomApplication.getQueue(), new LruImageCache());
    }

    @Override
    public int getCount() {
        return mPeriods.size() + mSectionsIndexer.size();
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) == TYPE_NORMAL;
    }

    @Override
    public Object getItem(int position) {
        if (getItemViewType(position) == TYPE_NORMAL) {
            return mPeriods.get(getItemPositionForPosition(position));
        }
        return mSectionsIndexer.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getPositionForSection(position)) {
            return TYPE_NORMAL;
        }
        return TYPE_HEADER;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int viewType = getItemViewType(position);
        if (viewType == TYPE_NORMAL) {
            AttendingEvent event = (AttendingEvent) getItem(position);
            convertView = setupConvertView(event, mInflater, position, convertView, parent);
        } else {
            Calendar calendar = (Calendar) getItem(position);

            HeaderViewHolder holder = null;
            if (convertView == null) {
                holder = new HeaderViewHolder();
                convertView = mInflater.inflate(R.layout.list_header_placeholder, parent, false);
                holder.textView = (TextView) convertView.findViewById(R.id.list_header_placeholder_date);
                holder.countView = (TextView) convertView.findViewById(R.id.list_header_placeholder_items);
                convertView.setTag(holder);
            } else {
                holder = (HeaderViewHolder) convertView.getTag();
            }
            final String customFormat = getGroupCustomFormat(calendar);
            holder.textView.setText(customFormat);
            final int currentSection = getSectionForPosition(position);
            final int nextSection = currentSection + 1;
            final int count = getPositionForThisSection(nextSection) - position - 1;
            // 日本語設定の場合prulalsは役に立たない．
            // String string = convertView.getResources().getQuantityString(R.plurals.list_header_placeholder_events, count, count)
            String string = convertView.getResources().getString((count != 1 ? R.string.list_header_placeholder_events :R.string.list_header_placeholder_event) , count);
            holder.countView.setText(string);
        }
        return convertView;
    }

    public void setItems(ArrayList<AttendingEvent> items) {
        mPeriods = items;
        mSectionsIndexer = calculateSectionHeaders(mPeriods);
    }

    private View setupConvertView(AttendingEvent event, LayoutInflater inflater, int position, View convertView, ViewGroup parent) {
        if (inflater == null) return null;

        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.list_row_placeholder, parent, false);
            {
                holder.title = (TextView) convertView.findViewById(android.R.id.text1);
                holder.location = (TextView) convertView.findViewById(android.R.id.text2);
                holder.begin = (TextView) convertView.findViewById(R.id.list_row_placeholder_datetime);
                //holder.url = (TextView) convertView.findViewById(R.id.url);
                holder.background = (NetworkImageView) convertView.findViewById(R.id.list_row_network_image_view);
                holder.background.setDefaultImageResId(R.color.image_mask);
                holder.attendStatus = convertView.findViewById(R.id.list_row_placeholder_flag);
                holder.attendStatusExpand = convertView.findViewById(R.id.list_row_placeholder_flag_expandable);
                holder.favoriteButton = (ImageButton) convertView.findViewById(R.id.list_row_placeholder_expandable_button_b);
                holder.showDetailButton = convertView.findViewById(R.id.list_row_placeholder_detail);
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (event != null) {
            holder.title.setText(event.getTitle());
            holder.begin.setText(CalendarUtils.getDateStringWithoutYear(event.getDateFrom(), "/") + " 〜 ");

            holder.location.setText(event.getLocation());

            final boolean willAttend = event.getAttending();
            holder.attendStatus.setBackgroundColor(//
                    inflater.getContext().getResources().getColor(//
                            willAttend ? R.color.attended_cell : android.R.color.transparent)//
                    );
            holder.attendStatusExpand.setBackgroundColor(//
                    inflater.getContext().getResources().getColor(//
                            willAttend ? R.color.attended_cell : android.R.color.transparent)//
                    );
            holder.favoriteButton.setImageResource(willAttend ? R.drawable.ic_favorite : R.drawable.ic_unfavorite);
            holder.background.setImageUrl(event.getImageUrl(), mImageLoader);

        }

        return convertView;
    }

    /****************************************
     * ViewHolder
     ****************************************/

    static class ViewHolder {
        TextView title;
        TextView begin;
        TextView location;
        TextView descrition;
        TextView eventId;
        public NetworkImageView background;
        public View attendStatus;
        public View attendStatusExpand;
        ImageButton favoriteButton;
        View showDetailButton;
    }
    
    static class HeaderViewHolder {
        public TextView countView;
        public TextView textView;
    }

    /****************************************
     * セクション関係の処理
     ****************************************/
    
    public int getPositionForThisSection(int section) {
        ArrayList<Integer> arrayList = new ArrayList<Integer>(mSectionsIndexer.keySet());
        Collections.sort(arrayList);
        if (arrayList.size() <= section) {
            return getCount();
        }
        return arrayList.get(section);
    }
    
    /**
     * セクションの開始positionを返します．
     */
    public int getPositionForSection(int section) {
        if (mSectionsIndexer.containsKey(section)) {
            return section + 1;
        }
        return section;
    }

    private LinkedHashMap<Integer, Calendar> calculateSectionHeaders(ArrayList<AttendingEvent> periods) {
        int i = 0;
        Calendar previous = null;
        int count = 0;

        mSectionsIndexer = new LinkedHashMap<Integer, Calendar>();
        for (AttendingEvent e : periods) {
            final Calendar group = e.getDateFrom();
            if (!CalendarUtils.isSameDateAs(previous, group)) {
                mSectionsIndexer.put(i + count, group);
                previous = group;
                count++;
            }
            i++;
        }
        return mSectionsIndexer;
    }

    public int getItemPositionForPosition(int position) {
        return position - getSectionForPosition(position);
    }

    /**
     * positionが入っているセクションを割り出します．
     */
    public int getSectionForPosition(int position) {
        int offset = 0;
        for (Integer key : mSectionsIndexer.keySet()) {
            if (position > key) {
                offset++;
            } else {
                break;
            }
        }
        return offset;
    }
    
    public String getGroupCustomFormat(Calendar calendar) {
        return CalendarUtils.getDateStringWithoutYear(calendar, "/");
    }
}

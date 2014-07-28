package jp.ne.hatena.hackugyo.thoughtscalendar.ui.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import jp.ne.hatena.hackugyo.thoughtscalendar.CustomApplication;
import jp.ne.hatena.hackugyo.thoughtscalendar.R;
import jp.ne.hatena.hackugyo.thoughtscalendar.model.AttendStatus;
import jp.ne.hatena.hackugyo.thoughtscalendar.model.AttendingEvent;
import jp.ne.hatena.hackugyo.thoughtscalendar.ui.fragment.PlaceholderFragmentHelper;
import jp.ne.hatena.hackugyo.thoughtscalendar.util.ArrayUtils;
import jp.ne.hatena.hackugyo.thoughtscalendar.util.CalendarUtils;
import jp.ne.hatena.hackugyo.thoughtscalendar.util.StringUtils;
import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.v4.widget.CursorAdapter;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.LruImageCache;
import com.android.volley.toolbox.NetworkImageView;

public class PlaceholderListAdapter extends CursorAdapter {
    @SuppressWarnings("unused")
    private final PlaceholderListAdapter self = this;

    private static final int TYPE_NORMAL = 1;
    public static final int TYPE_HEADER = 0;

    private LayoutInflater mInflater;

    /****************************************
     * Header
     ****************************************/
    private LinkedHashMap<Integer, String> sectionsIndexer;
    private int mHeaderLayoutId;
    private int mColumnForGrouping;
    private int mViewTypesCount = 1;

    public ArrayList<AttendingEvent> mAttendingEvents;
    private ImageLoader mImageLoader;

    static class HeaderViewHolder {
        public TextView textView;
        public TextView countView;
    }

    class ViewHolder {
        TextView title;
        TextView begin;
        TextView location;
        TextView descrition;
        TextView eventId;
        public NetworkImageView background;
        public View attendStatus;
        public View attendStatusExpand;
        ImageButton favoriteButton;
        ImageButton showDetailButton;
    }

    /****************************************
     * Constructor
     ****************************************/
    /**
     * @param context
     * @param c
     * @param autoRequery
     */
    public PlaceholderListAdapter(Context context, Cursor c, boolean autoRequery) {
        this(context, c, autoRequery, 0, 0, AttendingEvent.AUTHORITY_TOKYO_ART_BEAT);
    }

    public PlaceholderListAdapter(Context context, Cursor c, boolean autoRequery, //
            int headerLayoutId, int columnForGrouping, String authority) {
        super(context, c, autoRequery);

        mViewTypesCount = (headerLayoutId <= 0 ? 1 : 2);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        sectionsIndexer = new LinkedHashMap<Integer, String>();
        mHeaderLayoutId = headerLayoutId;
        mColumnForGrouping = columnForGrouping;
        if (c != null) {
            // 最初に，セクションがどこに収まるかを確認しておく．
            sectionsIndexer = calculateSectionHeaders();
            c.registerDataSetObserver(mMyDataSetObserver);
        }
        
        mAttendingEvents = ArrayUtils.asList(AttendingEvent.findEvents(context, authority));
        mImageLoader = new ImageLoader(CustomApplication.getQueue(), new LruImageCache());
    }

    /**
     * データセット変更を監視．
     */
    private DataSetObserver mMyDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            sectionsIndexer = calculateSectionHeaders();
        };

        @Override
        public void onInvalidated() {
            sectionsIndexer.clear();
        };
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Viewを再利用してデータをセットします
        ViewHolder holder = (ViewHolder) view.getTag();

        // Cursorからデータを取り出します
        final String title = cursor.getString(cursor.getColumnIndexOrThrow(PlaceholderFragmentHelper.Place.KEY_TITLE));
        final String begin = cursor.getString(cursor.getColumnIndexOrThrow(PlaceholderFragmentHelper.Place.KEY_BEGIN));
        final String location = cursor.getString(cursor.getColumnIndexOrThrow(PlaceholderFragmentHelper.Place.KEY_EVENTLOCATION));
        final String description = cursor.getString(cursor.getColumnIndexOrThrow(PlaceholderFragmentHelper.Place.KEY_DESCRIPTION));
        final String eventId = cursor.getString(cursor.getColumnIndexOrThrow(PlaceholderFragmentHelper.Place.KEY_EVENTID));
        final String detailUrl = cursor.getString(6);

        // 画面にセットします
        holder.title.setText(title);
        holder.location.setText(location);
        Time time = new Time();
        time.set(Long.parseLong(begin));
        holder.begin.setText(CalendarUtils.getTimeString(CalendarUtils.getInstance(time.toMillis(false))) + " 〜 ");

        final boolean willAttend = AttendStatus.getAttendStatus(eventId) || includeEventId(eventId, mAttendingEvents);
        holder.attendStatus.setBackgroundColor(//
                context.getResources().getColor(//
                        willAttend ? R.color.attended_cell : android.R.color.transparent)//
                );
        holder.attendStatusExpand.setBackgroundColor(//
                context.getResources().getColor(//
                        willAttend ? R.color.attended_cell : android.R.color.transparent)//
                );
        holder.favoriteButton.setImageResource(willAttend ? R.drawable.ic_favorite : R.drawable.ic_unfavorite);
        holder.showDetailButton.setImageResource(StringUtils.isEmpty(detailUrl) ? R.drawable.ic_tag_empty : R.drawable.ic_tag);
        holder.showDetailButton.setEnabled(StringUtils.isPresent(detailUrl));
        //holder.descrition.setText(description);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        // 新しくViewを作ります
        final View view = mInflater.inflate(R.layout.list_row_placeholder, null);

        ViewHolder holder = new ViewHolder();
        holder.title = (TextView) view.findViewById(android.R.id.text1);
        holder.location = (TextView) view.findViewById(android.R.id.text2);
        holder.begin = (TextView) view.findViewById(R.id.list_row_placeholder_datetime);
        holder.background = (NetworkImageView) view.findViewById(R.id.list_row_network_image_view);
        holder.background.setDefaultImageResId(R.color.image_mask);
        holder.attendStatus = view.findViewById(R.id.list_row_placeholder_flag);
        holder.attendStatusExpand = view.findViewById(R.id.list_row_placeholder_flag_expandable);
        holder.favoriteButton = (ImageButton) view.findViewById(R.id.list_row_placeholder_expandable_button_b);
        holder.showDetailButton = (ImageButton) view.findViewById(R.id.list_row_placeholder_expandable_button_c);

        view.setTag(holder);

        return view;
    }

    /****************************************
     * Cursor
     ****************************************/
    @Override
    public void changeCursor(Cursor cursor) {
        final Cursor old = swapCursor(cursor);

        if (old != null) {
            old.close();
        }
    }

    /**
     * <p>
     * swapCursor時にセクションヘッダを読み替える必要があるため実装．
     * </p>
     */
    @Override
    public Cursor swapCursor(Cursor newCursor) {
        if (getCursor() != null) {
            getCursor().unregisterDataSetObserver(mMyDataSetObserver);
        }

        final Cursor oldCursor = super.swapCursor(newCursor);
        sectionsIndexer = calculateSectionHeaders();
        if (newCursor != null) {
            newCursor.registerDataSetObserver(mMyDataSetObserver);
        }

        return oldCursor;
    }

    /****************************************
     * Header view
     ****************************************/
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int viewType = getItemViewType(position);

        if (mHeaderLayoutId == 0) {
            return super.getView(position, convertView, parent);
        }
        if (viewType == TYPE_NORMAL) {
            Cursor c = (Cursor) getItem(position);
            if (c == null) {
                return mInflater.inflate(mHeaderLayoutId, parent, false);
            }
            // 通常アイテムなので，positionだけずらす
            final int mapCursorPos = getCursorPosition(position);
            c.moveToPosition(mapCursorPos);
            return super.getView(mapCursorPos, convertView, parent);
        } else {
            HeaderViewHolder holder = null;
            if (convertView == null) {
                holder = new HeaderViewHolder();
                convertView = mInflater.inflate(mHeaderLayoutId, parent, false);
                holder.textView = (TextView) convertView.findViewById(R.id.list_header_placeholder_date);
                holder.countView = (TextView) convertView.findViewById(R.id.list_header_placeholder_items);
                convertView.setTag(holder);
            } else {
                holder = (HeaderViewHolder) convertView.getTag();
            }
            TextView sectionText = holder.textView;
            final String group = sectionsIndexer.get(position);
            final String customFormat = getGroupCustomFormat(group);
            sectionText.setText(customFormat == null ? group : customFormat);
            final int currentSection = getSectionForPosition(position);
            final int nextSection = currentSection + 1;
            final int count = getPositionForThisSection(nextSection) - position - 1;
            // 日本語設定の場合prulalsは役に立たない．
            // String string = convertView.getResources().getQuantityString(R.plurals.list_header_placeholder_events, count, count)
            String string = convertView.getResources().getString((count != 1 ? R.string.list_header_placeholder_events :R.string.list_header_placeholder_event) , count);
            holder.countView.setText(string);
            return convertView;
        }
    }

    private LinkedHashMap<Integer, String> calculateSectionHeaders() {
        int i = 0;
        String previous = "";
        int count = 0;
        final Cursor c = getCursor();
        sectionsIndexer.clear();
        if (c == null) {
            return sectionsIndexer;
        }
        c.moveToPosition(-1);
        while (c.moveToNext()) {
            final String group = getCustomGroup(c.getString(mColumnForGrouping));
            if (!previous.equals(group)) {
                sectionsIndexer.put(i + count, group);
                previous = group;
                count++;
            }
            i++;
        }
        return sectionsIndexer;
    }

    /**
     * <p>
     * This method serve as an intercepter before the sections are calculated so
     * you can transform some computer data into human readable, e.g. format a
     * unix timestamp, or a status.
     * </p>
     * 
     * <p>
     * By default this method returns the original data for the group column.
     * </p>
     * これの出力をもとにグルーピングを行いセクションを作る．
     * 
     * @param groupData
     * @return readable group data
     */
    protected String getCustomGroup(String groupData) {
        // ここで，受け取った時刻文字列を日付に変更する．

        Time time = new Time();
        time.set(Long.parseLong(groupData));
        final String dateString = CalendarUtils.getYearMonthDateString(CalendarUtils.getInstance(time.toMillis(false)), "/");
        // TODO 20140711 event数をとる方法を考える
        return dateString;
    }

    public String getGroupCustomFormat(Object obj) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getViewTypeCount() {
        return mViewTypesCount;
    }

    @Override
    public int getCount() {
        return super.getCount() + sectionsIndexer.size();
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) == TYPE_NORMAL;
    }

    @Override
    public Object getItem(int position) {
        if (getItemViewType(position) == TYPE_NORMAL) {
            return super.getItem(getCursorPosition(position));
        }
        return super.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        if (getItemViewType(position) == TYPE_NORMAL) {
            return super.getItemId(getCursorPosition(position));
        }
        return super.getItemId(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getPositionForSection(position)) {
            return TYPE_NORMAL;
        }
        return TYPE_HEADER;
    }
    
    public int getPositionForThisSection(int section) {
        ArrayList<Integer> arrayList = new ArrayList<Integer>(sectionsIndexer.keySet());
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
        if (sectionsIndexer.containsKey(section)) {
            return section + 1;
        }
        return section;
    }
    
    public int getItemPositionForPosition(int position) {
        return position - getSectionForPosition(position);
    }

    /**
     * positionが入っているセクションを割り出します．
     */
    public int getSectionForPosition(int position) {
        int offset = 0;
        for (Integer key : sectionsIndexer.keySet()) {
            if (position > key) {
                offset++;
            } else {
                break;
            }
        }
        return offset;
    }

    /**
     * 指定されたpositionがCursor（実データ）でいうとどこかを返します
     * 
     * @param position
     * @return cursor上のposition
     */
    public int getCursorPosition(int position) {
        return getItemPositionForPosition(position);
    }
    
    public boolean includeEventId(String eventId) {
        return includeEventId(eventId, mAttendingEvents);
    }
    
    private static boolean includeEventId(String eventId, List<AttendingEvent> attendingEvents) {
        for (AttendingEvent e : attendingEvents) {
            if (StringUtils.isSame(eventId, e.eventId) && e.attending) return true;
        }
        return false;
    }
}

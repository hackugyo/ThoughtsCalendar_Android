package jp.ne.hatena.hackugyo.thoughtscalendar.ui.adapter;

import java.util.ArrayList;

import jp.ne.hatena.hackugyo.thoughtscalendar.CustomApplication;
import jp.ne.hatena.hackugyo.thoughtscalendar.R;
import jp.ne.hatena.hackugyo.thoughtscalendar.model.TokyoArtBeatEvent;
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

    ArrayList<TokyoArtBeatEvent> mPeriods = new ArrayList<TokyoArtBeatEvent>();
    private LayoutInflater mInflater;

    private Context mContext;

    private ImageLoader mImageLoader;

    public TokyoArtBeatAdapter(Context context, ArrayList<TokyoArtBeatEvent> items) {
        super();
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mPeriods = items;
        mImageLoader = new ImageLoader(CustomApplication.getQueue(), new LruImageCache());
    }

    @Override
    public int getCount() {
        return mPeriods.size();
    }

    @Override
    public Object getItem(int position) {
        if (mPeriods.size() <= position) return null;
        return mPeriods.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TokyoArtBeatEvent event = (TokyoArtBeatEvent) getItem(position);
        convertView = setupConvertView(event, mInflater, position, convertView, parent);
        return convertView;
    }

    public void setItems(ArrayList<TokyoArtBeatEvent> items) {
        mPeriods = items;
    }

    private View setupConvertView(TokyoArtBeatEvent event, LayoutInflater inflater, int position, View convertView, ViewGroup parent) {
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

        if (event != null) {// TODO 20140716 びゅーをeventで埋める
            holder.title.setText(event.getTitle());
            holder.begin.setText(CalendarUtils.getDateStringWithoutYear(event.getDateFrom(), "/") + " 〜 ");

            holder.location.setText(event.getAddress());

            final boolean willAttend = false; // FakeIt 参加有無はDBからとる
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
}

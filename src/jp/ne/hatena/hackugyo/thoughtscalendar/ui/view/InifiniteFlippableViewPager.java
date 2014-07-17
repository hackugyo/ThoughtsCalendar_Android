package jp.ne.hatena.hackugyo.thoughtscalendar.ui.view;

import java.util.ArrayList;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 擬似的に循環ページングできるようにした{@link PagerAdapter}です．
 * @author kwatanabe
 *
 */
abstract public class InifiniteFlippableViewPager extends PagerAdapter implements AbsItemGettableViewPager.ItemGettable {
    @SuppressWarnings("unused")
    private final InifiniteFlippableViewPager self = this;
    private ArrayList<Object> mTopics;
    private LayoutInflater mInflater;
    private static final int MAX_POS = 1000000000; // 10億あるので，1秒に1ページずつ閲覧しても，片方の端に到達するまで15年ほどかかる
    private static final int START_POS =  MAX_POS / 2;
    
    /********************************************************************
     * コンストラクタ
     ********************************************************************/
    public InifiniteFlippableViewPager(Context context, ArrayList<Object> topics) {
        if (topics == null) throw new IllegalArgumentException("This arg is null topics. Pass an empty ArrayList insteadly.");
        mTopics = topics;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    
    /********************************************************************
     * {@link PagerAdapter} required
     ********************************************************************/
    @Override
    public int getCount() {
        // return mTopics == null ? 0 : mTopics.size();
        if (mTopics == null ) return 0;
        final int listSize = mTopics.size();
        if (listSize <= 1) return mTopics.size(); // 1個しかないときはFlipさせたくないので，全体数も1とする
        if (listSize > START_POS) return mTopics.size(); // 多すぎるときは，無限スクロールをあきらめる
        return MAX_POS;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }
    
    @Override
    public void destroyItem(View collection, int position, Object view) {
        ((ViewPager) collection).removeView((View) view);
        view = null;
    }
    
    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        
        Object topic = getItemAtPosition(position);
        
        View layout = createLayout(mInflater, collection, position, topic);
        ((ViewPager)collection).addView(layout,0);
        return layout;
    }
    
    abstract public View createLayout(LayoutInflater inflater, ViewGroup collection, int position, Object topic);

    /********************************************************************
     * 疑似無限スクロール用
     * {@link AbsItemGettableViewPager.ItemGettable}
     ********************************************************************/
    @Override
    public Object getItemAtPosition(int adapterPosition) {
        final int moveto = getItemPosition(adapterPosition);
        return mTopics.get(moveto);
    }
    
    public int getItemPosition(int adapterPosition) {
        return mod(adapterPosition - getStartingPos(), mTopics.size());
    }
    
    /**
     * 剰余演算子のかわり．
     * @param a
     * @param b
     * @return a (mod b)
     */
    private static int mod(int a, int b) {
        int result = a % b;
        if (result < 0) result = result + b;
        return result;
    }
    
    public int getStartingPos() {
        return 0;// (mTopics != null && mTopics.size() < START_POS) ? START_POS : 0;
    }
    
}

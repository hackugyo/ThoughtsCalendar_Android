package jp.ne.hatena.hackugyo.thoughtscalendar.ui.view;

import android.content.Context;
import android.support.v4.view.AnimatableViewPager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

abstract public class AbsItemGettableViewPager extends AnimatableViewPager {

    public AbsItemGettableViewPager(Context context) {
        super(context);
    }
    
    public AbsItemGettableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    

    /********************************************************************
     * メソッド
     ********************************************************************/
    /**
     * {@link #getItemAtPosition(int)}が使えるかどうか．<br>
     * 毎回キャストすると重いのでフラグで持ちます．
     */
    private boolean mIsItemGettable = false;
    
    @Override
    public void setAdapter(PagerAdapter adapter) {
        super.setAdapter(adapter);
        mIsItemGettable = (adapter instanceof ItemGettable);
    }
    
    /**
     * 指定位置のアイテムを取得します．<br>
     * {@link #getAdapter()}が{@link ItemGettable}でない場合は，{@link #getCurrentItem()}同様に，<br>
     * 位置だけを返します．
     * @param position
     * @return 指定位置のアイテム，または指定位置のインデックス
     */
    public Object getItemAtPosition(int position) {
        if (mIsItemGettable) return position;
        return getAdapterInternal().getItemAtPosition(position);
    }
    
    public Object getCurrentItemInstance() {
        return getItemAtPosition(getCurrentItem());
    }
    
    private ItemGettable getAdapterInternal() {
        return (ItemGettable) getAdapter();
    }
    
    /**
     * ArrayAdapter#getItem()のように，指定位置のアイテムを取得できるAdapterのインタフェースです．<br>
     * {@link PagerAdapter}にはgetメソッドがなく，{@link ViewPager#getCurrentItem()}は位置しか返してくれないので，<br>
     * 必要なら実装してください．
     * 
     * @author kwatanabe
     *
     */
    public interface ItemGettable {
        public Object getItemAtPosition(int position);
    }
}

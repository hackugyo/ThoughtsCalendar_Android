package jp.ne.hatena.hackugyo.thoughtscalendar.ui.view;

import android.content.Context;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.util.AttributeSet;


/**
 * 自動循環FlipができるViewPagerです．<br>
 * 循環間隔の初期値は{@link #DEFAULT_INTERVAL}msです．
 * アニメーション速度は，横の長さの距離 / {@link #DEFAULT_VEROCITY} * 4000msかけて1フリップします．
 * @author kwatanabe
 *
 */
public class AutoFlippableViewPager extends AbsItemGettableViewPager {
    
    public interface AutoFlippableViewPagerDelegate {
        public void onPositionChanged(AutoFlippableViewPager pager, int position);
    }
    
    public static final int DEFAULT_VEROCITY = 1;
    public static final int DEFAULT_INTERVAL = 3000;
    int mInterval = DEFAULT_INTERVAL;
    int mVelocity = DEFAULT_VEROCITY;
    private Handler mHandler;
    private Runnable mFlipRunnable;
    private AutoFlippableViewPagerDelegate mDelegate;

    /********************************************************************
     * コンストラクタ
     ********************************************************************/
    public AutoFlippableViewPager(Context context) {
        super(context);
        setup();
    }

    public AutoFlippableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    /********************************************************************
     * Flip設定
     ********************************************************************/
    public void startAutoFlip() {
        mHandler.postDelayed(mFlipRunnable, mInterval);
    }
    
    public void stopAutoFlip() {
        mHandler.removeCallbacks(mFlipRunnable);
    }
    
    /**
     * 自動Flipの間隔をmillisecで指定します．
     * @param interval
     */
    public void setAutoFlipInterval(int interval) {
        mInterval = interval;
    }
    
    private void showNextItem() {
        int targetPage = getCurrentItem() + 1 ;
        if (targetPage == getAdapter().getCount()) {
            setCurrentItem(0, false); // 末尾 -> 先頭だけはFlipしない
        } else {
            setCurrentItem(targetPage, true);
        }
    }
    
    public AutoFlippableViewPagerDelegate getmDelegate() {
        return mDelegate;
    }

    public void setmDelegate(AutoFlippableViewPagerDelegate mDelegate) {
        this.mDelegate = mDelegate;
    }
    
    /********************************************************************
     * 疑似無限Flip設定
     ********************************************************************/
    @Override
    public void setCurrentItem(int position) {
        super.setCurrentItem(position);
    }
    
    @Override
    public void setCurrentItem(int position, boolean smoothScroll) {
        super.setCurrentItem(position, smoothScroll);
    }

    @Override
    public Object getItemAtPosition(int position) {
        // 循環ページングさせている場合，Adapter内のpositionとアイテムの個数が一致しないので，
        // 取得方法をadapterに任せる．
        PagerAdapter adapter = getAdapter();
        if (adapter instanceof InfiniteFlippable) {
            return ((InfiniteFlippable) adapter).getItemAtPosition(position);
        } else {
            return super.getItemAtPosition(position);
        }
    }

    /********************************************************************
     * 表示ページ変更 delegate
     ********************************************************************/
    
    @Override
    protected void onPageScrolled(int position, float offset, int offsetPixels) {
        super.onPageScrolled(position, offset, offsetPixels);
        if (mDelegate != null) {
            mDelegate.onPositionChanged(this,position);
        }
    }
    
    /********************************************************************
     * private methods
     ********************************************************************/
    private void setup() {
        setAnimationVelocity(mVelocity);
        if (mHandler == null ) mHandler = new Handler();
        if (mFlipRunnable == null) {
            mFlipRunnable = new Runnable() {
                @Override
                public void run() {
                    showNextItem();
                    mHandler.removeCallbacks(mFlipRunnable);
                    mHandler.postDelayed(mFlipRunnable, mInterval);
                }
            };
        }
    }

    public interface InfiniteFlippable {
        public Object getItemAtPosition(int adapterPosition);
    }
}

package android.support.v4.view;

import java.lang.reflect.Field;

import jp.ne.hatena.hackugyo.thoughtscalendar.ui.view.ViewPagerAnimatable;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;


/**
 * {@link ViewPager}は，SDK 11未満のとき，{@link #setPageTransformer(boolean, PageTransformer)}を無視するので，<br>
 * 無視されないようにリフレクションを使ってoverrideします．<br>
 * @author kwatanabe
 *
 */
public class AnimatableViewPager extends ViewPager {
    private static final int DRAW_ORDER_DEFAULT = 0;
    private static final int DRAW_ORDER_FORWARD = 1;
    private static final int DRAW_ORDER_REVERSE = 2;
    
    /**
     * velocity the velocity associated with a fling, if applicable. (0 otherwise)
     */
    private int mAnimationVelocity = 0;
    
    /********************************************************************
     * コンストラクタ
     ********************************************************************/
    public AnimatableViewPager(Context context) {
        super(context);
    }

    public AnimatableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    /********************************************************************
     * {@link ViewPager}
     ********************************************************************/
    @Override
    public void setPageTransformer(boolean reverseDrawingOrder, PageTransformer transformer) {
        if (Build.VERSION.SDK_INT >= 11) {
            super.setPageTransformer(reverseDrawingOrder, transformer);
        } else if (transformer instanceof ViewPagerAnimatable) {
            boolean result = false;
            final boolean hasTransformer = transformer != null;
            final boolean needsPopulate = hasTransformer != (getPrivatePageTransformer() != null);
            result = setPrivatePageTransformer(transformer);
            if (!result) return;
            setChildrenDrawingOrderEnabledCompat(hasTransformer);
            if (hasTransformer) {
                result = setPrivateDrawingOrder(reverseDrawingOrder ? DRAW_ORDER_REVERSE : DRAW_ORDER_FORWARD);
            } else {
                result = setPrivateDrawingOrder(DRAW_ORDER_DEFAULT);
            }
            if (!result) return;
            if (needsPopulate) populate();
        }
    }
    

    /********************************************************************
     * animate by set velocity
     ********************************************************************/
    
    @Override
    void setCurrentItemInternal(int item, boolean smoothScroll, boolean always) {
        setCurrentItemInternal(item, smoothScroll, always, mAnimationVelocity);
    }
    
    /**
     * set velocity the velocity associated with a fling, if applicable. (0 otherwise)
     * @param velocity
     * @see {@link ViewPager#smoothScrollTo(int, int, int)}
     */
    @SuppressWarnings("javadoc")
    public void setAnimationVelocity(int velocity) {
        mAnimationVelocity = velocity;
    }
    
    public int getAnimationVelocity() {
        return mAnimationVelocity;
    }
    
    /********************************************************************
     * リフレクション
     ********************************************************************/
    private PageTransformer getPrivatePageTransformer() {
        Field nameField = null;
        try {
            nameField = ViewPager.class.getDeclaredField("mPageTransformer");
        } catch (SecurityException ignore) {
        } catch (NoSuchFieldException ignore) {
        }
        if (nameField == null) return null;
        
        nameField.setAccessible(true);
        Object result = null;
        try {
            result = nameField.get(this);
        } catch (IllegalArgumentException ignore) {
        } catch (IllegalAccessException ignore) {
        }
        return (PageTransformer) result;
    }
    
    private boolean setPrivatePageTransformer(PageTransformer transformer) {

        Field nameField = null;
        try {
            nameField = ViewPager.class.getDeclaredField("mPageTransformer");
        } catch (SecurityException ignore) {
        } catch (NoSuchFieldException ignore) {
        }

        if (nameField == null) return false;
        
        nameField.setAccessible(true);
        try {
             nameField.set(this, transformer);
             return true;
        } catch (IllegalArgumentException ignore) {
        } catch (IllegalAccessException ignore) {
        }

        return false;
    }
    
    
    private boolean setPrivateDrawingOrder(int drawingOrder) {
        Field nameField = null;
        try {
            nameField = ViewPager.class.getDeclaredField("mDrawingOrder");
        } catch (SecurityException ignore) {
        } catch (NoSuchFieldException ignore) {
        }
        if (nameField == null) return false;
        
        nameField.setAccessible(true);
        try {
             nameField.set(this, drawingOrder);
             return true;
        } catch (IllegalArgumentException ignore) {
        } catch (IllegalAccessException ignore) {
        }
        return false;
    }
}
package jp.ne.hatena.hackugyo.thoughtscalendar.ui.view;

import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.PageTransformer;
import android.view.View;

/**
 * Animation for {@link ViewPager}
 * スライド時に，下から上へ抜けていくようなアニメーションをさせます．
 * @author kwatanabe
 *
 */
public class VerticalPageTransformer implements PageTransformer, ViewPagerAnimatable {
    @SuppressWarnings("unused")
    private final VerticalPageTransformer self = this;
    

    @Override
    public void transformPage(View view, float position) {
        int pageWidth = view.getWidth();
        int pageHeight = view.getHeight();      
        // View#setAlpha()などがHONEYCOMB以降でないと使えないので，ラップする
        AnimatorProxy proxy = AnimatorProxy.wrap(view);

        if (position < -1) { // [-Infinity,-1)
            // This page is way off-screen to the left.

        } else if (position <= 1) { // [-1,1]
            // 横方向にはまったく動かしたくないので，デフォルトの動きを打ち消すように逆向きに動かす
            // Counteract the default slide transition
            proxy.setTranslationX(pageWidth * -position);

            proxy.setTranslationY(pageHeight * (position));

            // 上に行った子のせいで下の子との境界線が見えてしまうので，上の子を薄くしてやる
            proxy.setAlpha(1 - position * position); // y = -(x^2) + 1で，x = 0のとき濃さ1となる
        } else { // (1,+Infinity]
            // This page is way off-screen to the right.
        }
    }
}

package jp.ne.hatena.hackugyo.thoughtscalendar.util;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

public class DisplayUtils {
    private DisplayUtils() {

    }

    public static Display getDisplay(Context context) {
        if (context == null) return null;
        return ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    }

    @SuppressWarnings("deprecation")
    public static int getDisplayWidth(Display display) {
        int displayWidth = 0;
        if (Build.VERSION.SDK_INT >= 14) {
            Point size = new Point();
            display.getSize(size);
            displayWidth = size.x;
        } else {
            displayWidth = display.getWidth();
        }
        return displayWidth;
    }
}

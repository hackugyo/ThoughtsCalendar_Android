package jp.ne.hatena.hackugyo.thoughtscalendar.util;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.view.View;

public class PopupUtils {
    /***********************************************
     * Popup *
     **********************************************/

    public static PopupMenu getPopupMenu(Context context, View anchor, int menuId, PopupMenu.OnMenuItemClickListener onMenuItemClickListener, PopupMenu.OnDismissListener onDismissListener) {
        PopupMenu popup;
        popup = new PopupMenu(context, anchor);
        popup.getMenuInflater().inflate(menuId, popup.getMenu());
        if (onMenuItemClickListener != null) {
            popup.setOnMenuItemClickListener(onMenuItemClickListener);
        }
        if (onDismissListener != null) {
            popup.setOnDismissListener(onDismissListener);
        }
        return popup;
    }
}

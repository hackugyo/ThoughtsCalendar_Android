package jp.ne.hatena.hackugyo.thoughtscalendar.ui.fragment.dialogfragment;

import jp.ne.hatena.hackugyo.thoughtscalendar.R;
import jp.ne.hatena.hackugyo.thoughtscalendar.util.LogUtils;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.view.KeyEvent;

public class ProgressCenteringDialogFragment extends AbsCustomAlertDialogFragment {
    @SuppressWarnings("unused")
    private final ProgressCenteringDialogFragment self = this;

    public static final String TAG = "tag_progress_centering_dialog";

    /**
     * ファクトリーメソッド
     * 
     * @param title
     * @param message
     */
    public static ProgressCenteringDialogFragment newInstance(Bundle args, String title, String message) {
        ProgressCenteringDialogFragment fragment = new ProgressCenteringDialogFragment();
        args = initializeSettings(args, title, message, R.layout.dialog_progress_centering);
        args.putString(POSITIVE_TEXT, null);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Builder customizeBuilder(Builder builder, Bundle args) {
        return builder;
    }

    @Override
    public Dialog cutomizeDialog(Dialog dialog, Bundle args) {
        boolean isCancelable = args.getBoolean(IS_CANCELABLE, false);
        dialog.setCancelable(isCancelable);
        dialog.setCanceledOnTouchOutside(isCancelable);
        if (!isCancelable) { // キャンセル不可の場合，KEYCODE_SEARCHも無効にしておく
            dialog.setOnKeyListener(new OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_BACK) return true; // ignore
                    LogUtils.v("keycode: " + keyCode);
                    return false;
                }
            });
        }
        return dialog;
    }
}

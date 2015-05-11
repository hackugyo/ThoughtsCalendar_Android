package jp.ne.hatena.hackugyo.thoughtscalendar.exception;

import java.lang.Thread.UncaughtExceptionHandler;

import jp.ne.hatena.hackugyo.thoughtscalendar.util.AppUtils;
import android.content.Context;

/**
 * キャッチされなかった例外を処理する
 * 
 * @author kwatanabe
 * 
 */
public class CustomUncaughtExceptionHandler implements UncaughtExceptionHandler {

    private UncaughtExceptionHandler mDefaultUncaughtExceptionHandler;

    public CustomUncaughtExceptionHandler(Context context) {
        // デフォルト例外ハンドラを保持する。
        mDefaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (AppUtils.isDebuggable()) ex.printStackTrace();

        // デフォルト例外ハンドラを実行し、強制終了します。
        mDefaultUncaughtExceptionHandler.uncaughtException(thread, ex);
    }
}

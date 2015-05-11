package jp.ne.hatena.hackugyo.thoughtscalendar.util;

import jp.ne.hatena.hackugyo.thoughtscalendar.CustomApplication;
import android.util.Log;

/**
 * Log出力クラス. android:debuggable が false の場合はログを出力しない
 * 基本的に、アプリケーションでのログ出力はこのクラスを使う
 * 
 * @author User
 */
public final class LogUtils {
    
    /**
     * タグを指定してデバッグログを出力します.
     * 
     * @param tag
     *            タグ
     * @param msg
     *            デバッグログ
     */
    public static void d(String tag, CharSequence msg) {
        if (!AppUtils.isDebuggable()) return;
        Log.d(tag, getLogForm(Thread.currentThread().getStackTrace()) + msg);
    }

    /**
     * デバッグログを出力します.
     * 
     * @param msg
     *            デバッグログ
     */
    public static void d(CharSequence msg) {
        if (!AppUtils.isDebuggable()) return;
        Log.d(getLogTag(), getLogForm(Thread.currentThread().getStackTrace()) + msg);
    }

    public static void i(CharSequence msg) {
        if (!AppUtils.isDebuggable()) return;
        Log.i(getLogTag(), getLogForm(Thread.currentThread().getStackTrace()) + msg);
    }

    public static void i(String tag, CharSequence msg) {
        if (!AppUtils.isDebuggable()) return;
        Log.i(tag, getLogForm(Thread.currentThread().getStackTrace()) + msg);
    }

    /**
     * エラーログを出力します.
     * 
     * @param msg
     *            エラーログ
     */
    public static void e(CharSequence msg) {
        Log.e(getLogTag(), getLogForm(Thread.currentThread().getStackTrace()) + msg);
    }

    /**
     * エラーログを出力します.
     * 
     * @param tag
     *            タグ
     * @param msg
     *            エラーログ
     */
    public static void e(String tag, CharSequence msg) {
        Log.e(tag, getLogForm(Thread.currentThread().getStackTrace()) + msg);
    }

    /**
     * エラーログを出力します.
     * 
     * @param msg
     *            エラーログ
     * @param e
     *            例外
     */
    public static void e(CharSequence msg, Throwable e) {
        Log.e(getLogTag(), getLogForm(Thread.currentThread().getStackTrace()) + msg, e);
    }

    /**
     * エラーログを出力します.
     * 
     * @param tag
     *            タグ
     * @param msg
     *            エラーログ
     * @param e
     *            例外
     */
    public static void e(String tag, CharSequence msg, Throwable e) {
        Log.e(tag, getLogForm(Thread.currentThread().getStackTrace()) + msg, e);
    }

    /**
     * タグを指定してワーニングログを出力します.
     * 
     * @param tag
     *            タグ
     * @param msg
     *            ワーニングログ
     */
    public static void w(String tag, CharSequence msg) {
        if (!AppUtils.isDebuggable()) return;
        Log.w(tag, getLogForm(Thread.currentThread().getStackTrace()) + msg);
    }

    /**
     * ワーニングログを出力します.
     * 
     * @param msg
     *            ワーニングログ
     */
    public static void w(CharSequence msg) {
        if (!AppUtils.isDebuggable()) return;
        Log.w(getLogTag(), getLogForm(Thread.currentThread().getStackTrace()) + msg);
    }

    /**
     * タグを指定してVerboseログを出力します.
     * 
     * @param tag
     *            タグ
     * @param msg
     *            Verboseログ
     */
    public static void v(String tag, CharSequence msg) {
        if (!AppUtils.isDebuggable()) return;
        Log.v(tag, getLogForm(Thread.currentThread().getStackTrace()) + msg);
    }

    /**
     * Verboseログを出力します.
     * 
     * @param msg
     *            Verboseログ
     */
    public static void v(CharSequence msg) {
        if (!AppUtils.isDebuggable()) return;
        Log.v(getLogTag(), getLogForm(Thread.currentThread().getStackTrace()) + msg);
    }

    /**
     * ログ出力した箇所のメソッド情報に加え，そのメソッドを呼び出したメソッドの情報も表示します．
     * 
     * @param logLevel
     * @param maxSteps
     * @param msg
     */
    public static void withCaller(int logLevel, int maxSteps, CharSequence msg) {
        if (!AppUtils.isDebuggable()) return;
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        if (stackTrace.length <= 3 || stackTrace == null) return;
        String message = getLogForm(stackTrace[3]) + msg;
        maxSteps = Math.min(3 + maxSteps, stackTrace.length - 1);
        for (int step = 4; step <= maxSteps; step++) {
            message = StringUtils.build(//
                    message, StringUtils.getCRLF(),//
                    "at ", getLogForm(stackTrace[step])//
                    );
        }
        switch (logLevel) {
            case Log.VERBOSE:
                Log.v(getLogTag(), message);
                break;
            case Log.DEBUG:
                Log.d(getLogTag(), message);
                break;
            case Log.INFO:
                Log.i(getLogTag(), message);
                break;
            case Log.WARN:
                Log.w(getLogTag(), message);
                break;
            case Log.ERROR:
                Log.e(getLogTag(), message);
                break;
            default:
                break;
        }
    }

    /**
     * ログのヘッダ情報を整形します.
     * 
     * @param elements
     *            実行中のメソッド情報
     * @return ヘッダ情報
     */
    private static String getLogForm(StackTraceElement[] elements) {
        if (elements.length <= 3 || elements == null) return "";
        return getLogForm(elements[3]);
    }

    private static String getLogForm(StackTraceElement element) {

        StringBuilder sb = new StringBuilder();
        try {
            String file = element.getFileName();
            String method = element.getMethodName();
            int line = element.getLineNumber();
            sb.append(StringUtils.ellipsizeMiddle(file.replace(".java", ""), 25, true));
            sb.append("#").append(StringUtils.ellipsize(method, 18, true));
            sb.append("() [").append(String.format("%1$04d", line)).append("] : ");
        } catch (NullPointerException ignore) {
            // ignore. return blank string.
            // リリースビルドでは，elements[3]がnullになるようなので，ここで握りつぶしておく．
        }
        return sb.toString();
    }
    
    private static String getLogTag() {
        return CustomApplication.getAppContext().getPackageName();
    }
}

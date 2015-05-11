package jp.ne.hatena.hackugyo.thoughtscalendar.ui.fragment;

import java.util.HashMap;

import jp.ne.hatena.hackugyo.thoughtscalendar.CustomApplication;
import jp.ne.hatena.hackugyo.thoughtscalendar.R;
import jp.ne.hatena.hackugyo.thoughtscalendar.ui.AbsFragment;
import jp.ne.hatena.hackugyo.thoughtscalendar.ui.fragment.dialogfragment.AbsCustomAlertDialogFragment.Callbacks;
import jp.ne.hatena.hackugyo.thoughtscalendar.ui.fragment.dialogfragment.PlainAlertDialogFragment;
import jp.ne.hatena.hackugyo.thoughtscalendar.ui.fragment.dialogfragment.ProgressCenteringDialogFragment;
import jp.ne.hatena.hackugyo.thoughtscalendar.ui.fragment.dialogfragment.ProgressDialogFragment;
import jp.ne.hatena.hackugyo.thoughtscalendar.ui.fragment.dialogfragment.ProgressDialogFragment.ProgressCallbacks;
import jp.ne.hatena.hackugyo.thoughtscalendar.util.ConnectionUtils;
import jp.ne.hatena.hackugyo.thoughtscalendar.util.LogUtils;
import jp.ne.hatena.hackugyo.thoughtscalendar.util.StringUtils;
import jp.ne.hatena.hackugyo.thoughtscalendar.util.UrlUtils;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

/**
 * JSON通信を行うフラグメント．<br>
 * 通信時，プログレスダイアログを表示します．<br>
 * プログレスダイアログは，メッセージにnullを指定すると非表示，空文字を指定するとセンタリングされたものになります．<br>
 * 
 * @author kwatanabe
 * 
 */
public abstract class AbsApiFragment<T> extends AbsFragment implements Response.Listener<T>, Response.ErrorListener, Callbacks, ProgressCallbacks {
    protected static final String API = "AbsJsonApiFragment#API";
    protected static final String TAG_RETRY = "LoginFragment#TAG_RETRY";
    private final AbsApiFragment<T> self = this;
    private RequestQueue mQueue;
    private HashMap<String, Response.Listener<T>> mOnResponseListeners = new HashMap<String, Response.Listener<T>>();
    private HashMap<String, Response.ErrorListener> mOnErrorListeners = new HashMap<String, Response.ErrorListener>();

    /***********************************************
     * Fragment Showing *
     ***********************************************/

    /**
     * Fragmentが表示状態になったことの通知を受け取ります．
     * 
     * @param shouldReloadCurrentFragment
     *            true: すでに表示されているが改めて表示状態にしたい false: すでに表示されている場合は無視したい
     */
    public void notifyVisible(boolean shouldReloadCurrentFragment) {
        // Nothing to do(Use this method by overriding.)
    }

    /***********************************************
     * Volley *
     ***********************************************/
    /**
     * デフォルトのレスポンスイベント通知．<br>
     * 1つのFragmentに複数種類のAPIがある場合，
     * {@link #setOnResponseListener(Response.Listener, String)}を利用して個別に設定できます．
     */
    @Override
    public void onResponse(T response) {
        removeFragment(TAG_PROGRESS_DIALOG_DEFAULT);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        removeFragment(TAG_PROGRESS_DIALOG_DEFAULT);
        // エラー時の処理
        // ・アラート出して，こけたら破棄/再試行
        // ・単にアラート出して終了
    }

    protected RequestQueue getQueue() {
        return Volley.newRequestQueue(getActivitySafely(), CustomApplication.getHttpStack());
    }

    /**
     * 複数種類のAPIを操作するFragmentについては，<br>
     * このメソッドでListenerのタグを設定し，使い分けることができます．
     * 
     * @param listener
     * @param tag
     *            APIのURLを使うことができます．空文字の場合，例外を吐きます．
     */
    protected void setOnResponseListener(Response.Listener<T> listener, String tag) {
        if (StringUtils.isEmpty(tag)) throw new IllegalArgumentException("The tag must not be empty.");
        mOnResponseListeners.put(tag, listener);
    }

    protected boolean isOnResponseListenerExist(String reponseListenerTag) {
        return mOnResponseListeners.containsKey(reponseListenerTag);
    }

    /**
     * 複数種類のAPIを操作するFragmentについては，<br>
     * このメソッドでListenerのタグを設定し，使い分けることができます．
     * 
     * @param listener
     * @param tag
     *            APIのURLを使うことができます．
     */
    protected void setOnErrorListener(Response.ErrorListener listener, String tag) {
        if (StringUtils.isEmpty(tag)) throw new IllegalArgumentException("The tag must not be empty.");
        mOnErrorListeners.put(tag, listener);
    }

    protected boolean isOnErrorListenerExistFor(String errorListenerTag) {
        return mOnErrorListeners.containsKey(errorListenerTag);
    }

    /***********************************************
     * 通信(GET) *
     ***********************************************/

    public HashMap<String, String> getGETRequestParams() {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        return hashMap;
    }

    protected void getAPIAsync(String apiUrl) {
        getAPIAsync(apiUrl, "通信中");
    }

    /**
     * API取得．progressMessageがnullの場合，プログレスダイアログは出さない
     * 
     * @param apiUrl
     * @param progressMessage
     */
    protected void getAPIAsync(String apiUrl, String progressMessage) {
        getAPIAsync(apiUrl, progressMessage, "");
    }

    protected void getAPIAsync(String apiUrl, String progressMessage, String listenerTag) {
        // GETのときはパラメータをクエリストリングに変える
        String queryString = UrlUtils.getQueryStringFromParams(getGETRequestParams());
        if (StringUtils.isPresent(queryString)) {
            apiUrl = new StringBuilder(apiUrl).append("?").append(queryString).toString();
        }

        connectAPIAsync(apiUrl, progressMessage, listenerTag, Request.Method.GET, null);
    }

    protected void connectAPIAsync(String apiUrl, String progressMessage, String listenerTag, int method, HashMap<String, String> params) {

        if (StringUtils.isEmpty(listenerTag)) listenerTag = apiUrl;
        Response.Listener<T> listener = mOnResponseListeners.get(listenerTag);
        if (listener == null) listener = self;
        ErrorListener errorListener = mOnErrorListeners.get(listenerTag);
        if (errorListener == null) errorListener = self;

        if (StringUtils.isEmpty(apiUrl)) {
            errorListener.onErrorResponse(new VolleyError("Url is null."));
            return;
        }
        if (!ConnectionUtils.isConnected()) {
            if (progressMessage == null) {
                errorListener.onErrorResponse(new VolleyError("No Network Connection."));
            } else {
                showDialogFragment(//
                        PlainAlertDialogFragment.newInstance(null, CustomApplication.getStringById(R.string.Error_Message_NoNetworkConnection)), "");
            }
            return;
        }

        Request<T> request = createRequest(method, apiUrl, listener, errorListener);
        request.setTag(listenerTag); // キャンセル用
        request.setShouldCache(false);

        getQueue().add(request);
        // プログレスダイアログの表示
        if (progressMessage != null) {
            Bundle args = new Bundle();
            args.putString(API, listenerTag);
            DialogFragment fragment = null;
            if (progressMessage.isEmpty()) {
                args.putBoolean(ProgressCenteringDialogFragment.IS_CANCELABLE, false);
                args.putString(ProgressCenteringDialogFragment.NEGATIVE_TEXT, null);
                fragment = ProgressCenteringDialogFragment.newInstance(args, null, progressMessage);
            } else {
                fragment = ProgressDialogFragment.createProgressDialog(args, null, progressMessage);
            }
            showDialogFragment(fragment, TAG_PROGRESS_DIALOG_DEFAULT);
        }
    }

    abstract Request<T> createRequest(int method, String apiUrl, Listener<T> listener, ErrorListener errorListener);

    /***********************************************
     * 通信(POST) *
     ***********************************************/

    public HashMap<String, String> getPOSTRequestParams() {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        return hashMap;
    }

    protected void postAPIAsync(String apiUrl) {
        postAPIAsync(apiUrl, "処理中", apiUrl);
    }

    protected void postAPIAsync(String apiUrl, String progressMessage) {
        postAPIAsync(apiUrl, progressMessage, apiUrl);
    }

    protected void postAPIAsync(String apiUrl, String progressMessage, String listenerTag) {
        connectAPIAsync(apiUrl, progressMessage, listenerTag, Request.Method.POST, getPOSTRequestParams());
    }

    /***********************************************
     * {@link DialogFragment} *
     ***********************************************/

    protected void showUnexpectedErrorDialogFragment() {
        showRetryDialogFragment(CustomApplication.getStringById(R.string.Error_Message_UnexpectedError));
    }

    protected void showNetworkErrorDialogFragment() {
        showRetryDialogFragment(CustomApplication.getStringById(R.string.Error_Message_NoNetworkConnection));
    }

    void showRetryDialogFragment(String message) {
        Bundle args = new Bundle();
        args.putString(PlainAlertDialogFragment.POSITIVE_TEXT, "リトライ");
        args.putString(PlainAlertDialogFragment.NEGATIVE_TEXT, "キャンセル");
        showDialogFragment(//
                PlainAlertDialogFragment.newInstanceWithYesNo(null, //
                        message, //
                        args),//
                TAG_RETRY);
    }

    @Override
    public void onAlertDialogClicked(String tag, Bundle args, int which) {
        LogUtils.d("このメッセージが見えたら何かがおかしい " + tag);
    }

    @Override
    public void onAlertDialogCancelled(String tag, Bundle args) {
        LogUtils.d("このメッセージが見えたら何かがおかしい " + tag);
    }

    protected void onProgressDismissed() {
        // nothing to do.
    }

    @Override
    public void onProgressCancelled(String tag, Bundle args) {
        if (StringUtils.isSame(tag, TAG_PROGRESS_DIALOG_DEFAULT)) {
            if (args != null) mQueue.cancelAll(args.getString(API));
            onProgressDismissed();
        }
    }

    /**
     * 内部処理をキャンセルできるFragmentのインタフェース．
     * 
     * @author kwatanabe
     */
    public static interface Cancellable {
        public void cancelAll();
    }

    /**
     * Fragmentが表示されたタイミングを把握できるFragmentのインタフェース．
     * 
     * @author kwatanabe
     */
    public static interface NotifiableVisibilityChanged {
        public void notifyVisible(boolean shouldReloadCurrentFragment);
    }
}

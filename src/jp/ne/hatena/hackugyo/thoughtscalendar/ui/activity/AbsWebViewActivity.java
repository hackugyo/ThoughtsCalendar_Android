package jp.ne.hatena.hackugyo.thoughtscalendar.ui.activity;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import jp.ne.hatena.hackugyo.thoughtscalendar.R;
import jp.ne.hatena.hackugyo.thoughtscalendar.ui.AbsFragmentActivity;
import jp.ne.hatena.hackugyo.thoughtscalendar.ui.fragment.dialogfragment.AbsCustomAlertDialogFragment;
import jp.ne.hatena.hackugyo.thoughtscalendar.ui.fragment.dialogfragment.PlainAlertDialogFragment;
import jp.ne.hatena.hackugyo.thoughtscalendar.util.AppUtils;
import jp.ne.hatena.hackugyo.thoughtscalendar.util.LogUtils;
import jp.ne.hatena.hackugyo.thoughtscalendar.util.StringUtils;

import org.apache.http.protocol.HTTP;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslCertificate;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;

/**
 * WebViewの親abstractクラス
 * 
 * @author kwatanabe
 * 
 */
abstract public class AbsWebViewActivity extends AbsFragmentActivity implements AbsCustomAlertDialogFragment.Callbacks {
    public static final String TARGET_URL_KEY = "target_url";
    private final AbsWebViewActivity self = this;
    protected WebView mWebView;
    protected String mInitialTargetUrl;
    /** 戻る，進む，更新，中止のボタン */
    ImageButton mBackController, mNextController, mReloadController, mStopController;

    /***********************************************
     * Life Cycle *
     ***********************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isFinishing()) {
            return;
        }
        setContentView(R.layout.activity_webview);

        if (AppUtils.isDebuggable()) {
            getApplicationContext().enforceCallingOrSelfPermission(//
                    android.Manifest.permission.INTERNET, "need permission: INTERNET");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        setView();

        if (mInitialTargetUrl == null) mInitialTargetUrl = getIntent().getStringExtra(TARGET_URL_KEY);
        setWebView();
        if (mWebView.getOriginalUrl() == null) {
            // バックグラウンドから復帰させ，onStartが再度呼ばれても，最初のURLを再読み込みしないよう修正
            mWebView.loadUrl(mInitialTargetUrl);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // gifアニメが中にあった場合，停止させる
        // @see http://starzero.hatenablog.com/entry/20120716/1342421720
        mWebView.resumeTimers();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWebView.pauseTimers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // WebViewを廃棄
        destroyWebView();

        // クッキーを削除する場合下記コードを復帰させること
        // CookieManager cookieManager = CookieManager.getInstance();
        // cookieManager.removeAllCookie();
    }

    void destroyWebView() {
        if (mWebView == null) return;
        mWebView.stopLoading();
        mWebView.clearCache(true);
        unregisterForContextMenu(mWebView);

        // ここで，WebViewを即座に廃棄してしまうと，ZoomControlのfadingのバグが発生する．
        // http://stackoverflow.com/questions/5267639/how-to-safely-turn-webview-zooming-on-and-off-as-needed
        long timeout = ViewConfiguration.getZoomControlsTimeout();
        // Timerで動作させると'Timer-0'という別のスレッドで動いてしまいエラーになるので，
        // Handlerを介してUIスレッドで動作させる．
        new Handler(getMainLooper()).postDelayed(new Runnable() {

            @Override
            public void run() {
                ViewGroup vg = (ViewGroup) mWebView.getParent();
                if (vg != null) vg.removeView(mWebView);
                mWebView.destroy();
                mWebView = null;
            }
        }, timeout + 100l);
    }

    /***********************************************
     * View *
     ***********************************************/

    @SuppressLint("SetJavaScriptEnabled")
    protected void setWebView() {

        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.setWebViewClient(mWebViewClient);
        mWebView.setWebChromeClient(mWebChromeClient);
        // mWebView.setBackgroundColor(0); // 背景を透明に

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setGeolocationEnabled(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSaveFormData(false);
        setNotToSavepassword(webSettings);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        mWebView.setVerticalScrollbarOverlay(true);
        mWebView.setHorizontalScrollbarOverlay(true);

        // WebViewで使うcookieの準備
        CookieSyncManager.createInstance(self);
        CookieSyncManager.getInstance().startSync();
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().removeExpiredCookie();
    }

    protected void setView() {
        setActionBar();
    }

    protected abstract void setActionBar();

    /**
     * 「戻る」動作を規定する．メニューが出ていない状態でのバックキー押下時など
     * 
     * @return 「戻る」動作を実行し，これ以上の処理が不要な場合true．通常のbackキー動作も必要な場合false
     */
    protected boolean goBack() {
        mWebView.stopLoading();
        mWebView.goBack();
        return true;
    }

    @SuppressWarnings("deprecation")
    private static WebSettings setNotToSavepassword(WebSettings webSettings) {
        // Saving passwords in WebView will not be supported in future versions. 
        webSettings.setSavePassword(false);
        return webSettings;
    }

    /***********************************************
     * WebViewClient *
     **********************************************/

    /**
     * {@link WebViewClient}の
     * {@link WebViewClient#onPageStarted(WebView, String, Bitmap)}で呼ばせている．
     * 
     * @param webView
     * @param url
     * @param favicon
     */
    protected void onPageStarted(WebView webView, String url, Bitmap favicon) {
        getSupportActionBar().setTitle(getWebViewTitle(null));
    }

    /**
     * {@link WebViewClient}の
     * {@link WebViewClient#onPageFinished(WebView, String)}で呼ばせている．
     * 
     * @param webView
     * @param url
     */
    protected void onPageFinished(WebView webView, String url) {
        getSupportActionBar().setTitle(getWebViewTitle(webView));
    }

    /**
     * カスタムwebViewClient 読み込み進捗のプログレスダイアログを表示します． メールリンク・電話番号リンクを正しくハンドリングします．
     * 
     */
    private final WebViewClient mWebViewClient = new WebViewClientWithCookie() {
        private boolean mIsLoadingFinished = true;
        private boolean mIsRedirected = false;

        @Override
        public void onPageStarted(WebView webView, String url, Bitmap favicon) {
            mIsLoadingFinished = false;

            // webView.clearView(); を行うと，
            // 背景画像だけが見えてしまう場合があり，API18でdeprecatedになっているので，
            // コメントアウトした．
            // webView.clearView();

            super.onPageStarted(webView, url, favicon);
            self.onPageStarted(webView, url, favicon);
        }

        /**
         * {@link WebViewClientWithCookie#onPageFinished(WebView, String)}
         * をoverrideした．
         * GingerBreadやそれ以前では，リダイレクト後にonPageFinished()が呼ばれないバグがあるため，
         * リダイレクトは一律に新規読み込みとしている． そのフラグ処理の箇所は，HoneyComb以降では必ずスキップできるようにしている．
         */
        @Override
        public void onPageFinished(WebView view, String url) {
            if (!checkUrl(url)) {
                view.stopLoading();
            }

            super.onPageFinished(view, url);
            if (!mIsRedirected) mIsLoadingFinished = true;
            if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) && mIsRedirected) {
                mIsRedirected = false;
            } else {
                //HIDE LOADING. IT HAS FINISHED.
                self.onPageFinished(view, url);
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
        }

        /**
         * {@link WebViewClient#shouldOverrideUrlLoading(WebView, String)} の実装．
         * ホワイトリストをチェックする． また，MailTo,
         * Dialに反応しないWebViewが機種により存在するので，こちらでハンドリングする．
         */
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            if (!checkUrl(url)) {
                // これ以上何もせずに終了
                return true;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                // 素通しする
                return super.shouldOverrideUrlLoading(view, url);
            }

            // 以下の処理は，GingerBreadまたはそれ以前で，shouldOverrideLoading()がリダイレクト時に呼ばれないことを前提とした処理．
            if (!mIsLoadingFinished) mIsRedirected = true;
            mIsLoadingFinished = false;

            LogUtils.d("redirected to: " + url);
            mWebView.loadUrl(url); // リダイレクトのときは再読み込み．
            return true;
        }

        @Override
        public void showSslErrorDialog(SslError error) {
            self.showSslErrorDialog(error);
        }
    };

    /**
     * その他の事情で特定URLの検知時に反応すべきであれば，このメソッドをoverrideして対応します．
     * 
     * @param url
     * @return true: 読み込み停止 false: 読み込み続行
     */
    protected boolean shouldOverride(String url) {
        return false;
    }

    /***********************************************
     * WebChromeClient *
     **********************************************/
    private final WebChromeClient mWebChromeClient = new WebChromeClient() {
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, Callback callback) {
            callback.invoke(origin, true, false);
        }
    };

    /***********************************************
     * Protected Method *
     **********************************************/
    /**
     * 読み込みを停止します．
     */
    protected void stop() {
        if (mWebView != null) mWebView.stopLoading();
    }

    /**
     * URLをホワイトリストその他と照合し，読み込みを続行するかどうか判定する
     * 
     * @param url
     * @return true: 通常どおり読み込みを続行 false: これ以上の処理は必要ない
     */
    protected boolean checkUrl(String url) {
        if (url.contains(WebView.SCHEME_MAILTO)) {
            self.pushMailToAction(url);
            return false;
        } else if (url.contains(WebView.SCHEME_TEL)) {
            self.pushDialAction(url);
            return false;
        } else if (shouldOverride(url)) {
            return false;
        }
        return true;
    }

    private void pushMailToAction(String url) {
        self.stop();
        url = url.replace(WebView.SCHEME_MAILTO, "");
        String[] str = url.split("\\?");
        String email = str[0];
        String subject = "";
        try {
            if (str.length > 1) subject = URLDecoder.decode(str[1].replace("subject=", ""), HTTP.UTF_8);
        } catch (UnsupportedEncodingException e) {
            if (AppUtils.isDebuggable()) e.printStackTrace();
        }
        final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] { email });
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        startActivity(Intent.createChooser(intent, "アプリケーションを選択")); // TODO later メールアプリ選択
    }

    private void pushDialAction(String url) {
        self.stop();
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_DIAL, uri);
        startActivity(intent);
    }

    private void showSslErrorDialog(SslError error) {
        DialogFragment fragment = PlainAlertDialogFragment.newInstance("SSL接続エラー", createErrorMessage(error));
        showDialogFragment(fragment, "tag_ssl_error_alert_dialog");
    }

    protected String createErrorMessage(SslError error) {
        SslCertificate cert = error.getCertificate();
        StringBuilder result = new StringBuilder().append("サイトのセキュリティ証明書が信頼できません。接続を終了しました。\n\nエラーの原因\n");
        switch (error.getPrimaryError()) {
            case SslError.SSL_EXPIRED:
                result.append("証明書の有効期限が切れています。\n\n終了時刻=").append(cert.getValidNotAfterDate());
                return result.toString();
            case SslError.SSL_IDMISMATCH:
                result.append("ホスト名が一致しません。\n\nCN=").append(cert.getIssuedTo().getCName());
                return result.toString();
            case SslError.SSL_NOTYETVALID:
                result.append("証明書はまだ有効ではありません\n\n開始時刻=").append(cert.getValidNotBeforeDate());
                return result.toString();
            case SslError.SSL_UNTRUSTED:
                result.append("証明書を発行した認証局が信頼できません\n\n認証局\n").append(cert.getIssuedBy().getDName());
                return result.toString();
            default:
                result.append("原因不明のエラーが発生しました");
                return result.toString();
        }
    }

    protected String getWebViewTitle(WebView webView) {
        if (webView == null) return "Loading…";
        return webView.getTitle();
    }

    /***********************************************
     * Dialog fragment *
     **********************************************/
    public static class BackAlertDialogFragment extends AbsCustomAlertDialogFragment {
        public static final String TAG = "tag_back_alert_dialog";

        /** フラグメントのファクトリーメソッド． */
        public static BackAlertDialogFragment newInstance() {
            BackAlertDialogFragment fragment = new BackAlertDialogFragment();
            Bundle args = initializeSettings(null, null, "前の画面に戻りますか？", null);
            args.putBoolean(IS_CANCELABLE, true);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public Builder customizeBuilder(Builder builder, Bundle args) {
            return builder;
        }

        @Override
        public Dialog cutomizeDialog(Dialog dialog, Bundle args) {
            return dialog;
        }
    }

    @Override
    public void onAlertDialogClicked(String tag, Bundle args, int which) {
        if (StringUtils.isSame(tag, BackAlertDialogFragment.TAG)) {
            // 前の画面に戻ってOK
            if (which == DialogInterface.BUTTON_POSITIVE) self.finish();
        }
    }

    @Override
    public void onAlertDialogCancelled(String tag, Bundle args) {
        if (StringUtils.isSame(tag, BackAlertDialogFragment.TAG)) {
            // nothing to do.
        }
    }

    /***********************************************
     * public method *
     **********************************************/
    public WebView getWebView() {
        return mWebView;
    }

    /***********************************************
     * Cookieを処理できるWebViewClient *
     **********************************************/
    public abstract class WebViewClientWithCookie extends WebViewClient {

        //  private String mLoginCookie = "";

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
            // CookieManager cMgr = CookieManager.getInstance();
            // mLoginCookie = cMgr.getCookie(url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            // CookieManager cMgr = CookieManager.getInstance();
            // cMgr.setCookie(url, mLoginCookie);
            // LogUtils.v("onPageFinished: " + url + " / cookie: " + (mLoginCookie == null ? "no cookie." : mLoginCookie.toString()));
            LogUtils.v(new StringBuilder("onPageFinished: ").append(url).append(" : ").append(CookieManager.getInstance().getCookie(url)).toString());
        }

        /**
         * SSL通信で問題があるとエラーダイアログを表示し、接続を中止する
         */
        @Override
        public void onReceivedSslError(WebView webview, SslErrorHandler handler, SslError error) {
            showSslErrorDialog(error);
            handler.cancel();
        }

        public abstract void showSslErrorDialog(SslError error);
    }

}

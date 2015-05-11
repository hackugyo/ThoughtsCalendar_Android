package jp.ne.hatena.hackugyo.thoughtscalendar.ui.activity;

import jp.ne.hatena.hackugyo.thoughtscalendar.R;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.widget.ImageButton;

/**
 * アプリ内WebView<br>
 * 
 * @author kwatanabe
 * 
 */
public class WebViewActivity extends AbsWebViewActivity {

    public static final String FIXED_TITLE = "WebViewActivity:FixedTitle";
    private String mFixedTitle;

    /***********************************************
     * Life Cycle *
     ***********************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(getWebViewTitle(null));
        restoreMemberInfo(getIntent());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // クッキー削除
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
    }

    @Override
    public void onBackPressed() {
        boolean consumed = false;
        if (mWebView.canGoBack()) {
            consumed = goBack();
        }
        if (!consumed) {
            super.onBackPressed();
            overridePendingTransition(R.anim.activity_close_enter, R.anim.activity_close_exit);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                boolean result = (super.onOptionsItemSelected(menuItem));
                overridePendingTransition(R.anim.activity_close_enter, R.anim.activity_close_exit);
                return result;

        }
        return (super.onOptionsItemSelected(menuItem));
    }

    /***********************************************
     * View *
     ***********************************************/
    @Override
    protected void setWebView() {
        super.setWebView();
        // クッキーをWebViewに引き継ぐ
        // CookieManager.getInstance().setCookie("");
        // CookieSyncManager.getInstance().sync();
    }

    @Override
    protected void setView() {
        // ActionBar設定
        setActionBar();
        setOnClickListener();
    }

    /***********************************************
     * ActionBar *
     ***********************************************/
    @Override
    protected void setActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    /***********************************************
     * View *
     ***********************************************/
    /**
     * WebViewのコントローラ（戻る，進む，更新，中止）を作ります．
     */
    private void setOnClickListener() {
        findViewById(R.id.WebView_Controller).setVisibility(View.VISIBLE);
        getBackController().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mWebView.canGoBack()) return;
                goBack();
            }
        });
        getNextController().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mWebView.canGoForward()) return;
                mWebView.goForward();
            }
        });
        getReloadController().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWebView.reload();
            }
        });
        getStopController().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWebView.stopLoading();
            }
        });
    }

    private ImageButton getBackController() {
        if (mBackController == null) mBackController = (ImageButton) findViewById(R.id.WebView_Controller_Back);
        return this.mBackController;
    }

    private ImageButton getNextController() {
        if (mNextController == null) mNextController = (ImageButton) findViewById(R.id.WebView_Controller_Next);
        return this.mNextController;
    }

    private ImageButton getReloadController() {
        if (mReloadController == null) mReloadController = (ImageButton) findViewById(R.id.WebView_Controller_Reload);
        return this.mReloadController;
    }

    private ImageButton getStopController() {
        if (mStopController == null) mStopController = (ImageButton) findViewById(R.id.WebView_Controller_Stop);
        return this.mStopController;
    }

    /***********************************************
     * WebViewClient *
     **********************************************/

    @Override
    protected void onPageStarted(WebView webView, String url, Bitmap favicon) {
        super.onPageStarted(webView, url, favicon);
        setControllerAvailability(true, webView);
        setActionBarLoadingVisible(true);
    }

    @Override
    protected void onPageFinished(WebView webView, String url) {
        super.onPageFinished(webView, url);
        setControllerAvailability(false, webView);
        setActionBarLoadingVisible(false);
    }

    /**
     * 各ボタンの押下可能状態を更新します． 初期状態では，戻る・進むはdisable です． ページ読み込み開始時，中止ボタンをenableにします．
     * ページ読み込み終了時，中止ボタンをdisableにし，その他すべてをenableにします．
     * 
     * @param isLoading
     *            : ページ読み込み中かどうか
     */
    private void setControllerAvailability(boolean isLoading, WebView webView) {
        getBackController().setEnabled(webView != null && webView.canGoBack());
        getNextController().setEnabled(webView != null && webView.canGoForward());
        getReloadController().setEnabled(true);
        getStopController().setEnabled(isLoading);
    }

    /**
     * タイトルラベルの左側にloadingスピナを出すか制御します
     * 
     * @param visible
     */
    protected void setActionBarLoadingVisible(boolean visible) {
        // nothing to do.
    }

    @Override
    protected String getWebViewTitle(WebView webView) {
        if (mFixedTitle == null) {
            return super.getWebViewTitle(webView);
        } else {
            return mFixedTitle;
        }
    }

    /***********************************************
     * Restore member info *
     ***********************************************/

    private void restoreMemberInfo(Intent callerIntent) {
        if (callerIntent == null) return;
        mFixedTitle = callerIntent.getStringExtra(WebViewActivity.FIXED_TITLE);
    }

}

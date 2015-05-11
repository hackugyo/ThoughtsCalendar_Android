package jp.ne.hatena.hackugyo.thoughtscalendar.ui;

import jp.ne.hatena.hackugyo.thoughtscalendar.CustomApplication;
import jp.ne.hatena.hackugyo.thoughtscalendar.R;
import jp.ne.hatena.hackugyo.thoughtscalendar.ui.activity.AbsWebViewActivity;
import jp.ne.hatena.hackugyo.thoughtscalendar.ui.activity.WebViewActivity;
import jp.ne.hatena.hackugyo.thoughtscalendar.ui.fragment.dialogfragment.ProgressCenteringDialogFragment;
import jp.ne.hatena.hackugyo.thoughtscalendar.ui.fragment.dialogfragment.ProgressDialogFragment;
import jp.ne.hatena.hackugyo.thoughtscalendar.util.AppUtils;
import jp.ne.hatena.hackugyo.thoughtscalendar.util.FragmentUtils;
import jp.ne.hatena.hackugyo.thoughtscalendar.util.LogUtils;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;

public class AbsFragment extends Fragment {
    private final AbsFragment self = this;
    /**
     * {@link #onAttach(Activity)}のタイミングで，親Activityへの参照を確保する．
     * 
     * @see <a href="https://gist.github.com/nagakenjs/6098350">参考ページ</a>
     */
    private AbsFragmentActivity mActivity;

    /***********************************************
     * Life Cycle *
     **********************************************/
    @Override
    public void onAttach(Activity activity) {
        if (!(activity instanceof ActionBarActivity)) {
            throw new IllegalStateException(getClass().getSimpleName() + " must be attached to a ActionBarActivity.");
        }
        mActivity = (AbsFragmentActivity) activity;

        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mActivity = null;
        super.onDetach();
    }

    /***********************************************
     * Fragment Control *
     **********************************************/
    /**
     * タグで指定されたフラグメントを消去します
     * 
     * @param fragmentTag
     */
    protected boolean removeFragment(String fragmentTag) {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        if (getFragmentManager() == null) {
            LogUtils.e("fragmentmanger not found");
            return false;
        }
        Fragment prev = getFragmentManager().findFragmentByTag(fragmentTag);

        if (prev == null) return false;

        if (prev instanceof DialogFragment) {
            final Dialog dialog = ((DialogFragment) prev).getDialog();

            if (dialog != null && dialog.isShowing()) {
                // 最新のソースだと，dialogそのものをdismissする前にフラグを見て抜けてしまうので，
                // dialog自体は別途dismiss()してやるのが確実．
                dialog.dismiss(); // http://blog.zaq.ne.jp/oboe2uran/article/876/
                // prev.dismiss()を呼んではだめ． http://memory.empressia.jp/article/44110106.html
                ((DialogFragment) prev).onDismiss(dialog); // DialogFragmentの場合は閉じる処理も追加
            }
        }

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.remove(prev);
        ft.commitAllowingStateLoss();
        ft = null;
        getFragmentManager().executePendingTransactions();
        return true;
    }

    /**
     * {@link #getActivity()}の代わりに，親Activityを安全に取得する．
     * 
     * @return 親の{@link AbsFragmentActivity}
     */
    protected AbsFragmentActivity getActivitySafely() {
        if (mActivity == null) {
            FragmentActivity activity = getActivity();
            if (activity instanceof AbsFragmentActivity) mActivity = (AbsFragmentActivity) activity;
        }
        return mActivity;
    }

    /**
     * {@link AbsFragmentActivity#isShowingSameDialogFragment(String)}と同様
     * 
     * @param fragmentTag
     */
    protected boolean isShowingSameDialogFragment(String fragmentTag) {
        return FragmentUtils.isShowingSameDialogFragment(getFragmentManager(), fragmentTag);
    }

    protected void showDialogFragment(final DialogFragment fragment, final String tag) {
        // onLoadFinished()で呼ぶ際，単にshowしてしまうとCan not perform this action inside of onLoadFinishedが出るので，
        // Handlerを使う．
        Handler h = new Handler(Looper.getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                fragment.setTargetFragment(self, 0);
                try {
                    fragment.show(getFragmentManager(), tag);
                } catch (NullPointerException e) {
                    LogUtils.e("cannot get SupportFragmentManager." + tag);
                } catch (IllegalStateException e) {
                    LogUtils.e(tag + ": cannot show a dialog when this activity is in background.");
                    LogUtils.e(self.getClass().getSimpleName() + e.getMessage());
                    // 表示のタイミングでバックグラウンドにいた場合など，
                    // show()だとIllegalStateExceptionで落ちてしまう
                    // http://stackoverflow.com/a/16206036/2338047
                    // ただし，show()を使わないと内部的なフラグが動かないので，
                    // まずshow()を使ってフラグを立て，
                    // 失敗したときのみFragmet#commit()のかわりにFragment#commitAllowingStateLoss()を呼ぶ．
                    removeFragment(tag);
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.add(fragment, tag);
                    ft.commitAllowingStateLoss();
                    getFragmentManager().executePendingTransactions();
                }
            }
        });
    }

    /**
     * フラグメントから進捗ダイアログを表示します．
     * 
     * @param loaderId
     * @param message
     * @param isCancelable
     */
    protected boolean showProgressDialog(int loaderId, String message, boolean isCancelable) {
        if (isShowingSameDialogFragment(ProgressCenteringDialogFragment.TAG)) return false; // すでに同じものが出ていた場合，何もしない

        Bundle argsForProgressDialog = new Bundle();
        argsForProgressDialog.putInt(ProgressDialogFragment.TARGET_LOADER_ID, loaderId);
        argsForProgressDialog.putBoolean(ProgressDialogFragment.IS_CANCELABLE, isCancelable);
        if (isCancelable) {
            argsForProgressDialog.putString(ProgressDialogFragment.NEGATIVE_TEXT, CustomApplication.getStringById(R.string.Dialog_Button_Label_Negative));
        } else {
            argsForProgressDialog.putString(ProgressDialogFragment.NEGATIVE_TEXT, null); //キャンセル不可の場合，ボタンもなし
        }
        ProgressCenteringDialogFragment dialogFragment = ProgressCenteringDialogFragment.newInstance(argsForProgressDialog, null, null);
        dialogFragment.setTargetFragment(self, 0);
        dialogFragment.show(getFragmentManager(), ProgressCenteringDialogFragment.TAG);

        return true;
    }

    /**
     * このFragment専用のChildFragmentManagerを使って，内側のFragmentを置き換えます．
     * 
     * @param fragment
     * @param replacedId
     * @param fragmentTag
     */
    protected void updateFragment(Fragment fragment, int replacedId, String fragmentTag) {
        try {
            FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
            fragmentTransaction.replace(replacedId, fragment, fragmentTag)//
                    .commitAllowingStateLoss();
            getChildFragmentManager().executePendingTransactions();
        } catch (IllegalStateException e) {
            LogUtils.e("cannot replace.");
            if (AppUtils.isDebuggable()) e.printStackTrace();
        }
    }

    /***********************************************
     * intent handling *
     **********************************************/

    /**
     * 外部ブラウザを選択させて表示します．<br>
     * Andorid4.0以降，外部ブラウザが端末にインストールされていない場合があるため，<br>
     * このメソッドを利用することを推奨します．<br>
     * 
     * @param url
     */
    public void launchExternalBrowser(String url) {
        getActivitySafely().launchExternalBrowser(url);
    }

    protected void launchWebView(String url) {
        Intent i = new Intent(getActivitySafely(), WebViewActivity.class)//
                .putExtra(AbsWebViewActivity.TARGET_URL_KEY, url)//
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        getActivitySafely().overridePendingTransition(R.anim.activity_open_enter, R.anim.activity_open_exit);
    }

    protected void launchWebView(String url, String title) {
        Intent i = new Intent(getActivitySafely(), WebViewActivity.class)//
                .putExtra(AbsWebViewActivity.TARGET_URL_KEY, url)//
                .putExtra(WebViewActivity.FIXED_TITLE, title)//
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
    }

    protected void launchWebView(String url, Bundle extras) {
        Intent i = new Intent(getActivitySafely(), WebViewActivity.class)//
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (extras == null) extras = new Bundle();
        extras.putString(AbsWebViewActivity.TARGET_URL_KEY, url);

        i.putExtras(extras);
        startActivity(i);
    }
    
    protected void launchMap(String query) {
        Uri geoUri = Uri.parse("geo:0,0?q=" + query);
        Intent mapCall = new Intent(Intent.ACTION_VIEW, geoUri);
        mapCall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mapCall.setData(geoUri);
        startActivity(mapCall);
    }

    /***********************************************
     * プログレスダイアログ *
     **********************************************/
    protected static final String TAG_PROGRESS_DIALOG_DEFAULT = "TAG_PROGRESS_DIALOG_DEFAULT";

    protected void showProgressDialog() {
        showProgressDialog(TAG_PROGRESS_DIALOG_DEFAULT, null);
    }

    protected void showProgressDialog(String message) {
        showProgressDialog(TAG_PROGRESS_DIALOG_DEFAULT, message);
    }

    protected void showProgressDialog(String tag, String message) {
        ProgressDialogFragment fragment = ProgressDialogFragment.createProgressDialog(null, null, message);
        showDialogFragment(fragment, tag);
    }

    /***********************************************
     * Toast *
     **********************************************/
    /**
     * Activity内で消し忘れがないよう，単一のToastインスタンスを使い回します．
     * 
     * @param text
     * @param length
     */
    protected void showSingleToast(String text, int length) {
        getActivitySafely().showSingleToast(text, length);
    }

    /**
     * Activity内で消し忘れがないよう，単一のToastインスタンスを使い回します．
     * 
     * @param resId
     * @param length
     */
    protected void showSingleToast(int resId, int length) {
        getActivitySafely().showSingleToast(resId, length);
    }

    /**
     * 使い回している単一のToastインスタンスを破棄します．
     * 
     */
    protected void removeSingleToast() {
        getActivitySafely().removeSingleToast();
    }

}

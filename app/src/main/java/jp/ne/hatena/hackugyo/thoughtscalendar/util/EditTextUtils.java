package jp.ne.hatena.hackugyo.thoughtscalendar.util;

import android.app.Activity;
import android.content.Context;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * {@link EditText}関連のユーティリティです．<br>
 * 
 * @author kwatanabe
 * 
 */
public class EditTextUtils {

    /**
     * キーボードを閉じます
     * 
     * @see <a href="http://stackoverflow.com/a/17789187/2338047">参考ページ</a>
     * @param context
     * @param view
     */
    public static void closeKeyboard(Context context, View view) {

        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            return;
        }

        if (context instanceof Activity) {
            // ((Activity) context).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            View v = ((Activity) context).getCurrentFocus();
            if (v == null) {
                v = new View(((Activity) context));
            }
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
        // imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    /**
     * {@link EditText}の入力制限をかけます．<br>
     * {@link EditText#setInputType(int)}では，最初に表示するキーボードの種類しか制限できません．<br>
     * 
     * @return {@link EditText#setFilters(InputFilter[])}に渡してください．
     */
    public static InputFilter[] createNumericInputFilter() {
        InputFilter inputFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, //
                    int start, int end, Spanned dest, int dstart, int dend) {
                if (source.toString().matches("^[0-9]+$")) {
                    return source;
                } else {
                    return "";
                }
            }
        };
        return new InputFilter[] { inputFilter };
    }

    /**
     * {@link EditText}の入力制限をかけます．<br>
     * {@link EditText#setInputType(int)}では，最初に表示するキーボードの種類しか制限できません．<br>
     * 
     * @return {@link EditText#setFilters(InputFilter[])}に渡してください．
     */
    public static InputFilter[] createAlphaNumericInputFilter() {
        InputFilter inputFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, //
                    int start, int end, Spanned dest, int dstart, int dend) {
                if (source.toString().matches("^[0-9a-zA-Z]+$")) {
                    return source;
                } else {
                    return "";
                }
            }
        };
        return new InputFilter[] { inputFilter };
    }
}

package jp.ne.hatena.hackugyo.thoughtscalendar.util;

import android.util.Patterns;
import android.webkit.URLUtil;

public class UrlUtils {
    /**
     * URLが適切かどうかを返します．{@link URLUtil#isValidUrl(String)}にはバグがあるので使わないでください．
     * 
     * @see <a href="http://stackoverflow.com/a/5930532/2338047">参考ページ</a>
     * @param potentialUrl
     * @return valid or not valid
     */
    public static boolean isValidUrl(String potentialUrl) {
        return Patterns.WEB_URL.matcher(potentialUrl).matches();
    }
}

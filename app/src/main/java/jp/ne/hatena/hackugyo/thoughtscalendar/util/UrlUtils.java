package jp.ne.hatena.hackugyo.thoughtscalendar.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

import android.util.Patterns;
import android.webkit.URLUtil;

public class UrlUtils {

    private UrlUtils() {

    }

    /**
     * URLが適切かどうかを返します．{@link URLUtil#isValidUrl(String)}にはバグがあるので使わないでください．
     * 
     * @see <a href="http://stackoverflow.com/a/5930532/2338047">参考ページ</a>
     * @param potentialUrl
     * @return valid or not valid
     */
    public static boolean isValidUrl(String potentialUrl) {
        if (potentialUrl == null) return false;
        return Patterns.WEB_URL.matcher(potentialUrl).matches();
    }

    public static String getQueryStringFromParams(HashMap<String, String> params) {
        if (params == null) return null;
        StringBuilder sb = new StringBuilder();

        for (java.util.Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = "";

            if (entry.getValue() == null) {
                LogUtils.e("Cannot read params for " + key + " = null", new NullPointerException());
                continue;
            }
            try {
                value = URLEncoder.encode(entry.getValue(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                LogUtils.e("Cannot encode.", e);
            }
            if (StringUtils.isEmpty(value)) continue;
            if (sb.length() != 0) sb.append("&");
            sb.append(key).append("=").append(value);
        }
        return sb.toString();
    }
}

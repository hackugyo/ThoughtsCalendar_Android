package jp.ne.hatena.hackugyo.thoughtscalendar.util;

import jp.ne.hatena.hackugyo.thoughtscalendar.CustomApplication;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionUtils {
    public static boolean isConnected() {

        CustomApplication.getAppContext().enforceCallingOrSelfPermission(//
                android.Manifest.permission.ACCESS_NETWORK_STATE, "need permission: ACCESS_NETWORK_STATE");
        ConnectivityManager cm = (ConnectivityManager) CustomApplication.getSystemServiceOf(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null) {
            return cm.getActiveNetworkInfo().isConnected();
        }
        return false;
    }
}

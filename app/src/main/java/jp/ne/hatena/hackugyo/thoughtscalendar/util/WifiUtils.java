package jp.ne.hatena.hackugyo.thoughtscalendar.util;

import java.util.ArrayList;
import java.util.List;

import jp.ne.hatena.hackugyo.thoughtscalendar.CustomApplication;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

public class WifiUtils {

    /**
     * 現在利用可能なAccessPointの一覧を返します．
     */
    public static List<ScanResult> getScanResults() {
        WifiManager manager = (WifiManager) CustomApplication.getAppContext().getSystemService(Context.WIFI_SERVICE);
        if (manager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) return null;
        // APをスキャン
        CustomApplication.getAppContext().enforceCallingOrSelfPermission(android.Manifest.permission.CHANGE_WIFI_STATE, "need permission: CHANGE_WIFI_STATE");
        manager.startScan();
        // スキャン結果を取得
        return manager.getScanResults();
    }

    /**
     * 現在利用可能なAccessPointのSSID一覧のなかに，keyが含まれているかどうかを返します．
     */
    public static boolean getSSIDListContainsKey(String targetSSID) {
        if (targetSSID == null) return false;
        List<ScanResult> apList = WifiUtils.getScanResults();
        if (apList == null) return false;
        for (ScanResult ap : apList) {
            if (ap.SSID.equals(targetSSID)) {
                return true;
            }
        }
        return false;
    }

    public static boolean getSSIDListContainsSome(ArrayList<String> ssids) {
        if (ssids == null) return false;
        for (String ssid : ssids) {
            if (getSSIDListContainsKey(ssid)) return true;
        }
        return false;
    }

}

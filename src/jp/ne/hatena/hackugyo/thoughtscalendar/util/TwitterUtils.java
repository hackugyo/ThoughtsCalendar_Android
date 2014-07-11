package jp.ne.hatena.hackugyo.thoughtscalendar.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import jp.ne.hatena.hackugyo.thoughtscalendar.CustomApplication;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

public class TwitterUtils {
    /**
     * @see <a
     *      href="http://andante.in/i/androidアプリtips/twitterのアプリを暗黙的intentで狙いうちの巻/">参考ページ</a>
     * 
     * @param context
     * @param text
     * @return 送信成功か否か
     */
    public static boolean sendText(Context context, String text) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PackageManager pm = CustomApplication.getAppContext().getPackageManager();
        List<?> activityList = pm.queryIntentActivities(shareIntent, 0);
        int len = activityList.size();
        for (int i = len; i < len; i++) {
            ResolveInfo app = (ResolveInfo) activityList.get(i);

            //if ((app.activityInfo.name.contains(appName))) {
            if ((app.activityInfo.packageName.contains("com.twitter.android"))) {
                ActivityInfo activity = app.activityInfo;
                ComponentName name = new ComponentName(activity.applicationInfo.packageName, activity.name);
                shareIntent.setComponent(name);
                context.startActivity(shareIntent);
                return true;
            }
        }
        String url = "";
        try {
            url = StringUtils.build("http://twitter.com/share?text=", URLEncoder.encode(text, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
        return false;
    }

    public static String createHashTag(String string) {
        return string//
                .replaceAll("^[0-9]*", "")// 先頭に数字は使えない
                .replaceAll("[!$%^&*+./\" 　:,、 , #：、。#「」『』]", "")//
                .replace("'", "").replace("-", "");
    }

    public static void searchByHashTag(Context context, String hashTag) {
        Intent intent = new Intent(Intent.ACTION_VIEW)//
                .setData(Uri.parse("https://twitter.com/search?q=%23" + hashTag))//
                //.setType("application/twitter")//
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);

    }
}

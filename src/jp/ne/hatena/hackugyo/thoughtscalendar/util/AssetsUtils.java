package jp.ne.hatena.hackugyo.thoughtscalendar.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import android.content.Context;
import android.content.res.AssetManager;

public class AssetsUtils {

    /**
     * read a Text file from assets.
     * 
     * @param context
     * @param fileName
     * @return Text
     * @throws IOException
     */
    public static String readTextFromAsset(Context context, String fileName) throws IOException {
        AssetManager am = context.getResources().getAssets();
        BufferedReader br = null;

        StringBuilder sb = new StringBuilder();
        try {
            br = new BufferedReader(new InputStreamReader(am.open(fileName), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // ignore.
            return null;
        }

        String str;
        while ((str = br.readLine()) != null) {
            sb.append(str + "\n");
        }
        if (br != null) br.close();

        return sb.toString();
    }
}

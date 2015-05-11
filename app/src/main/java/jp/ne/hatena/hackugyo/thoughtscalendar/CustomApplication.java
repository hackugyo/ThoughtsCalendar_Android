package jp.ne.hatena.hackugyo.thoughtscalendar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

import jp.ne.hatena.hackugyo.thoughtscalendar.util.AppUtils;
import jp.ne.hatena.hackugyo.thoughtscalendar.util.LogUtils;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.webkit.CookieSyncManager;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;

public class CustomApplication extends Application {
    /** コンテキスト. */
    private static Context _context;
    /** プリファレンス. */
    private static SharedPreferences _sharedPreferences;

    /***********************************************
     * Life Cycle *
     ***********************************************/
    @Override
    public void onCreate() {
        super.onCreate();

        _context = getApplicationContext();
    }

    /***********************************************
     * Other methods *
     ***********************************************/

    /**
     * アプリケーションコンテキストを取得します.
     * 
     * @return Application Context
     */
    public static Context getAppContext() {
        return _context;
    }

    /**
     * アプリケーションで利用するプリファレンスを取得します. 共有モードは Private となります.
     * 
     * @return SharedPreferences
     */
    public static SharedPreferences getSharedPreferences() {
        if (_sharedPreferences == null) {
            _sharedPreferences = getAppContext().getSharedPreferences(Defines.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        }
        return _sharedPreferences;
    }

    public static Resources getResource() {
        return _context.getResources();
    }

    /**
     * {@link TypedArray}を取得して返します． 使い終わったら{@link TypedArray#recycle()}してください．
     * 
     * @param typedArrayId
     * @return TypedArray
     */
    @SuppressLint("Recycle")
    public static TypedArray getTypedArrayById(int typedArrayId) {
        return _context.getResources().obtainTypedArray(typedArrayId);
    }

    public static TypedArray getTypedArrayByName(String resourceName) {
        try {
            return getTypedArrayById(getArrayIdFor(resourceName));
        } catch (NotFoundException e) {
            if (AppUtils.isDebuggable()) e.printStackTrace();
            return null;
        }
    }

    public static String getStringById(int stringId) {
        return _context.getResources().getString(stringId);
    }

    public static String getStringByName(String resourceName) {
        try {
            return _context.getResources().getString(getStringIdFor(resourceName));
        } catch (NotFoundException e) {
            if (AppUtils.isDebuggable()) e.printStackTrace();
            return "";
        }
    }

    public static int getColorIntByColorId(int colorId) {
        return _context.getResources().getColor(colorId);
    }

    public static Drawable getDrawableById(int id) {
        return _context.getResources().getDrawable(id);
    }

    public static Drawable getDrawableByName(String resourceName) {
        return _context.getResources().getDrawable(getDrawableIdFor(resourceName));
    }

    public static float getDimenById(int id) {
        return _context.getResources().getDimension(id);
    }

    public static int getDimensionPixelSizeById(int id) {
        return _context.getResources().getDimensionPixelSize(id);
    }

    public static Object getSystemServiceOf(String name) {
        return _context.getSystemService(name);
    }

    public static ArrayList<String> getStringArrayById(int id) {
        return new ArrayList<String>(Arrays.asList(getAppContext().getResources().getStringArray(id)));
    }

    public static ArrayList<String> getStringArrayByName(String resourceName) {
        try {
            return getStringArrayById(getArrayIdFor(resourceName));
        } catch (NotFoundException e) {
            if (AppUtils.isDebuggable()) e.printStackTrace();
            return null;
        }
    }

    public static int getIntegerById(int id) {
        try {
            return _context.getResources().getInteger(id);
        } catch (NotFoundException e) {
            LogUtils.w("id not found: " + id);
            return -1;
        }
    }

    public static int getIntegerByName(String resourceName) {
        return getIntegerById(getIntegerIdFor(resourceName));
    }

    /**
     * アプリバージョンに対応した，想定しているAPIバージョンを返します．
     * 
     */
    public static String getTargetApiVersion() {
        String versionName = getAppVersionName();
        if (versionName == null) return null;
        return versionName.split(Pattern.quote("."), 2)[0]; // versionNameがx.y...となっていることを期待し，xを取り出す．
    }

    /**
     * アプリバージョンを返します．
     * 
     */
    public static String getAppVersionName() {
        String versionName = null;
        try {
            PackageInfo packageInfo = getAppContext().getPackageManager().getPackageInfo(getAppContext().getPackageName(), PackageManager.GET_ACTIVITIES);
            versionName = packageInfo.versionName;
        } catch (NameNotFoundException ignore) {
        }
        return versionName;
    }

    /***********************************************
     * Resouce id definition *
     **********************************************/

    public static int getResourceIdFor(Resources resources, String defPackage, String resourceName, ResourceType resourceType) {
        String defType = "notfound";
        switch (resourceType) {
            case LAYOUT:
                defType = "layout";
                break;
            case ID:
                defType = "id";
                break;
            case STRING:
                defType = "string";
                break;
            case INTEGER:
                defType = "integer";
                break;
            case DIMEN:
                defType = "dimen";
                break;
            case COLOR:
                defType = "color";
                break;
            case ARRAY:
                defType = "array";
                break;
            case DRAWABLE:
                defType = "drawable";
                break;
            case MENU:
                defType = "menu";
                break;
            case STYLABLE:
                defType = "styleable";
                break;
            default:
                break;
        }
        int resourceId = resources.getIdentifier(resourceName, defType, defPackage);
        if (resourceId == 0x0) {
            String replacedResourceName = resourceName.replaceAll("_", ".");
            if (replacedResourceName.equals(resourceName)) {
                throw new IllegalArgumentException("R." + defType + "." + resourceName + " in " + defPackage + " is " + resourceId);
            } else {
                return getResourceIdFor(resources, defPackage, replacedResourceName, resourceType);
            }
        }
        return resourceId;
    }

    public enum ResourceType {
        LAYOUT, ID, STRING, INTEGER, DIMEN, COLOR, ARRAY, DRAWABLE, MENU, STYLABLE
    }

    public static int getLayoutIdFor(String resourceName) {
        return getResourceIdFor(_context.getResources(), _context.getPackageName(), resourceName, ResourceType.LAYOUT);
    }

    public static int getIdFor(String resourceName) {
        int id = getResourceIdFor(_context.getResources(), _context.getPackageName(), resourceName, ResourceType.ID);
        if (id == 0x0) {
            throw new IllegalArgumentException("resourceName not found. " + resourceName);
        }
        return id;
    }

    public static int getStringIdFor(String resourceName) {
        return getResourceIdFor(_context.getResources(), _context.getPackageName(), resourceName, ResourceType.STRING);
    }

    public static int getIntegerIdFor(String resourceName) {
        return getResourceIdFor(_context.getResources(), _context.getPackageName(), resourceName, ResourceType.INTEGER);
    }

    public static int getDimenIdFor(String resourceName) {
        return getResourceIdFor(_context.getResources(), _context.getPackageName(), resourceName, ResourceType.DIMEN);
    }

    public static int getColorIdFor(String resourceName) {
        return getResourceIdFor(_context.getResources(), _context.getPackageName(), resourceName, ResourceType.COLOR);
    }

    public static int getArrayIdFor(String resourceName) {
        return getResourceIdFor(_context.getResources(), _context.getPackageName(), resourceName, ResourceType.ARRAY);
    }

    public static int getDrawableIdFor(String resourceName) {
        return getResourceIdFor(_context.getResources(), _context.getPackageName(), resourceName, ResourceType.DRAWABLE);
    }

    public static int getMenuIdFor(String resourceName) {
        return getResourceIdFor(_context.getResources(), _context.getPackageName(), resourceName, ResourceType.MENU);
    }

    public static int getStyleableIdFor(String resourceName) {
        return getResourceIdFor(_context.getResources(), _context.getPackageName(), resourceName, ResourceType.STYLABLE);
    }

    static HttpStack sHurlStack;
    private static RequestQueue sRequestQueue;

    public static HttpStack getHttpStack() {
        if (sHurlStack == null) {
            CookieSyncManager.createInstance(CustomApplication.getAppContext());
            sHurlStack = new HurlStack();
            ;
        }
        return sHurlStack;
    }

    public static RequestQueue getQueue() {
        if (sRequestQueue == null) {
            sRequestQueue = Volley.newRequestQueue(getAppContext(), getHttpStack());
        }
        return sRequestQueue;
    }
}

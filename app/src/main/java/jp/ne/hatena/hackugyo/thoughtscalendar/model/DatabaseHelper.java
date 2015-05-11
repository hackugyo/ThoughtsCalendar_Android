package jp.ne.hatena.hackugyo.thoughtscalendar.model;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import jp.ne.hatena.hackugyo.thoughtscalendar.util.LogUtils;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;


public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    private static final String DATABASE_NAME = "philosocial.db";
    private static final int DATABASE_VERSION = 1;
    private File mDatabasePath;
    private static AtomicInteger sHelperReferenceCount = new AtomicInteger(0);
    private static volatile DatabaseHelper sHelper;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mDatabasePath = context.getDatabasePath(DATABASE_NAME);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource myConnectionSource) {
       
        try {
            // エンティティを指定してcreate tableします
            TableUtils.createTable(myConnectionSource, AttendingEvent.class);
        } catch (java.sql.SQLException e) {
            LogUtils.e("データベースを作成できませんでした", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource myConnectionSource, int oldVersion, int newVersion) {
        // DBのアップグレード処理（今回は割愛）
    }

    /***********************************************
     * get / release helper *
     **********************************************/
    /**
     * {@link OpenHelperManager#getHelper(Context, Class)}のラッパです
     * 
     * @param context
     * @return DBヘルパ
     */
    public static synchronized DatabaseHelper getHelper(Context context) {
        if (sHelperReferenceCount.getAndIncrement() == 0) {
            sHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        }
        return sHelper;
    }

    /**
     * DBヘルパを使い終わったら，{@link #close()}のかわりに呼んでください．
     */
    public static synchronized void releaseHelper() {
        if (sHelperReferenceCount.decrementAndGet() <= 0) {
            sHelperReferenceCount.set(0);
            OpenHelperManager.releaseHelper();
        }
    }

    public static synchronized void destroyHelper() {
        OpenHelperManager.releaseHelper();
        sHelperReferenceCount.set(0);
    }

    /**
     * Close any open connections.
     * 
     * @deprecated DB更新をAtomicにするため，直接closeせず，<br>
     *             {@link #releaseHelper()}を呼んでください．
     */
    public void close() {
        super.close();
    }
}

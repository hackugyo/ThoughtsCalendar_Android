package jp.ne.hatena.hackugyo.thoughtscalendar.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class FileUtils {
    
    private FileUtils() {
        
    }

    public static int deleteAllFiles(String dirPath) {
        int count = 0;

        final File dir = new File(dirPath);
        if (!dir.exists()) {
            LogUtils.w("Not Exist:" + dirPath);
            return 0;
        }
        if (dir.isFile()) {
            dir.delete();
            return 1;
        } else if (dir.isDirectory()) {
            final File[] files = dir.listFiles();
            if (files == null) {
                LogUtils.d("No files at " + dir.getAbsolutePath());
                return 0;
            }
            for (final File file : files) {
                if (!file.delete()) {
                    LogUtils.e("Can't remove " + file.getAbsolutePath());
                } else {
                    count += 1;
                }
            }
        }
        return count;
    }

    /**
     * Creates the specified <code>toFile</code> as a byte for byte copy of the
     * <code>fromFile</code>. If <code>toFile</code> already exists, then it
     * will be replaced with a copy of <code>fromFile</code>. The name and path
     * of <code>toFile</code> will be that of <code>toFile</code>.<br/>
     * <br/>
     * <i> Note: <code>fromFile</code> and <code>toFile</code> will be closed by
     * this function.</i>
     *
     * @param fromFile
     *            - FileInputStream for the file to copy from.
     * @param toFile
     *            - FileInputStream for the file to copy to.
     */
    public static void copyFile(FileInputStream fromFile, FileOutputStream toFile) throws IOException {
        FileChannel fromChannel = null;
        FileChannel toChannel = null;
        try {
            fromChannel = fromFile.getChannel();
            toChannel = toFile.getChannel();
            fromChannel.transferTo(0, fromChannel.size(), toChannel);
        } finally {
            try {
                if (fromChannel != null) {
                    fromChannel.close();
                }
            } finally {
                if (toChannel != null) {
                    toChannel.close();
                }
            }
        }
    }

    /**
     * targetPathがtopDirPathの直下のパスであるかどうかを返します．
     * 
     * @param targetPath
     *            検査対象のパス
     * @param topDirPath
     *            どのパスの直下であってほしいか
     * @return true: 直下<br>
     *         false: 直下でないか，隠し属性である
     */
    public static boolean isDirectlyUnder(String targetPath, String topDirPath) {

        if (targetPath.indexOf(topDirPath) != 0) return false; // topDirPathと関係ないパス
        if (topDirPath.length() == targetPath.length()) return false; // topDirPathそのままのパス

        String snd = targetPath.substring(topDirPath.length() + 1);
        if (snd.indexOf("/") > 0) return false; // 対象はtopDirPathの直下ではない．直下なら"/"は含まれないため-1が返るはず
        if (snd.indexOf(".") == 0) return false; // 隠し属性なら無視

        return true;
    }

}

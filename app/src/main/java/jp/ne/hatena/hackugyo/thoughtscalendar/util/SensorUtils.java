package jp.ne.hatena.hackugyo.thoughtscalendar.util;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import java.util.HashMap;
import java.util.List;

/**
 * センサ関連のUtiです
 * Created by kwatanabe on 15/05/11.
 */
public class SensorUtils {

  private SensorUtils() {

  }
  private static final String[] sSensorLabels = {"accel","atemp","g","gyro","light","laccel","mag","pressure","prox","humid","rot"};
  private static HashMap<String, Integer> sSensorList = new HashMap<String, Integer>() {

    {
      put("accel", Sensor.TYPE_ACCELEROMETER);
      put("atemp", Sensor.TYPE_AMBIENT_TEMPERATURE);
      put("g", Sensor.TYPE_GRAVITY);
      put("gyro", Sensor.TYPE_GYROSCOPE);
      put("light", Sensor.TYPE_LIGHT);
      put("laccel", Sensor.TYPE_LINEAR_ACCELERATION);
      put("mag", Sensor.TYPE_MAGNETIC_FIELD);
      put("pressure", Sensor.TYPE_PRESSURE);
      put("prox", Sensor.TYPE_PROXIMITY);
      put("humid", Sensor.TYPE_RELATIVE_HUMIDITY);
      put("rot", Sensor.TYPE_ROTATION_VECTOR);
    }
  };

  /**
   * どのセンサがサポートされているかをDEBUGログに表示します.
   * @see <a href="http://d.hatena.ne.jp/seinzumtode/20150509/1431159597">参考リンク</a>
   * @param sensorManager (SensorManager) getSystemService(Context.SENSOR_SERVICE)で取得して渡してください．
   */
  public static void logAvailableSensors(SensorManager sensorManager) {
    for (String name : sSensorLabels) {
      try {
        int sensorId = sSensorList.get(name);
        List<Sensor> sensors = sensorManager.getSensorList(sensorId);
        if(sensors.size() > 0){
          LogUtils.d("result","SUPPORTED: " +  name);
        } else {
          LogUtils.d("result", "N/A: " + name);
        }
      } catch (RuntimeException e) {
        e.printStackTrace();
      }
    }
  }
}

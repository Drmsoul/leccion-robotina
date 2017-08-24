package soul.leccionrafael;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity  implements SensorEventListener,DownloadResultReceiver.Receiver {


    private SensorManager senSensorManager;
    private Sensor senAccelerometer;


    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 600;
    private DownloadResultReceiver mReceiver;
    final String url = "https://jsonplaceholder.typicode.com/posts";

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);
        super.onCreate(savedInstanceState);




        setContentView(R.layout.activity_main);

        mReceiver = new DownloadResultReceiver(new Handler());
        mReceiver.setReceiver(this);
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, mService.class);

/* Send optional extras to Download IntentService */
        intent.putExtra("url", url);
        intent.putExtra("receiver", mReceiver);
        intent.putExtra("requestId", 101);

        startService(intent);


    }
    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }


    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case mService.STATUS_RUNNING:

                setProgressBarIndeterminateVisibility(true);
                break;
            case mService.STATUS_FINISHED:
                /* Hide progress & extract result from bundle */
                setProgressBarIndeterminateVisibility(false);

                String[] results = resultData.getStringArray("result");

                /* Update ListView with result */

                Log.e("@@@yum","totally should work" + results[0] + " bytes");
                arrayAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, results);
                Log.e("@@@yum","totally should work" + arrayAdapter + " bytes");
                listView.setAdapter(arrayAdapter);


                break;
            case mService.STATUS_ERROR:
                /* Handle the error */
                String error = resultData.getString(Intent.EXTRA_TEXT);
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                break;
        }
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];
            long curTime = System.currentTimeMillis();


            TextView t1 = (TextView)this.findViewById(R.id.text1);
            t1.setText("val x:"+ x + " Val Y:" + y + " Val Z:"+ z);
            ;
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}

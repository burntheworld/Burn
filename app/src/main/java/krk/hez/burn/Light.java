package krk.hez.burn;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.Random;


public class Light extends Activity {
    private static Camera cam;
    private View[] views;
    private Thread mors;
    private Thread screen;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        Tracker t = ((AnalyticsApp)getApplication()).getTracker();
        t.send(new HitBuilders.ScreenViewBuilder().set("screen","Flare").build());
        stop = false;
        setBrightness();
        setContentView(R.layout.light);
        views = new View[6];
        views[0] = findViewById(R.id.l1);
        views[1] = findViewById(R.id.l2);
        views[2] = findViewById(R.id.l3);
        views[3] = findViewById(R.id.l4);
        views[4] = findViewById(R.id.l5);
        views[5] = findViewById(R.id.l6);
        boolean hasFlash = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        if (hasFlash) {
            mors = new Thread(new Runnable() {
                @Override
                public void run() {
                    cam = Camera.open();
                    Camera.Parameters p = cam.getParameters();
                    //-.- .. .-.. .-.. / .- .-.. .-.. / .... ..- -- .- -. ...#kill all humans
                    int[] morse = new int[]{
                            //k
                            3, -1, 1, -1, 3,
                            -3,
                            //i
                            1, -1, 1,
                            -3,
                            //l
                            1, -1, 3, -1, 1, -1, 1,
                            -3,
                            //l
                            1, -1, 3, -1, 1, -1, 1,
                            -7,
                            //a
                            1, -1, 3,
                            -3,
                            //l
                            1, -1, 3, -1, 1, -1, 1,
                            -3,
                            //l
                            1, -1, 3, -1, 1, -1, 1,
                            -7,
                            //h
                            1, -1, 1, -1, 1, -1, 1,
                            -3,
                            //u
                            1, -1, 1, -1, 3,
                            -3,
                            //m
                            3, -1, 3,
                            -3,
                            //a
                            1, -1, 3,
                            -3,
                            //n
                            3, -1, 1,
                            -3,
                            //s
                            1, -1, 1, -1, 1};
                    int sum = 0;
                    for (int mor : morse) {
                        sum += Math.abs(mor);
                    }
                    int unit = Global.TIME_FOR_FLARE / sum;
                    for (int a = 0; !stop && a < morse.length; a++) {
                        if (morse[a] > 0)
                            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                        else
                            p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        if (!stop)
                            cam.setParameters(p);
                        try {
                            Thread.sleep(unit * Math.abs(morse[a]));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (!stop && !screen.isAlive()) finish();
                }
            });
            mors.start();
        }
        screen = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Random r = new Random();
                    long w = 1000 / 18;
                    while (!stop && mors.isAlive()) {
                        int red = r.nextInt(256);
                        int green = r.nextInt(256);
                        int blue = r.nextInt(256);
                        int add = Math.min(Math.min(255 - red, 255 - green), 255 - blue);
                        if (red >= green && red >= blue)
                            red += add;
                        else if (green >= blue && green >= red)
                            green += add;
                        else
                            blue += add;
                        final int BLACK = Color.argb(0xFF, red, green, blue);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!stop)
                                    views[r.nextInt(6)].setBackgroundColor(BLACK);
                            }
                        });
                        Thread.sleep(w);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!stop && !mors.isAlive()) finish();
            }
        });
        screen.start();
    }

    private boolean stop;

    @Override
    public void onPause() {
        super.onPause();
        stop = true;
        cam.release();
        finish();
    }

    @Override
    public void finish() {
        resetBrightness();
        super.finish();
    }

    private int prev = 0;
    private int auto = 0;

    private void setBrightness() {
        try {
            prev = Settings.System.getInt(this.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        try {
            auto = Settings.System.getInt(this.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        Settings.System.putInt(this.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS, 255);
        Settings.System.putInt(this.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
    }

    private void resetBrightness() {
        Settings.System.putInt(this.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS, prev);
        Settings.System.putInt(this.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE, auto);
    }
}

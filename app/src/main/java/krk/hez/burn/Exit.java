package krk.hez.burn;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;


public class Exit extends Activity {
    public static final int NOTIF_ID = 765;
    public static MapsActivity main;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tracker t = ((AnalyticsApp)getApplication()).getTracker();
        t.send(new HitBuilders.ScreenViewBuilder().set("screen","Exit").build());
        setContentView(R.layout.exit_dlg);
        findViewById(R.id.o).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelNotif();
                Global.stop = true;
                startService(new Intent(Exit.this, UpdateLoc.class));
                if (main != null) {
                    main.finish();
                    main = null;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new DefaultHttpClient().execute(new HttpGet(Global.domain+"?exit=0&id=" + Global.getID(Exit.this)));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                finish();
            }
        });
        findViewById(R.id.c).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    private void cancelNotif(){
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(NOTIF_ID);
    }
}

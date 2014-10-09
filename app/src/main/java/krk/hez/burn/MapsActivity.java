package krk.hez.burn;


import android.app.DialogFragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MapsActivity extends FragmentActivity {
    private GoogleMap mMap;
    private GoogleCloudMessaging gcm;
    private final BroadcastReceiver bReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Global.rcv1)) {
                String action = intent.getStringExtra("act");
                if (action.equals("accept")) {
                    cancelDialog();
                    Toast.makeText(MapsActivity.this, R.string.toast_flare_accepted, Toast.LENGTH_SHORT).show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1700);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            startActivity(new Intent(MapsActivity.this, Light.class));
                        }
                    }).start();
                } else if (action.equals("decline")) {
                    cancelDialog();
                    Toast.makeText(MapsActivity.this, R.string.toast_flare_declined, Toast.LENGTH_SHORT).show();
                }
            }
        }
    };
    private final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Global.rcv)) {
                if (mMap != null)
                    update();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tracker t = ((AnalyticsApp)getApplication()).getTracker();
        t.send(new HitBuilders.ScreenViewBuilder().set("screen","main screen").build());
        setContentView(R.layout.activity_maps);
        MyMarker.initMarkers();
        setUpMapIfNeeded();
        Exit.main = this;
        Global.stop = false;
        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Global.rcv);
        IntentFilter intentFilter1 = new IntentFilter();
        intentFilter1.addAction(Global.rcv1);
        bManager.registerReceiver(bReceiver, intentFilter);
        bManager.registerReceiver(bReceiver1, intentFilter1);
        startService(new Intent(this, UpdateLoc.class));
        getActionBar().hide();
        String s = PreferenceManager.getDefaultSharedPreferences(this).getString("status", getString(R.string.default_status));
        ((Button) findViewById(R.id.status_title)).setText(s);
        ((ImageButton) findViewById(R.id.icon)).setImageBitmap(Global.fromIndex(this, PreferenceManager.getDefaultSharedPreferences(this).getInt("icon", 0)));
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String g = PreferenceManager.getDefaultSharedPreferences(MapsActivity.this).getString("regid", null);
                    if (g == null) {
                        if (gcm == null) {
                            gcm = GoogleCloudMessaging.getInstance(MapsActivity.this);
                        }
                        g = gcm.register(Kk.sender_id);
                        PreferenceManager.getDefaultSharedPreferences(MapsActivity.this).edit().putString("regid", g).apply();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cancelNotif();
        if (UpdateLoc.mLocationRequest != null)
            UpdateLoc.mLocationRequest.setFastestInterval(Global.FOREGROUND_WAIT);
        MyMarker.initMarkers();
        setUpMapIfNeeded();
    }

    @Override
    public void onNewIntent(final Intent intent) {
        final int index = intent.getIntExtra("index", -1);
        if (index != -1) {
            ((NotificationManager) MapsActivity.this.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(index);
            MyMarker myMarker = MyMarker.find(index);
            if(myMarker==null){
                update();
                myMarker = MyMarker.find(index);
            }
            final String poke = intent.getStringExtra("poke");
            if (myMarker != null) {
                final MyMarker mym = myMarker;
                mMap.animateCamera(CameraUpdateFactory.zoomTo(12), new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        anim();
                    }

                    @Override
                    public void onCancel() {
                        anim();
                    }

                    private void anim() {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mym.latLng, 14), new GoogleMap.CancelableCallback() {
                            @Override
                            public void onFinish() {
                                sho();
                            }

                            @Override
                            public void onCancel() {
                                sho();
                            }

                            private void sho() {
                                if (poke != null) {
                                    mym.marker.setTitle(poke);
                                    mym.marker.showInfoWindow();
                                } else {
                                    FlareDialog.Show(getFragmentManager(),index);
                                }
                            }
                        });
                    }
                });

            }else{
                Toast.makeText(this,R.string.user_not_found,Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (UpdateLoc.mLocationRequest != null)
            UpdateLoc.mLocationRequest.setFastestInterval(Global.BACKGROUND_WAIT);
        sendNotify();
    }

    private void cancelNotif() {
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(765);
    }

    private void sendNotify() {
        Intent resultIntent = new Intent(this, Exit.class);
        Intent resultIntent1 = new Intent(this, MapsActivity.class);
        PendingIntent rec =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent1,
                        PendingIntent.FLAG_CANCEL_CURRENT
                );
        PendingIntent exit =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT
                );
        String s = PreferenceManager.getDefaultSharedPreferences(this).getString("status", getString(R.string.default_status));
        String st = "Your Status: ";
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.logo_)
                        .setContentTitle(getString(R.string.kipod_working_notification)).setOngoing(true)
                        .setContentText(s.length() == 0 ? st + getString(R.string.empty_status_notification) : st + s)
                        .setContentIntent(rec)
                        .addAction(new NotificationCompat.Action(R.drawable.rsz_kipidead, getString(R.string.exit_action_notification), exit));
        Notification n = mBuilder.build();
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(Exit.NOTIF_ID, n);
    }

    public void exit(View v) {
        startActivity(new Intent(this, Exit.class));
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(final Marker marker) {
                        UserDialog.Show(getFragmentManager(), marker);
                        return false;
                    }
                });
                mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        if (getIntent().getIntExtra("index", -1) != -1) {
                            onNewIntent(getIntent());
                        }
                    }
                });
                mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(final LatLng latLng) {
                        LongClickMenu.Show(getFragmentManager(), latLng);
                    }
                });
                if (Global.js != null)
                    update();
            }
        }
    }

    @Override
    public void onDestroy() {
        mMap = null;
        MyMarker.destroy();
        super.onDestroy();
    }

    public void setStatus(View v) {
        StatusDlg.Show(getFragmentManager());
    }

    public void setIcon(View v) {
        IconDlg.Show(getFragmentManager());
    }

    private void update() {
        try {
            JSONArray jsonArray0 = new JSONArray((String) Global.js.get(0));
            int myIndex = Integer.valueOf((String) jsonArray0.get(0));
            int poks = Integer.valueOf((String) jsonArray0.get(1));
            for (int a = 1; a < Global.js.length(); a++) {
                try {
                    JSONArray jsonArray = new JSONArray((String) Global.js.get(a));
                    int index = Integer.valueOf((String) jsonArray.get(0));
                    if (index != myIndex) {
                        MyMarker m = MyMarker.init(index, Double.valueOf((String) jsonArray.get(1)), Double.valueOf((String) jsonArray.get(2)), (String) jsonArray.get(3), Integer.valueOf((String) jsonArray.get(4)));
                        if(jsonArray.length()>5){
                            m.isEvent = true;
                            DateFormat df = new SimpleDateFormat("yyyy-mm-dd kk:mm:ss", Locale.ENGLISH);
                            m.dateBegin = df.parse((String) jsonArray.get(6));
                            m.dateEnd = df.parse((String) jsonArray.get(5));
                            EventDialog.tryToUpdate(this,index);
                        }else{
                            UserDialog.tryToUpdate(this,index);
                        }
                        addToMap(this, m);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            MyMarker.clean();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void cancelDialog() {
        DialogFragment df = (DialogFragment) getFragmentManager().findFragmentByTag("user");
        if (df == null)
            df = (DialogFragment) getFragmentManager().findFragmentByTag("event");
        if (df != null) {
            df.getDialog().cancel();
        }
    }

    private void addToMap(Context context, MyMarker mym) {
        if (!MyMarker.contains(mym)) {
            Bitmap b = Global.fromIndex(context, mym.icon);
            MarkerOptions mo = new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(b))
                    .position(mym.latLng);
            mym.marker = mMap.addMarker(mo);
            MyMarker.add(mym);
        } else {
            mym.marker.setPosition(mym.latLng);
            Bitmap b = Global.fromIndex(context, mym.icon);
            mym.marker.setIcon(BitmapDescriptorFactory.fromBitmap(b));
        }
    }
}


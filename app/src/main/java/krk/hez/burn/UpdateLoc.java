package krk.hez.burn;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class UpdateLoc extends Service implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationListener {
    private LocationClient mLocationClient;
    public static LocationRequest mLocationRequest;

    public UpdateLoc() {
    }

    private void exit() {
        mLocationClient.disconnect();
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flag, int startId) {
        if (Global.stop) {
            exit();
            return 0;
        }
        if(mLocationClient==null)
            mLocationClient = new LocationClient(this, this, this);
        if(mLocationRequest==null) {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setFastestInterval(Global.FOREGROUND_WAIT);
        }
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if(!mLocationClient.isConnected()&&!mLocationClient.isConnecting())
            mLocationClient.connect();
        return super.onStartCommand(intent, flag, startId);
    }

    @Override
    public void onLocationChanged(Location location) {
        Global.mCurrentLocation = location;
        new Thread(new Runnable() {
            @Override
            public void run() {
                connectToServer();
                if (mLocationRequest.getFastestInterval()==Global.FOREGROUND_WAIT) {
                    Intent RTReturn = new Intent(Global.rcv);
                    LocalBroadcastManager.getInstance(UpdateLoc.this).sendBroadcast(RTReturn);
                }
            }
        }).start();
    }

    @Override
    public void onConnected(Bundle dataBundle) {
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
    }

    @Override
    public void onDisconnected() {
        Toast.makeText(this, "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //todo something about the error showErrorDialog(connectionResult.getErrorCode());}
    }

    private void connectToServer() {
        String stat = PreferenceManager.getDefaultSharedPreferences(this).getString("status", "status");
        try {
            stat = URLEncoder.encode(stat, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        int icon = PreferenceManager.getDefaultSharedPreferences(this).getInt("icon", 0);
        try {
            HttpResponse h = new DefaultHttpClient().execute(new HttpGet(Global.domain + "?id=" + Global.getID(this) + "&lat=" + Global.mCurrentLocation.getLatitude() + "&lng=" + Global.mCurrentLocation.getLongitude() + "&stat=" + stat + "&icon=" + icon + "&regid=" + PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("regid", "")));
            if (h.getStatusLine().getStatusCode() == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        h.getEntity().getContent()));
                StringBuilder sb = new StringBuilder();
                String l;
                while ((l = in.readLine()) != null) {
                    sb.append(l);
                }
                in.close();
                Global.js = new JSONArray(sb.toString());
            } else {
                Log.e("httpcode", "" + h.getStatusLine().getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package krk.hez.burn;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.provider.Settings;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONArray;

import java.text.DecimalFormat;

class Global {
    public final static int DIST = 300;
    public final static int BACKGROUND_WAIT = 300000;//5 MINUTE BETWEEN REFRESH
    public final static int FOREGROUND_WAIT = 5000;//5 SECOND BETWEEN REFRESH
    public static final int TIME_FOR_FLARE = 7000;
    public static final String domain ="http://146.148.116.247/";
    public static JSONArray js;
    public static Location mCurrentLocation;
    public static final String rcv = "hez.krk.rcv";
    public static final String rcv1 = "hez.krk.rcv1";
    public static int icon;
    public static boolean stop = false;
    public static final Integer[] mThumbIds = {
            R.drawable.porcupic1, R.drawable.new_1,
            R.drawable.porcupic2, R.drawable.new_2,
            R.drawable.porcupic3, R.drawable.new_3,
            R.drawable.porcupic4, R.drawable.new_4,
            R.drawable.porcupic5, R.drawable.new_5,
            R.drawable.porcupic6, R.drawable.new_6,
            R.drawable.porcupic7, R.drawable.new_7,
            R.drawable.porcupic8, R.drawable.new_8,
            R.drawable.porcupic9, R.drawable.new_9,
            R.drawable.porcupic10, R.drawable.new_10,
            R.drawable.porcupic11, R.drawable.new_11,
            R.drawable.porcupic12, R.drawable.new_12,
            R.drawable.porcupic13, R.drawable.new_13,
            R.drawable.porcupic14, R.drawable.new_14
    };
    public static Bitmap fromIndex(Context context,int icon) {
        Bitmap b;
        switch (icon) {
            case -6:
                b = BitmapFactory.decodeResource(context.getResources(),R.drawable.party);
                break;
            case 1:
                b = BitmapFactory.decodeResource(context.getResources(), mThumbIds[0]);
                break;
            case 2:
                b = BitmapFactory.decodeResource(context.getResources(), mThumbIds[1]);
                break;
            case 3:
                b = BitmapFactory.decodeResource(context.getResources(), mThumbIds[2]);
                break;
            case 4:
                b = BitmapFactory.decodeResource(context.getResources(), mThumbIds[3]);
                break;
            case 5:
                b = BitmapFactory.decodeResource(context.getResources(), mThumbIds[4]);
                break;
            case 6:
                b = BitmapFactory.decodeResource(context.getResources(), mThumbIds[5]);
                break;
            case 7:
                b = BitmapFactory.decodeResource(context.getResources(), mThumbIds[6]);
                break;
            case 8:
                b = BitmapFactory.decodeResource(context.getResources(), mThumbIds[7]);
                break;
            case 9:
                b = BitmapFactory.decodeResource(context.getResources(), mThumbIds[8]);
                break;
            case 10:
                b = BitmapFactory.decodeResource(context.getResources(), mThumbIds[9]);
                break;
            case 11:
                b = BitmapFactory.decodeResource(context.getResources(), mThumbIds[10]);
                break;
            case 12:
                b = BitmapFactory.decodeResource(context.getResources(), mThumbIds[11]);
                break;
            case 13:
                b = BitmapFactory.decodeResource(context.getResources(), mThumbIds[12]);
                break;
            case 14:
                b = BitmapFactory.decodeResource(context.getResources(), mThumbIds[13]);
                break;
            case 15:
                b = BitmapFactory.decodeResource(context.getResources(), mThumbIds[14]);
                break;
            case 16:
                b = BitmapFactory.decodeResource(context.getResources(), mThumbIds[15]);
                break;
            case 17:
                b = BitmapFactory.decodeResource(context.getResources(), mThumbIds[16]);
                break;
            case 18:
                b = BitmapFactory.decodeResource(context.getResources(), mThumbIds[17]);
                break;
            case 19:
                b = BitmapFactory.decodeResource(context.getResources(), mThumbIds[18]);
                break;
            case 20:
                b = BitmapFactory.decodeResource(context.getResources(), mThumbIds[19]);
                break;
            case 21:
                b = BitmapFactory.decodeResource(context.getResources(), mThumbIds[20]);
                break;
            case 22:
                b = BitmapFactory.decodeResource(context.getResources(), mThumbIds[21]);
                break;
            case 23:
                b = BitmapFactory.decodeResource(context.getResources(), mThumbIds[22]);
                break;
            case 24:
                b = BitmapFactory.decodeResource(context.getResources(), mThumbIds[23]);
                break;
            case 25:
                b = BitmapFactory.decodeResource(context.getResources(), mThumbIds[24]);
                break;
            case 26:
                b = BitmapFactory.decodeResource(context.getResources(), mThumbIds[25]);
                break;
            case 27:
                b = BitmapFactory.decodeResource(context.getResources(), mThumbIds[26]);
                break;
            case 28:
                b = BitmapFactory.decodeResource(context.getResources(), mThumbIds[27]);
                break;
            default:
                b = BitmapFactory.decodeResource(context.getResources(), R.drawable.porcupic1);
        }
        return b;
    }
    public static String getID(Context context) {
        return Settings.Secure.getString(
                context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
    public static String formatDistance(float f){
        DecimalFormat dfr;
        if(f>=1000000){
            dfr = new DecimalFormat("#.0");
            return dfr.format(f/1000000)+"\nKKM";
        }
        if(f>=10000){
            dfr = new DecimalFormat("#");
            return dfr.format(f/1000)+"\nKM";
        }
        if(f>=1000){
            dfr = new DecimalFormat("#.00");
            return dfr.format(f/1000)+"\nKM";
        }
        if(f>=1000){
            dfr = new DecimalFormat("#.00");
            return dfr.format(f/1000)+"\nKM";
        }
        dfr = new DecimalFormat("#.00");
        return dfr.format(f)+"\nmeter";
    }

    public static float getDis(Marker marker){
        float[] results = new float[2];
        Location.distanceBetween(marker.getPosition().latitude, marker.getPosition().longitude,
                mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), results);
        return results[0];
    }
    public static void navigate(Context context,LatLng latLng){
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?daddr=" + latLng.latitude + "," + latLng.longitude));
        context.startActivity(intent);
    }
}

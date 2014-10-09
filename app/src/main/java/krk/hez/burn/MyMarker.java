package krk.hez.burn;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;

public class MyMarker {
    private static ArrayList<MyMarker> markers;
    Marker marker;
    final int index;
    boolean isEvent;
    Date dateEnd,dateBegin;
    LatLng latLng;
    int icon;
    String title;
    public static void initMarkers(){
        if (markers == null)
            markers = new ArrayList<MyMarker>();
    }
    public static MyMarker init(int index, double x, double y, String title, int icon) {
        MyMarker m = find(index);
        if (m != null) {
            m.latLng = new LatLng(x, y);
            m.title = title;
            m.icon = icon;
            return m;
        }
        return new MyMarker(index, x, y, title, icon);
    }

    private MyMarker(int index, double x, double y, String title, int icon) {
        this.index = index;
        try {
            this.title = URLDecoder.decode(title, "utf-8");
        } catch (UnsupportedEncodingException e) {
            this.title = title;
        }
        this.icon = icon;
        latLng = new LatLng(x, y);
    }
    public static void add(MyMarker mym){
        markers.add(mym);
    }
    public static boolean contains(MyMarker myMarker){
        return markers.contains(myMarker);
    }
    public static MyMarker find(int index) {
        for (MyMarker marker1 : markers) {
            if (marker1.index == index) {
                return marker1;
            }
        }
        return null;
    }
    public static void destroy(){
        markers = null;
    }

    static MyMarker find(Marker marker) {
        for (MyMarker mrk : markers) {
            if (mrk.marker.equals(marker)) {
                return mrk;
            }
        }
        return null;
    }

    public static void clean() {
        boolean found = false;
        for (int a = 0; a < markers.size(); a++) {
            for (int b = 1; b < Global.js.length(); b++) {
                try {
                    JSONArray jsonArray = new JSONArray((String) Global.js.get(b));
                    int index = Integer.valueOf((String) jsonArray.get(0));
                    if (index == markers.get(a).index) {
                        found = true;
                        break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (!found) {
                markers.get(a).marker.remove();
                markers.remove(a);
            }
            found = false;
        }
    }
}

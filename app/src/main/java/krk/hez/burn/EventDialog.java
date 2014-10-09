package krk.hez.burn;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class EventDialog extends DialogFragment {
    private static MyMarker myMarker;
    public static void Show(FragmentManager fm,MyMarker myMarker){
        EventDialog.myMarker = myMarker;
        new EventDialog().show(fm, "event");
    }
    public EventDialog(){
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Tracker t = ((AnalyticsApp)getActivity().getApplication()).getTracker();
        t.send(new HitBuilders.ScreenViewBuilder().set("screen","Show Event").build());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.event_dlg, null);
        ((TextView)v.findViewById(R.id.flare_dis)).setText(Global.formatDistance(Global.getDis(myMarker.marker))+"");
        ((TextView)v.findViewById(R.id.status)).setText(myMarker.title);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        Calendar c = Calendar.getInstance();
        int offset = c.getTimeZone().getOffset(System.currentTimeMillis());
        ((TextView) v.findViewById(R.id.finish)).setText(sdf.format(myMarker.dateEnd.getTime()+offset));
        ((TextView)v.findViewById(R.id.start)).setText(sdf.format(myMarker.dateBegin.getTime()+offset));
        v.findViewById(R.id.nev).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().cancel();
                Global.navigate(getActivity(),myMarker.latLng);
            }
        });
        v.findViewById(R.id.flare_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View vv) {
                if (Global.getDis(myMarker.marker) > Global.DIST && myMarker.index != 7) {
                    Toast.makeText(getActivity(), R.string.too_far_for_flare, Toast.LENGTH_SHORT).show();
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String url = Global.domain + "gcm.php?id=" + Global.getID(getActivity()) + "&type=eflare&lat=" + Global.mCurrentLocation.getLatitude() + "&lng=" + Global.mCurrentLocation.getLongitude();
                                final HttpResponse r = new DefaultHttpClient().execute(new HttpGet(url));
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getActivity(), R.string.toast_flare_sent, Toast.LENGTH_SHORT).show();
                                        getDialog().cancel();
                                    }
                                });
                                if (r.getStatusLine().getStatusCode() == 200) {
                                    BufferedReader in = new BufferedReader(new InputStreamReader(
                                            r.getEntity().getContent()));
                                    StringBuilder sb = new StringBuilder();
                                    String l;
                                    while ((l = in.readLine()) != null) {
                                        sb.append(l);
                                    }
                                    in.close();
                                    try {
                                        int i = Integer.valueOf(sb.toString());
                                        if (i == 0) {
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(getActivity(), R.string.toast_no_user_in_event_to_flare, Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getActivity(), getString(R.string.net_error) + r.getStatusLine().getStatusCode(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            }
        });
        builder.setView(v);
        return builder.create();
    }

    public static void tryToUpdate(Activity context,int index){
        if(myMarker==null)return;
        if(myMarker.index!=index)return;
        EventDialog ed = (EventDialog) context.getFragmentManager().findFragmentByTag("event");
        if(ed==null)return;
        if(!ed.isVisible())return;
        if(!ed.getDialog().isShowing())return;
        TextView tv = (TextView)ed.getDialog().findViewById(R.id.flare_dis);
        if(tv==null)return;
        tv.setText(Global.formatDistance(Global.getDis(myMarker.marker)));
    }
}

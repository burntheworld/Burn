package krk.hez.burn;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.model.LatLng;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Calendar;


public class CreateEventDialog extends DialogFragment {
    private static LatLng latLng;
    public static void Show(FragmentManager fm,LatLng latLng){
        CreateEventDialog.latLng = latLng;
        new CreateEventDialog().show(fm, "");
    }
    public CreateEventDialog(){
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Tracker t = ((AnalyticsApp)getActivity().getApplication()).getTracker();
        t.send(new HitBuilders.ScreenViewBuilder().set("screen","Create Event").build());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dd = inflater.inflate(R.layout.event, null);
        final Button tp = (Button) dd.findViewById(R.id.time);
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        c.set(Calendar.HOUR_OF_DAY,hour+3);
        hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        tp.setText(hour+" : "+minute);
        tp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] s = tp.getText().toString().split(" : ");
                TimePickerDialog tpd = new TimePickerDialog(getActivity(),new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        tp.setText(hourOfDay+" : "+minute);
                    }
                },Integer.parseInt(s[0]),Integer.parseInt(s[1]),true);
                tpd.show();
            }
        });
        dd.findViewById(R.id.ok1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String desc = URLEncoder.encode(((EditText) getDialog().findViewById(R.id.desc)).getText().toString(), "utf-8");
                            Calendar c = Calendar.getInstance();
                            int timeH = Integer.parseInt(tp.getText().toString().split(" : ")[0]) - c.get(Calendar.HOUR_OF_DAY);
                            new DefaultHttpClient().execute(new HttpGet(Global.domain + "?event=0&id=" + Global.getID(getActivity()) + "event&stat=" + desc + "&lat=" + latLng.latitude + "&lng=" + latLng.longitude + "&timeH=" + timeH + "&timeM=" + Integer.parseInt(tp.getText().toString().split(" : ")[1])));
                            getDialog().cancel();
                        } catch (IOException e) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(), "Network error", Toast.LENGTH_SHORT).show();
                                }
                            });
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
        dd.findViewById(R.id.no1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().cancel();
            }
        });
        builder.setView(dd);
        return builder.create();
    }
}

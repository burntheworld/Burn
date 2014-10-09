package krk.hez.burn;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;


public class FlareDialog extends DialogFragment {
    private static int index;
    public static void Show(FragmentManager fm,int index) {
        FlareDialog.index = index;
         new FlareDialog().show(fm, "");
    }

    public FlareDialog() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Tracker t = ((AnalyticsApp)getActivity().getApplication()).getTracker();
        t.send(new HitBuilders.ScreenViewBuilder().set("screen","Flare Request").build());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.flare_dlg, null);
        setCancelable(false);
        v.findViewById(R.id.no).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new DefaultHttpClient().execute(new HttpGet(Global.domain + "gcm.php?id=" + Global.getID(getActivity()) + "&type=decline&i=" + index));
                            getDialog().cancel();
                        } catch (IOException e) {
                            e.printStackTrace();
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(), R.string.net_error, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).start();
            }
        });
        v.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            HttpGet hg = new HttpGet(Global.domain + "gcm.php?id=" + Global.getID(getActivity()) + "&type=accept&i=" + index);
                            new DefaultHttpClient().execute(hg);
                            Intent i = new Intent(getActivity(), Light.class);
                            getDialog().cancel();
                            startActivity(i);
                        } catch (IOException e) {
                            e.printStackTrace();
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(), R.string.net_error, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).start();
            }
        });
        builder.setView(v);
        return builder.create();
    }
}

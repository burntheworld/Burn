package krk.hez.burn;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.model.Marker;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public class UserDialog extends DialogFragment {
    private static MyMarker m;
    public static void Show(FragmentManager fm,Marker marker) {
        m = MyMarker.find(marker);
        if(m.isEvent)
            EventDialog.Show(fm,m);
        else
            new UserDialog().show(fm, "user");
    }

    public UserDialog() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Tracker t = ((AnalyticsApp)getActivity().getApplication()).getTracker();
        t.send(new HitBuilders.ScreenViewBuilder().set("screen","User screen").build());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.burner, null);
        ((ImageView) v.findViewById(R.id.icon1)).setImageBitmap(Global.fromIndex(getActivity(), m.icon));
        ((TextView) v.findViewById(R.id.status)).setText("status:\n" + m.title);
        ((TextView)v.findViewById(R.id.flare_dis)).setText(Global.formatDistance(Global.getDis(m.marker))+"");
        v.findViewById(R.id.nev).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().cancel();
                Global.navigate(getActivity(),m.latLng);
            }
        });
        v.findViewById(R.id.poke_sent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View vv) {
                final Dialog pokeChoose = new Dialog(getActivity());
                ListView lv = new ListView(getActivity());
                final String[] poks = getResources().getStringArray(R.array.poks);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, poks);
                lv.setAdapter(adapter);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        vv.setEnabled(false);
                        Toast.makeText(getActivity(), R.string.toast_poke_sent, Toast.LENGTH_SHORT).show();
                        String emoj;
                        try {
                            emoj = URLEncoder.encode(poks[position], "utf-8");
                        } catch (UnsupportedEncodingException e) {
                            emoj = poks[position];
                        }
                        final String url = Global.domain + "gcm.php?id=" + Global.getID(getActivity()) + "&type=poke&i=" + m.index + "&m=" + emoj;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    new DefaultHttpClient().execute(new HttpGet(url));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                        pokeChoose.cancel();
                        getDialog().cancel();
                    }
                });
                pokeChoose.setContentView(lv);
                pokeChoose.setTitle(getString(R.string.choose_poke_title));
                pokeChoose.show();
            }
        });
        v.findViewById(R.id.flare_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Global.getDis(m.marker) > Global.DIST&&m.index!=7) {
                    Toast.makeText(getActivity(), R.string.too_far_for_flare, Toast.LENGTH_SHORT).show();
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String url = Global.domain + "gcm.php?id=" + Global.getID(getActivity()) + "&type=flare&i=" + m.index;
                                new DefaultHttpClient().execute(new HttpGet(url));
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getActivity(), R.string.toast_flare_sent, Toast.LENGTH_SHORT).show();
                                        getDialog().cancel();
                                    }
                                });
                            } catch (IOException e) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getActivity(), R.string.net_error, Toast.LENGTH_SHORT).show();
                                    }
                                });
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
        if(m==null)return;
        if(m.index!=index)return;
        UserDialog ed = (UserDialog) context.getFragmentManager().findFragmentByTag("user");
        if(ed==null)return;
        if(!ed.isVisible())return;
        if(!ed.getDialog().isShowing())return;
        TextView tv = (TextView)ed.getDialog().findViewById(R.id.flare_dis);
        if(tv==null)return;
        tv.setText(Global.formatDistance(Global.getDis(m.marker)));
    }
}

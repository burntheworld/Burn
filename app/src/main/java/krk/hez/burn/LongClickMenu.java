package krk.hez.burn;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.model.LatLng;


public class LongClickMenu extends DialogFragment {
    private static LatLng latLng;
    public static void Show(FragmentManager fm,LatLng latLng){
        LongClickMenu.latLng = latLng;
        new LongClickMenu().show(fm, "");
    }
    public LongClickMenu(){
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Tracker t = ((AnalyticsApp)getActivity().getApplication()).getTracker();
        t.send(new HitBuilders.ScreenViewBuilder().set("screen","LongClickMenu").build());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.long_click_menu, null);
        v.findViewById(R.id.create_event).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().cancel();
                CreateEventDialog.Show(getFragmentManager(),latLng);
            }
        });
        v.findViewById(R.id.nev).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().cancel();
                Global.navigate(getActivity(),latLng);
            }
        });
        builder.setView(v);
        return builder.create();
    }
}

package krk.hez.burn;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;


public class StatusDlg extends DialogFragment {
    public static void Show(FragmentManager fm){
        new StatusDlg().show(fm, "");
    }
    public StatusDlg(){
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Tracker t = ((AnalyticsApp)getActivity().getApplication()).getTracker();
        t.send(new HitBuilders.ScreenViewBuilder().set("screen","set status").build());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dlg, null);
        v.findViewById(R.id.c).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().cancel();
            }
        });
        v.findViewById(R.id.o).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = ((EditText) getDialog().findViewById(R.id.status)).getText().toString();
                ((Button) getActivity().findViewById(R.id.status_title)).setText(s);
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putString("status", s).apply();
                getDialog().cancel();
            }
        });
        builder.setView(v);
        return builder.create();
    }
}

package krk.hez.burn;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;


public class IconDlg extends DialogFragment {
    public static void Show(FragmentManager fm){
        new IconDlg().show(fm, "");
    }
    public IconDlg(){
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Tracker t = ((AnalyticsApp)getActivity().getApplication()).getTracker();
        t.send(new HitBuilders.ScreenViewBuilder().set("screen","set icon").build());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dlg2, null);
        GridView gv = (GridView) v.findViewById(R.id.gridview);
        gv.setAdapter(new ImageAdapter(getActivity()));
        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Global.icon = position+1;
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putInt("icon", Global.icon).apply();
                ((ImageButton) getActivity().findViewById(R.id.icon)).setImageBitmap(Global.fromIndex(getActivity(), Global.icon));
                getDialog().cancel();
            }
        });
        builder.setView(v);
        return builder.create();
    }
    public class ImageAdapter extends BaseAdapter {
        private final Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return Global.mThumbIds.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null||convertView.getHeight()==0) {  // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
                int w = parent.getWidth();
                imageView.setLayoutParams(new GridView.LayoutParams(w/4, w/4));
                imageView.setScaleType(ImageButton.ScaleType.FIT_CENTER);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }
            imageView.setImageResource(Global.mThumbIds[position]);
            return imageView;
        }

        // references to our images
    }
}

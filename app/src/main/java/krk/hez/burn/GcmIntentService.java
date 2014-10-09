package krk.hez.burn;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class GcmIntentService extends IntentService {
    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);
        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                int index = Integer.parseInt(extras.getString("index"));
                if(extras.getString("message").equals("flare"))
                    sendNotification(getString(R.string.notification_flare_received), null,index);
                else if(extras.getString("message").equals("eflare")){
                    sendNotification(getString(R.string.notification_group_flare_received), null,index);
                }
                else if(extras.getString("message").equals("poke")) {
                    String s;
                    try {
                        s = URLDecoder.decode(extras.getString("m"), "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        s = extras.getString("m");
                    }
                    sendNotification(getString(R.string.notification_poke_received), s,index);
                }else if(extras.getString("message").equals("accept")){
                    Intent RTReturn = new Intent(Global.rcv1);
                    RTReturn.putExtra("act","accept");
                    LocalBroadcastManager.getInstance(this).sendBroadcast(RTReturn);
                }else if(extras.getString("message").equals("decline")){
                    Intent RTReturn = new Intent(Global.rcv1);
                    RTReturn.putExtra("act","decline");
                    LocalBroadcastManager.getInstance(this).sendBroadcast(RTReturn);
                }
            }
        }
        GumBroadcastReceiver.completeWakefulIntent(intent);
    }
    private void sendNotification(String msg,String poke,int index) {
        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("index",index);
        intent.putExtra("poke",poke);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,intent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.new_1)
                        .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(msg+(poke!=null?poke:""))).setAutoCancel(true)
                        .setContentText(msg+(poke!=null?poke:""))
                        .setContentTitle(poke!=null?getString(R.string.notification_title_poke):getString(R.string.notification_title_flare));
        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(index, mBuilder.build());
        try {
            final MediaPlayer mPlayer = MediaPlayer.create(getBaseContext(), R.raw.kipod_audio);
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mPlayer.release();
                }
            });
            mPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

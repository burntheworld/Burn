package krk.hez.burn;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;

public class AnalyticsApp extends Application {


    public AnalyticsApp() {
        super();
    }

    synchronized Tracker getTracker() {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
        Tracker t = analytics.newTracker(Kk.analytics);
        t.setSessionTimeout(300);
        t.enableAutoActivityTracking(true);
        t.enableExceptionReporting(true);
        t.enableAdvertisingIdCollection(false);
        return t;
    }
}

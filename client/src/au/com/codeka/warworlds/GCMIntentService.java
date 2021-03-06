package au.com.codeka.warworlds;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import au.com.codeka.common.Log;
import au.com.codeka.warworlds.model.EmpireManager;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

/**
 * Receive a push message from the Google Cloud Messaging.
 */
public class GCMIntentService extends GCMBaseIntentService {
    private static Log log = new Log("GCMIntentService");

    public static String PROJECT_ID = "990931198580";

    public GCMIntentService() {
        super(PROJECT_ID);
    }

    public static void register(Activity activity) {
        GCMRegistrar.register(activity, PROJECT_ID);
    }

    public static void unregister(Activity activity) {
        GCMRegistrar.unregister(activity);
    }

    @Override
    public void onRegistered(Context context, String gcmRegistrationID) {
        log.info("GCM device registration complete, gcmRegistrationID = %s", gcmRegistrationID);
        DeviceRegistrar.updateGcmRegistration(context, gcmRegistrationID);
    }

    @Override
    public void onUnregistered(Context context, String deviceRegistrationID) {
        log.info("Unregistered from GCM, deviceRegistrationID = "+deviceRegistrationID);
        DeviceRegistrar.unregister(false);
    }

    /** Called where there's a non-recoverable error.*/
    @Override
    public void onError(Context context, String errorId) {
        log.error("An error has occured! Error: %s", errorId);
    }

    /** Called when there's a \i recoverable error. */
    @Override
    public boolean onRecoverableError(Context context, String errorId) {
        log.error("A recoverable error has occured, trying again. Error: %s", errorId);
        return true;
    }

    /** Called when a cloud message has been received. */
    @Override
    public void onMessage(Context context, Intent intent) {
        // since this can be called when the application is not running, make sure we're
        // set to go still.
        Util.loadProperties();
        Util.setup(context);

        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey("sitrep")) {
                Notifications.handleNotfication(context, "sitrep", extras.getString("sitrep"));
            }
            if (extras.containsKey("chat")) {
                Notifications.handleNotfication(context, "chat", extras.getString("chat"));
            }
            if (extras.containsKey("empire_updated")) {
                EmpireManager.i.refreshEmpire();
            }
        }
    }
}

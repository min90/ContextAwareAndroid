package sdu.dk.iot.contextaware;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.awareness.fence.FenceState;

/**
 * Created by Jesper on 05/05/2017.
 */

public class FenceReceiver extends BroadcastReceiver {
    private static final String DEBUG_TAG = FenceReceiver.class.getSimpleName();
    private static final String SHARED = "MY_PREFS";
    private Context context;
    private NetworkManager networkManager;
    private String name;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public FenceReceiver(Context context) {
        this.context = context;
        this.networkManager = new NetworkManager(context);
        sharedPreferences = context.getSharedPreferences(SHARED, Context.MODE_PRIVATE);
        editor = this.sharedPreferences.edit();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        FenceState fenceState = FenceState.extract(intent);
        boolean send = false;
        boolean arrived = false;
        if (sharedPreferences.getString("name", null) != null) {
            name = sharedPreferences.getString("name", "No name");
            arrived = sharedPreferences.getBoolean("arrived", false);
            send = true;
        }

        if(TextUtils.equals(fenceState.getFenceKey(), "daycareAndMorningFence")) {
            switch(fenceState.getCurrentState()) {
                case FenceState.TRUE:

                    Log.d(DEBUG_TAG, "Daycare fence is active");
                    if (send) {
                        if (arrived) editor.putBoolean("arrived", false);
                        networkManager.addArrival(new Arrival(name, arrived));
                    }
                    break;
                case FenceState.FALSE:
                    Log.d(DEBUG_TAG, "Daycare fence is NOT active");
                    break;
                case FenceState.UNKNOWN:
                    Log.d(DEBUG_TAG, "Daycare fence is in an unknown state");
            }
        }
    }
}

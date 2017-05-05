package sdu.dk.iot.contextaware;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.awareness.fence.LocationFence;
import com.google.android.gms.awareness.fence.TimeFence;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;

import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String DEBUG_TAG = MainActivity.class.getSimpleName();

    private static final String SHARED = "MY_PREFS";
    private static final String FENCE_RECEIVER_ACTION = "fence_receiver_action";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private PendingIntent fencePendingIntent;


    private GoogleApiClient client;
    private static final double LAT_44 = 55.367302;
    private static final double LONG_44 = 10.430714;
    private static final double RADIUS_44 = 50;
    private static final TimeZone COPENHAGEN = TimeZone.getTimeZone("Europe/Copenhagen");

    private static final Long ARRIVAL_TIME = 8L * 60L * 60L * 1000L;
    private static final Long LEAVING_TIME = 16L * 60L * 60L * 1000L;

    private AwarenessFence daycareAndMorningFence;
    private FenceReceiver fenceReceiver;

    private FloatingActionButton fab;
    private EditText edtName;
    private TextView txtInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.sharedPreferences = this.getSharedPreferences(SHARED, Context.MODE_PRIVATE);
        this.editor = this.sharedPreferences.edit();

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
        edtName = (EditText) findViewById(R.id.edt_name);
        txtInfo = (TextView) findViewById(R.id.txt_info);

        connectGoogleAware();
        registerFences();
        registerIntentForFences();



    }

    private void connectGoogleAware() {
        client = new GoogleApiClient.Builder(this)
                .addApi(Awareness.API)
                .build();
        client.connect();
    }

    private void registerFences() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        AwarenessFence daycareFence = LocationFence.entering(LAT_44, LONG_44, RADIUS_44);
        AwarenessFence morningFence = TimeFence.inDailyInterval(COPENHAGEN, ARRIVAL_TIME, LEAVING_TIME);

        daycareAndMorningFence = AwarenessFence.and(daycareFence, morningFence);
    }

    private void registerIntentForFences() {
        Intent fenceIntent = new Intent(FENCE_RECEIVER_ACTION);
        fencePendingIntent = PendingIntent.getBroadcast(this, 0, fenceIntent, 0);
        fenceReceiver = new FenceReceiver(this);
        registerReceiver(fenceReceiver, new IntentFilter(FENCE_RECEIVER_ACTION));
    }

    protected void registerContextFence(final String fenceKey, final AwarenessFence daycareFence) {
        Awareness.FenceApi.updateFences(client,
                new FenceUpdateRequest.Builder()
                .addFence(fenceKey, daycareFence, fencePendingIntent)
                .build()).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if(status.isSuccess()) {
                    Log.d(DEBUG_TAG, "Fence was successfully created");
                } else {
                    Log.d(DEBUG_TAG, "Fence could not be registered: " + status);
                }
            }
        });
    }

    protected void unregisterContextFence(final String fenceKey) {
        Awareness.FenceApi.updateFences(client,
                new FenceUpdateRequest.Builder()
                .removeFence(fenceKey)
                .build()).setResultCallback(new ResultCallbacks<Status>() {
            @Override
            public void onSuccess(@NonNull Status status) {
                Log.d(DEBUG_TAG, "Fence " + fenceKey + " successfully removed");
            }

            @Override
            public void onFailure(@NonNull Status status) {
                Log.d(DEBUG_TAG, "Fence " + fenceKey + " could NOT be removed");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerContextFence("daycareAndMorningFence", daycareAndMorningFence);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterContextFence("daycareAndMorningFence");
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == fab.getId()) {
            String name = edtName.getText().toString();
            editor.putString("name", name).commit();
            txtInfo.setText(sharedPreferences.getString("name", "No name").concat(" is saved!"));
        }
    }
}

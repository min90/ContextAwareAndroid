package sdu.dk.iot.contextaware;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Jesper on 05/05/2017.
 */

public class NetworkManager {

    private static final String SHARED = "MY_PREFS";
    private static final String DEBUG_TAG = NetworkManager.class.getSimpleName();
    private static final String BASE_URL = "https://intense-sea-52416.herokuapp.com/";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private Context context;

    public NetworkManager(Context context) {
        this.context = context;
        this.sharedPreferences = this.context.getSharedPreferences(SHARED, Context.MODE_PRIVATE);
        this.editor = this.sharedPreferences.edit();
    }

    private Retrofit getRetrofitInstance(OkHttpClient builder) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(builder)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private OkHttpClient builder() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);
        return httpClient.build();

    }

    public void addArrival(Arrival arrival) {
        Log.d(DEBUG_TAG, "Name of arrival is: " + sharedPreferences.getString("name", null));

        ApiEndpoints apiEndpoints = getRetrofitInstance(builder()).create(ApiEndpoints.class);
        Call callToCreateArrival = apiEndpoints.createArrival(arrival);
        callToCreateArrival.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                Log.d(DEBUG_TAG, "Arrival: " + response.body());
                Toast.makeText(context, "Created arrival", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Log.d(DEBUG_TAG, "Arrival failure: " + t.toString());
                Toast.makeText(context, "Arrival creations failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

package sdu.dk.iot.contextaware;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by Jesper on 05/05/2017.
 */

public interface ApiEndpoints {

    @POST("api/v1/arrivals")
    Call<Arrival> createArrival(@Body Arrival arrival);

}

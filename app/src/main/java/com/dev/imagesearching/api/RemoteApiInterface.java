package com.dev.imagesearching.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * (c) All rights reserved.
 * Defines all the api calls to fetch the weather details.
 */

public interface RemoteApiInterface {

    @GET(IMGUR_API_URL)
    Call<ImagesResponse> getCurrentWeatherForecast(@Query("q") final String latLong);
}

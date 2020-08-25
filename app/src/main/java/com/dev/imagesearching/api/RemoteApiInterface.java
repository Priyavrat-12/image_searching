package com.dev.imagesearching.api;

import com.dev.imagesearching.models.ImagesResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Path;
import retrofit2.http.Query;

import static com.dev.imagesearching.utils.NetworkConstants.IMAGE_PATH_URI;
import static com.dev.imagesearching.utils.NetworkConstants.IMGUR_API_VERSION;

/**
 * (c) All rights reserved.
 *
 * Defines all the api calls/ methods.
 */

public interface RemoteApiInterface {

    @GET("/" + IMGUR_API_VERSION + IMAGE_PATH_URI)
    Call<ImagesResponse> getImagesList(@HeaderMap Map<String, String> headers,
                                       @Path("pageNo") int pageNo,
                                       @Query("q") final String keyword);
}

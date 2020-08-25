package com.dev.imagesearching.api;

import okhttp3.OkHttpClient;

import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.dev.imagesearching.utils.NetworkConstants.BASE_ENDPOINT_URL;

/**
 * (c) All rights are reserved.
 *
 * This class prepares the initial Networking API(Retrofit) object which is needed to make the REST API call.
 */

public class ApiClient {

    private static ApiClient sApiClient;
    private static Retrofit sRetrofit;

    private ApiClient() {
        prepareRetrofitInstance();
    }

    /**
     * Method to prepare retrofit API instance, with request URL, response converter etc.
     */
    private void prepareRetrofitInstance() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient httpClient = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        sRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_ENDPOINT_URL)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create()) // Adding the GSON converter, response will be mapped GSON ->> POJO.
                .build();
    }

    /**
     * Preparing the singleton instance of retrofit API client.
     * @return sApiClient
     */
    public static ApiClient getInstance () {
        if (null == sApiClient) {
            synchronized (ApiClient.class) {
                sApiClient = new ApiClient();
            }
        }
        return sApiClient;
    }

    /**
     * To create and return the API interface object.
     */
    public RemoteApiInterface getApiInterface() {
        return sRetrofit.create(RemoteApiInterface.class);
    }
}

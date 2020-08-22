package com.dev.imagesearching.api;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

import okhttp3.internal.http.CallServerInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * (c) . All rights are reserved.
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
        HttpLog interceptor = new CallServerInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient httpClient = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        sRetrofit = new Retrofit.Builder()
                .baseUrl(WEATHER_APP_REQUEST_URL)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
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

    public RemoteApiInterface getApiInterface() {
        return sRetrofit.create(RemoteApiInterface.class);
    }
}

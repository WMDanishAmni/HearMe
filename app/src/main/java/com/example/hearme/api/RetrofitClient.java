// file: RetrofitClient.java
package com.example.hearme.api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit client with logging and reasonable timeouts.
 *
 * IMPORTANT:
 * - For emulator use: BASE_URL = "http://10.0.2.2/fullsms_api/";
 * - For real device use: BASE_URL = "http://192.168.x.y/fullsms_api/"; (replace with your laptop IP)
 *
 * The base URL MUST end with a trailing slash.
 */
public class RetrofitClient {
    private static Retrofit retrofit = null;

    // EDIT THIS depending on environment:
    // For emulator:
    // private static final String BASE_URL = "http://10.0.2.2/fullsms_api/";
    // For real device (replace with your laptop LAN IP):
    // private static final String BASE_URL = "http://192.168.1.100/fullsms_api/";
    private static final String BASE_URL = "http://10.119.89.233/hear_me_api/";

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            // Logging interceptor for debugging
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }

    public static ConversationApiService getApiService() {
        return getRetrofitInstance().create(ConversationApiService.class);
    }
}

package com.example.hearme.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // --- THIS IS THE LINE TO CHANGE ---
    //
    // ⬇️ REPLACE THIS (Emulator IP)
    // private static final String BASE_URL = "http://10.0.2.2/hear_me_api/";
    //
    // ⬇️ WITH THIS (Your computer's Wi-Fi IP)
    private static final String BASE_URL = "http://192.168.67.98/hear_me_api/"; // 👈 Use your IP


    private static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> {
                android.util.Log.d("HTTP", message);
            });
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
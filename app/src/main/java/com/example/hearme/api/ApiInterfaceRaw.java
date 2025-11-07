// file: ApiInterfaceRaw.java
package com.example.hearme.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiInterfaceRaw {

    // --- FIX: Point this to your new login.php file ---
    @FormUrlEncoded
    @POST("login.php")
    Call<ResponseBody> loginRaw(
            @Field("username") String username,
            @Field("password") String password
    );

    // You might have other "raw" calls here
}
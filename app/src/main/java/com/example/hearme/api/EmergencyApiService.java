package com.example.hearme.api;

import com.example.hearme.models.BasicResponse; // ⭐️ FIXED IMPORT
import com.example.hearme.models.CustomCallResponseModel; // ⭐️ FIXED IMPORT
import com.example.hearme.models.EmergencyHistoryResponseModel; // ⭐️ FIXED IMPORT

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface EmergencyApiService {

    @GET("get_custom_calls.php")
    Call<CustomCallResponseModel> getCustomCalls(@Query("token") String token);

    @GET("get_emergency_history.php")
    Call<EmergencyHistoryResponseModel> getEmergencyHistory(@Query("token") String token);

    // ⭐️ --- ADDED THIS METHOD --- ⭐️
    @FormUrlEncoded
    @POST("save_custom_call.php")
    Call<BasicResponse> saveCustomCall(
            @Field("token") String token,
            @Field("custom_name") String customName,
            @Field("custom_number") String customNumber
    );

    // ⭐️ --- ADDED THIS METHOD --- ⭐️
    @FormUrlEncoded
    @POST("delete_custom_call.php")
    Call<BasicResponse> deleteCustomCall(
            @Field("token") String token,
            @Field("custom_id") int customId
    );
}
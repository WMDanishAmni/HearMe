package com.example.hearme.api;

import com.example.hearme.models.CustomCallResponseModel;
import com.example.hearme.models.EmergencyHistoryResponseModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface EmergencyApiService {

    @GET("get_custom_calls.php")
    Call<CustomCallResponseModel> getCustomCalls(@Query("token") String token);

    // --- (NEW) ADD THIS METHOD ---
    @GET("get_emergency_history.php")
    Call<EmergencyHistoryResponseModel> getEmergencyHistory(@Query("token") String token);
}
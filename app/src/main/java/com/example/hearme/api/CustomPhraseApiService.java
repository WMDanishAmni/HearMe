// file: CustomPhraseApiService.java
// package should match your other API interfaces (adjust if needed)
package com.example.hearme.api;

import com.example.hearme.models.CustomPhraseListResponseModel;
import com.example.hearme.models.CustomPhraseResponseModel;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface CustomPhraseApiService {

    // Save a custom phrase for a specific user
    @FormUrlEncoded
    @POST("save_custom_phrase.php")
    Call<CustomPhraseResponseModel> saveCustomPhrase(
            @Field("user_id") int userId,
            @Field("category") String category,
            @Field("phrase") String phrase,
            @Field("language") String language
    );

    // Get all custom phrases for a specific user (optionally filter by language)
    @GET("get_custom_phrases.php")
    Call<CustomPhraseListResponseModel> getCustomPhrases(
            @Query("user_id") int userId,
            @Query("language") String language
    );
}

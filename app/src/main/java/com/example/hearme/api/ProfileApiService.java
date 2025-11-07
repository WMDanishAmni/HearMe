// file: ProfileApiService.java
package com.example.hearme.api;

import com.example.hearme.models.UpdateProfileResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ProfileApiService {

    @FormUrlEncoded
    @POST("update_profile.php")
    Call<UpdateProfileResponse> updateProfile(
            @Field("token") String token,
            @Field("username") String username,
            @Field("full_name") String fullName,
            @Field("email") String email,
            @Field("address") String address,
            @Field("old_password") String oldPassword,
            @Field("new_password") String newPassword,
            @Field("confirm_password") String confirmPassword
    );
}
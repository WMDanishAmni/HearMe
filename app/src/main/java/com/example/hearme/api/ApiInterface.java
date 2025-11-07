package com.example.hearme.api;

import com.example.hearme.models.AdminDashboardResponse;
import com.example.hearme.models.AdminUserListResponse;
import com.example.hearme.models.BasicResponse;
import com.example.hearme.models.ForgotPasswordResponse;
import com.example.hearme.models.GetEmergencyUsersResponse;
import com.example.hearme.models.UnifiedLoginResponse;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiInterface {

    // --- From Your (Amni's) Project ---

    @FormUrlEncoded
    @POST("unified_login.php")
    Call<UnifiedLoginResponse> unifiedLogin(
            @Field("username") String username,
            @Field("password") String password
    );

    @FormUrlEncoded
    @POST("signup.php")
    Call<Map<String, String>> signup(
            @Field("username") String username,
            @Field("password") String password,
            @Field("email") String email,
            @Field("full_name") String fullName,
            @Field("address") String address,
            @Field("phone_no") String phoneNo
    );

    @FormUrlEncoded
    @POST("forgot_password_request.php")
    Call<ForgotPasswordResponse> forgotPassword(
            @Field("email") String email
    );

    @FormUrlEncoded
    @POST("verify_otp.php")
    Call<BasicResponse> verifyOtp(
            @Field("email") String email,
            @Field("otp") String otp
    );

    @FormUrlEncoded
    @POST("reset_password.php")
    Call<BasicResponse> resetPassword(
            @Field("email") String email,
            @Field("new_password") String newPassword,
            @Field("confirm_password") String confirmPassword
    );

    @GET("admin_dashboard.php")
    Call<AdminDashboardResponse> getAdminDashboardData();

    @GET("admin_user_details.php")
    Call<AdminUserListResponse> getAdminUserList(@Query("search") String search);

    @GET("get_emergency_users.php")
    Call<GetEmergencyUsersResponse> getEmergencyUsers(@Query("category") String category);
}
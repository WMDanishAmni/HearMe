// file: ConversationApiService.java
// Make sure this file is in the same package as your ApiClient
package com.example.hearme.api;

import com.example.hearme.models.ChatHistoryResponseModel;
import com.example.hearme.models.ConversationResponseModel;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ConversationApiService {

    @FormUrlEncoded
    @POST("api.php")
    Call<ConversationResponseModel> saveConversation(
            @Field("user_id") int userId,
            @Field("hear") String hearText,
            @Field("speak") String speakText
    );

    @FormUrlEncoded
    @POST("delete_conversation.php")
    Call<ConversationResponseModel> deleteConversation(
            @Field("chat_id") int chatId,
            @Field("token") String token // <-- ADD THIS TOKEN
    );

    @GET("get_chat_history.php")
    Call<ChatHistoryResponseModel> getChatHistory(@Query("user_id") int userId);
}